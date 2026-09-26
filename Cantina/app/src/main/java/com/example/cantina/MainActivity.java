package com.example.cantina;

import android.os.Bundle;
import android.text.InputType;
import android.widget.*;

public class MainActivity extends TelaBase {
    @Override protected void onCreate(Bundle state) {
        super.onCreate(state);
        titulo("Bem-vindo à Cantina");
        EditText email = campo(conteudo,"E-mail", InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        EditText senha = campo(conteudo,"Senha", InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        Button entrar = botao(conteudo,"Entrar", () -> {});
        entrar.setOnClickListener(v -> {
            String e = email.getText().toString().trim();
            String s = senha.getText().toString();
            if (e.isEmpty() || s.isEmpty()) { aviso("Informe e-mail e senha."); return; }
            entrar.setEnabled(false);
            auth.signInWithEmailAndPassword(e,s).addOnSuccessListener(result -> direcionar())
                .addOnFailureListener(ex -> { entrar.setEnabled(true); erro(ex); });
        });
        botao(conteudo,"Criar conta", () -> abrir(Cadastro.class));
        if (auth.getCurrentUser() != null) direcionar();
    }
    private void direcionar() {
        db.collection("usuarios").document(auth.getUid()).get().addOnSuccessListener(d -> {
            String tipo = d.getString("tipo");
            if ("CANTINA".equals(tipo)) abrir(PainelCantina.class);
            else if ("RESPONSAVEL".equals(tipo)) abrir(ResponsavelActivity.class);
            else if ("ALUNO".equals(tipo)) abrir(TelaPrincipal.class);
            else { aviso("Cadastro da cantina aguardando liberação pelo administrador."); auth.signOut(); recreate(); return; }
            finish();
        }).addOnFailureListener(e -> { erro(e); auth.signOut(); recreate(); });
    }
}
