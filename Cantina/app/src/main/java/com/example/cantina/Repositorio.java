package com.example.cantina;

import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.*;
import java.time.*;
import java.util.*;

public final class Repositorio {
    private Repositorio() {}
    public static String hoje() { return LocalDate.now(ZoneId.of("America/Sao_Paulo")).toString(); }
    public static String mes() { return hoje().substring(0,7); }
    public static long numero(DocumentSnapshot d,String campo) { Long n=d.getLong(campo); return n==null ? 0 : n; }
    public static Task<String> comprar(String alunoId, Map<String,Long> carrinho, String horario, boolean balcao, String pagamento) {
        FirebaseFirestore db=FirebaseFirestore.getInstance();
        String autor=FirebaseAuth.getInstance().getUid(), mes=mes(), data=hoje();
        Map<String,Long> quantidades=new LinkedHashMap<>(carrinho);
        DocumentReference alunoRef=db.collection("usuarios").document(alunoId);
        DocumentReference consumoRef=db.collection("consumos").document(alunoId+"_"+mes);
        DocumentReference pedidoRef=db.collection("pedidos").document();
        return db.runTransaction(tx -> {
            if(quantidades.isEmpty() || quantidades.size()>5) throw new IllegalStateException("Selecione de 1 a 5 produtos diferentes por pedido.");
            DocumentSnapshot aluno=tx.get(alunoRef), consumo=tx.get(consumoRef);
            if(!"ALUNO".equals(aluno.getString("tipo"))) throw new IllegalStateException("Aluno inválido.");
            if("CONTA".equals(pagamento) && "FECHADO".equals(consumo.getString("status"))) throw new IllegalStateException("A conta deste mês já foi fechada.");
            List<DocumentSnapshot> produtos=new ArrayList<>();
            for(String id:quantidades.keySet()) produtos.add(tx.get(db.collection("produtos").document(id)));
            List<Map<String,Object>> itens=new ArrayList<>(); long total=0;
            for(DocumentSnapshot doc:produtos) {
                long qtd=quantidades.get(doc.getId()), preco=numero(doc,"precoCentavos");
                if(!doc.exists() || !Boolean.TRUE.equals(doc.getBoolean("disponivel")) || !data.equals(doc.getString("dataCardapio"))) throw new IllegalStateException("Um produto não está mais no cardápio de hoje.");
                if(qtd<=0 || qtd>1000 || numero(doc,"estoque")<qtd) throw new IllegalStateException("Estoque insuficiente: " + doc.getString("nome"));
                Map<String,Object> item=new HashMap<>(); item.put("produtoId",doc.getId()); item.put("nome",doc.getString("nome"));
                item.put("quantidade",qtd); item.put("precoCentavos",preco); itens.add(item);
                total=Math.addExact(total,Math.multiplyExact(preco,qtd));
            }
            long usado=numero(consumo,"totalCentavos"), limite=numero(aluno,"limiteMensalCentavos");
            if(total<=0 || ("CONTA".equals(pagamento) && usado+total>limite)) throw new IllegalStateException("Limite mensal insuficiente. Disponível: " + Dinheiro.formatar(Math.max(0,limite-usado)));
            Map<String,Object> pedido=new HashMap<>(); pedido.put("alunoId",alunoId); pedido.put("alunoNome",aluno.getString("nome"));
            pedido.put("autorId",autor); pedido.put("itens",itens); pedido.put("quantidades",quantidades); pedido.put("totalCentavos",total);
            pedido.put("horarioRetirada",horario); pedido.put("origem",balcao ? "BALCAO" : "ANTECIPADO"); pedido.put("formaPagamento",pagamento); pedido.put("pagamentoStatus","CONTA".equals(pagamento) ? "NA_CONTA" : (balcao ? "PAGO" : "PENDENTE"));
            pedido.put("status",balcao ? "RETIRADO" : "PENDENTE"); pedido.put("data",data); pedido.put("mesReferencia",mes); pedido.put("criadoEm",FieldValue.serverTimestamp());
            Map<String,Object> conta=new HashMap<>(); conta.put("alunoId",alunoId); conta.put("mesReferencia",mes);
            conta.put("totalCentavos",usado+total); conta.put("ultimoPedidoId",pedidoRef.getId()); conta.put("status","ABERTO");
            for(DocumentSnapshot doc:produtos) tx.update(doc.getReference(),"estoque",numero(doc,"estoque")-quantidades.get(doc.getId()),"ultimoPedidoId",pedidoRef.getId());
            tx.set(pedidoRef,pedido); if("CONTA".equals(pagamento)) tx.set(consumoRef,conta);
            tx.set(db.collection("lancamentos").document(pedidoRef.getId()),pedido);
            return pedidoRef.getId();
        });
    }
}
