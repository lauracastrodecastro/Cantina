package com.example.cantina;

import android.os.Bundle;
import android.text.InputType;
import android.widget.*;
import java.util.*;
import com.google.firebase.firestore.DocumentSnapshot;

public class PedidoActivity extends TelaBase {
    private final Map<String,EditText> quantidades=new LinkedHashMap<>();
    private final Map<String,Produto> produtos=new LinkedHashMap<>();
    private boolean balcao;
    private String alunoId;
    @Override protected void onCreate(Bundle state) {
        super.onCreate(state); balcao=getIntent().getBooleanExtra("balcao",false);
        acesso(balcao ? new String[]{"CANTINA"} : new String[]{"ALUNO"});
    }
    @Override protected void carregar() {
        titulo(balcao ? "Venda no balcão" : "Cardápio de hoje");
        if(balcao) {
            texto(conteudo,"Selecione o aluno");
            db.collection("usuarios").whereEqualTo("tipo","ALUNO").get().addOnSuccessListener(snap -> {
                if(snap.isEmpty()) { texto(conteudo,"Nenhum aluno cadastrado."); return; }
                List<String> nomes=new ArrayList<>(); List<DocumentSnapshot> alunos=snap.getDocuments();
                for(DocumentSnapshot a:alunos) nomes.add(a.getString("nome")+" • "+a.getString("turma")+" • "+a.getId().substring(0,6));
                Spinner escolha=opcoes(conteudo,nomes);
                escolha.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
                    @Override public void onItemSelected(android.widget.AdapterView<?> p,android.view.View v,int pos,long id) { alunoId=alunos.get(pos).getId(); }
                    @Override public void onNothingSelected(android.widget.AdapterView<?> p) { alunoId=null; }
                });
                alunoId=alunos.get(0).getId(); montar();
            }).addOnFailureListener(this::erro);
        } else { alunoId=auth.getUid(); montar(); }
    }
    private void montar() {
        texto(conteudo,"Compras na conta respeitam o limite mensal. Pix, dinheiro e cartão são pagos presencialmente na cantina. Até 5 produtos diferentes por pedido.");
        LinearLayout lista=grupo(conteudo);
        TextView total=texto(conteudo,"Total: R$0.00");
        texto(conteudo,"Horário de retirada");
        Spinner horario=opcoes(conteudo,balcao ? Arrays.asList("Agora") : Arrays.asList("09:00","15:30"));
        texto(conteudo,"Forma de pagamento");
        Spinner pagamento=opcoes(conteudo,Arrays.asList("Conta do aluno","Pix","Dinheiro","Cartão"));
        if(balcao) texto(conteudo,"Confirme o recebimento antes de finalizar vendas em Pix, dinheiro ou cartão.");
        Button finalizar=botao(conteudo,balcao ? "Finalizar venda" : "Confirmar pedido", () -> {});
        finalizar.setEnabled(false);
        db.collection("produtos").whereEqualTo("dataCardapio",Repositorio.hoje()).get().addOnSuccessListener(snap -> {
            int exibidos=0;
            for(DocumentSnapshot doc:snap.getDocuments()) {
                Produto p=Produto.de(doc); if(!p.disponivel || p.estoque<=0) continue; exibidos++;
                produtos.put(p.id,p); LinearLayout card=grupo(lista);
                ImageView foto=new ImageView(this); foto.setAdjustViewBounds(true); foto.setMaxHeight(240); card.addView(foto); FotoProduto.mostrar(foto,p.fotoBase64);
                texto(card,p.nome+" • "+Dinheiro.formatar(p.precoCentavos)); texto(card,p.descricao==null ? "" : p.descricao);
                texto(card,"Disponível: "+p.estoque+" unidades");
                EditText qtd=campo(card,"Quantidade",InputType.TYPE_CLASS_NUMBER); qtd.setText("0"); quantidades.put(p.id,qtd);
                qtd.addTextChangedListener(new android.text.TextWatcher() {
                    @Override public void beforeTextChanged(CharSequence s,int st,int count,int after) {}
                    @Override public void onTextChanged(CharSequence s,int st,int before,int count) { atualizarTotal(total); }
                    @Override public void afterTextChanged(android.text.Editable s) {}
                });
            }
            if(exibidos==0) texto(lista,"Nenhum produto disponível para hoje.");
            finalizar.setEnabled(exibidos>0);
        }).addOnFailureListener(this::erro);
        finalizar.setOnClickListener(v -> {
            try {
                Map<String,Long> carrinho=new LinkedHashMap<>();
                for(Map.Entry<String,EditText> e:quantidades.entrySet()) {
                    String texto=e.getValue().getText().toString(); long n=texto.isEmpty() ? 0 : Long.parseLong(texto);
                    if(n>0) carrinho.put(e.getKey(),n);
                }
                if(alunoId==null || carrinho.isEmpty() || carrinho.size()>5) { aviso("Selecione o aluno e de 1 a 5 produtos diferentes."); return; }
                finalizar.setEnabled(false);
                Repositorio.comprar(alunoId,carrinho,horario.getSelectedItem().toString(),balcao,new String[]{"CONTA","PIX","DINHEIRO","CARTAO"}[pagamento.getSelectedItemPosition()])
                    .addOnSuccessListener(id -> { new androidx.appcompat.app.AlertDialog.Builder(this).setTitle("Compra confirmada").setMessage("Código de retirada: "+id).setCancelable(false).setPositiveButton("Concluir",(d,w)->finish()).show(); })
                    .addOnFailureListener(e -> { finalizar.setEnabled(true); erro(e); });
            } catch(Exception e) { aviso("Informe quantidades válidas."); }
        });
    }
    private void atualizarTotal(TextView total) {
        try {
            long soma=0;
            for(Map.Entry<String,EditText> e:quantidades.entrySet()) {
                String s=e.getValue().getText().toString(); long n=s.isEmpty()?0:Long.parseLong(s);
                if(n>1000) throw new IllegalArgumentException();
                soma=Math.addExact(soma,Math.multiplyExact(n,produtos.get(e.getKey()).precoCentavos));
            }
            total.setText("Total: "+Dinheiro.formatar(soma));
        } catch(Exception e) { total.setText("Quantidade inválida (máximo 1000 por produto)."); }
    }
}
