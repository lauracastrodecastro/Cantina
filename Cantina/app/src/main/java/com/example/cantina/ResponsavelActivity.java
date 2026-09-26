package com.example.cantina;

import android.os.Bundle;
import android.widget.*;

public class ResponsavelActivity extends TelaBase {
    @Override protected void onCreate(Bundle state) { super.onCreate(state); acesso("RESPONSAVEL"); }
    @Override protected void carregar() {
        titulo("Meus alunos");
        TextView id = texto(conteudo,"Meu código de responsável: " + auth.getUid()); id.setTextIsSelectable(true);
        texto(conteudo,"Apresente este código à cantina para confirmar o vínculo. O limite mensal é para compras na conta e renova a disponibilidade a cada mês.");
        LinearLayout lista = grupo(conteudo);
        ouvir(db.collection("usuarios").whereEqualTo("responsavelId",auth.getUid()).addSnapshotListener((snap,e) -> {
            if(e!=null) { erro(e); return; }
            lista.removeAllViews();
            if(snap.isEmpty()) texto(lista,"Nenhum aluno vinculado. Solicite o vínculo à cantina.");
            for(com.google.firebase.firestore.DocumentSnapshot aluno : snap.getDocuments()) {
                LinearLayout card = grupo(lista);
                texto(card,aluno.getString("nome") + " • " + aluno.getString("turma"));
                texto(card,"Limite atual: " + Dinheiro.formatar(numero(aluno,"limiteMensalCentavos")));
                EditText limite = moeda(card,"Novo limite mensal (R$)");
                limite.setText(Dinheiro.numero(numero(aluno,"limiteMensalCentavos")));
                Button salvar = botao(card,"Salvar limite", () -> {});
                salvar.setOnClickListener(v -> {
                    try {
                        long centavos = Dinheiro.centavos(limite.getText().toString());
                        salvar.setEnabled(false);
                        db.collection("usuarios").document(aluno.getId()).update("limiteMensalCentavos",centavos)
                            .addOnSuccessListener(ok -> aviso("Limite atualizado."))
                            .addOnFailureListener(ex -> { salvar.setEnabled(true); erro(ex); });
                    } catch(IllegalArgumentException ex) { erro(ex); }
                });
                botao(card,"Ver extrato", () -> startActivity(new android.content.Intent(this,ExtratoActivity.class).putExtra("alunoId",aluno.getId())));
            }
        }));
        botao(conteudo,"Sair",this::sair);
    }
}
