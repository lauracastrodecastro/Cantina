package com.example.cantina;

import android.os.Bundle;
import android.widget.*;

public class PedidoCantina extends TelaBase {
    @Override protected void onCreate(Bundle state) { super.onCreate(state); acesso("CANTINA"); }
    @Override protected void carregar() {
        titulo("Pedidos e retirada");
        LinearLayout lista=grupo(conteudo);
        ouvir(db.collection("pedidos").whereEqualTo("data",Repositorio.hoje()).addSnapshotListener((snap,e) -> {
            if(e!=null) { erro(e); return; } lista.removeAllViews();
            if(snap.isEmpty()) texto(lista,"Nenhum pedido hoje.");
            for(com.google.firebase.firestore.DocumentSnapshot d:snap.getDocuments()) {
                LinearLayout card=grupo(lista); String status=d.getString("status");
                texto(card,d.getString("alunoNome")+" • "+status+" • "+Dinheiro.formatar(numero(d,"totalCentavos")));
                texto(card,"Código: "+d.getId()+" • "+d.getString("horarioRetirada"));
                Object itens=d.get("itens");
                if(itens instanceof java.util.List) for(Object item:(java.util.List<?>)itens) if(item instanceof java.util.Map) {
                    java.util.Map<?,?> i=(java.util.Map<?,?>)item; texto(card,i.get("quantidade")+" × "+i.get("nome"));
                }
                texto(card,"Pagamento: "+d.getString("formaPagamento")+" • "+d.getString("pagamentoStatus"));
                if("PENDENTE".equals(d.getString("pagamentoStatus"))) botao(card,"Registrar pagamento recebido", () -> new androidx.appcompat.app.AlertDialog.Builder(this)
                    .setTitle("Confirmar recebimento").setMessage("O pagamento integral foi recebido?")
                    .setNegativeButton("Cancelar",null).setPositiveButton("Sim",(dialog,w)->d.getReference().update("pagamentoStatus","PAGO").addOnFailureListener(this::erro)).show());
                if("PENDENTE".equals(status)) botao(card,"Marcar pronto",()->d.getReference().update("status","PRONTO").addOnFailureListener(this::erro));
                if("PRONTO".equals(status) && !"PENDENTE".equals(d.getString("pagamentoStatus"))) botao(card,"Confirmar retirada",()->{
                    EditText codigo=new EditText(this); codigo.setHint("Código completo do pedido");
                    new androidx.appcompat.app.AlertDialog.Builder(this).setTitle("Identificar retirada").setView(codigo)
                        .setNegativeButton("Cancelar",null).setPositiveButton("Confirmar",(dialog,w)->{
                            if(!d.getId().equals(codigo.getText().toString().trim())) { aviso("Código incorreto. Retirada não confirmada."); return; }
                            d.getReference().update("status","RETIRADO").addOnFailureListener(this::erro);
                        }).show();
                });
            }
        }));
    }
}
