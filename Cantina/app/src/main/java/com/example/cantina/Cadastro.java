package com.example.cantina;

import android.os.Bundle;
import android.text.InputType;
import android.widget.*;
import java.util.*;

public class Cadastro extends TelaBase {
    @Override protected void onCreate(Bundle state) {
        super.onCreate(state);
        titulo("Criar conta");
        EditText nome = campo(conteudo,"Nome completo",InputType.TYPE_CLASS_TEXT);
        EditText email = campo(conteudo,"E-mail",InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        EditText senha = campo(conteudo,"Senha",InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        EditText repetir = campo(conteudo,"Repetir senha",InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        EditText turma = campo(conteudo,"Turma (para alunos)",InputType.TYPE_CLASS_TEXT);
        Spinner tipo = opcoes(conteudo,Arrays.asList("Aluno","Responsável","Cantina"));
        texto(conteudo,"A cantina confirma o vínculo entre responsável e aluno. Contas de cantina precisam de liberação administrativa.");
        Button salvar = botao(conteudo,"Cadastrar", () -> {});
        salvar.setOnClickListener(v -> {
            String n=nome.getText().toString().trim(), e=email.getText().toString().trim(), s=senha.getText().toString();
            String t=turma.getText().toString().trim();
            if (n.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(e).matches() || s.length()<6 || !s.equals(repetir.getText().toString()) || (tipo.getSelectedItemPosition()==0 && t.isEmpty())) {
                aviso("Confira nome, e-mail, turma e senhas iguais com pelo menos 6 caracteres."); return;
            }
            String papel = new String[]{"ALUNO","RESPONSAVEL","CANTINA_PENDENTE"}[tipo.getSelectedItemPosition()];
            salvar.setEnabled(false);
            auth.createUserWithEmailAndPassword(e,s).addOnSuccessListener(result -> {
                Map<String,Object> dados = new HashMap<>();
                dados.put("nome",n); dados.put("email",e); dados.put("tipo",papel); dados.put("turma",t);
                dados.put("responsavelId",""); dados.put("limiteMensalCentavos",0L);
                db.collection("usuarios").document(result.getUser().getUid()).set(dados)
                    .addOnSuccessListener(ok -> { auth.signOut(); aviso("Cadastro realizado. Faça login."); finish(); })
                    .addOnFailureListener(ex -> result.getUser().delete().addOnCompleteListener(task -> {
                        auth.signOut(); salvar.setEnabled(true); erro(ex);
                    }));
            }).addOnFailureListener(ex -> { salvar.setEnabled(true); erro(ex); });
        });
    }
}
