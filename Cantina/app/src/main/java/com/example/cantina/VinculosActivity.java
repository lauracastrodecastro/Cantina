package com.example.cantina;

import android.os.Bundle;
import android.text.InputType;
import android.widget.*;

public class VinculosActivity extends TelaBase {
    @Override protected void onCreate(Bundle state) { super.onCreate(state); acesso("CANTINA"); }
    @Override protected void carregar() {
        titulo("Vincular responsáveis");
        texto(conteudo,"Confirme a identidade do responsável antes de vincular. Somente o responsável vinculado pode definir o limite mensal.");
        LinearLayout lista=grupo(conteudo);
        ouvir(db.collection("usuarios").whereEqualTo("tipo","ALUNO").addSnapshotListener((snap,e) -> {
            if(e!=null) { erro(e); return; } lista.removeAllViews();
            for(com.google.firebase.firestore.DocumentSnapshot a:snap.getDocuments()) {
                LinearLayout card=grupo(lista); texto(card,a.getString("nome")+" • "+a.getString("turma"));
                texto(card,"Limite definido pelo responsável: "+Dinheiro.formatar(numero(a,"limiteMensalCentavos")));
                EditText id=campo(card,"Código do responsável",InputType.TYPE_CLASS_TEXT); id.setText(a.getString("responsavelId"));
                Button vincular=botao(card,"Confirmar vínculo", () -> {});
                vincular.setOnClickListener(v -> {
                    String uid=id.getText().toString().trim();
                    if(uid.isEmpty() || uid.contains("/")) { aviso("Informe o código completo do responsável."); return; }
                    vincular.setEnabled(false);
                    db.collection("usuarios").document(uid).get().addOnSuccessListener(responsavel -> {
                        if(!"RESPONSAVEL".equals(responsavel.getString("tipo"))) { aviso("Responsável não encontrado."); vincular.setEnabled(true); return; }
                        new androidx.appcompat.app.AlertDialog.Builder(this).setTitle("Confirmar responsável")
                            .setMessage("Vincular "+responsavel.getString("nome")+" a "+a.getString("nome")+"?")
                            .setNegativeButton("Cancelar",(d,w)->vincular.setEnabled(true))
                            .setOnCancelListener(d->vincular.setEnabled(true))
                            .setPositiveButton("Vincular",(d,w)->a.getReference().update("responsavelId",uid)
                                .addOnSuccessListener(ok -> aviso("Vínculo salvo."))
                                .addOnFailureListener(ex -> { vincular.setEnabled(true); erro(ex); })).show();
                    }).addOnFailureListener(ex -> { vincular.setEnabled(true); erro(ex); });
                });
                botao(card,"Ver extrato", () -> startActivity(new android.content.Intent(this,ExtratoActivity.class).putExtra("alunoId",a.getId())));
            }
        }));
    }
}
