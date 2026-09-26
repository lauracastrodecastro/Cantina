package com.example.cantina;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.text.method.DigitsKeyListener;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.graphics.Insets;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.*;
import java.util.*;

public abstract class TelaBase extends AppCompatActivity {
    protected final FirebaseAuth auth = FirebaseAuth.getInstance();
    protected final FirebaseFirestore db = FirebaseFirestore.getInstance();
    protected LinearLayout conteudo;
    protected DocumentSnapshot perfil;
    private final List<ListenerRegistration> listeners = new ArrayList<>();

    @Override protected void onCreate(Bundle state) {
        super.onCreate(state);
        setContentView(R.layout.activity_base);
        conteudo = findViewById(R.id.conteudo);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (view, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            view.setPadding(bars.left, bars.top, bars.right, bars.bottom);
            return insets;
        });
    }
    protected void acesso(String... tipos) {
        if (auth.getCurrentUser() == null) { abrir(MainActivity.class); finish(); return; }
        db.collection("usuarios").document(auth.getUid()).get().addOnSuccessListener(doc -> {
            if (!doc.exists() || !Arrays.asList(tipos).contains(doc.getString("tipo"))) {
                aviso("Sua conta não tem acesso a esta tela."); finish(); return;
            }
            perfil = doc;
            carregar();
        }).addOnFailureListener(this::erro);
    }
    protected void carregar() {}
    protected void titulo(String texto) {
        TextView t = texto(conteudo, texto); t.setTextSize(24); t.setPadding(0, 24, 0, 20);
    }
    protected TextView texto(LinearLayout pai, String valor) {
        TextView t = new TextView(this); t.setText(valor); t.setTextSize(16);
        t.setTextColor(0xff282b2f); t.setPadding(0, 10, 0, 10); pai.addView(t); return t;
    }
    protected EditText campo(LinearLayout pai, String label, int tipo) {
        texto(pai, label);
        EditText e = new EditText(this); e.setInputType(tipo); e.setSingleLine(true);
        e.setTextSize(16); e.setHint(label); pai.addView(e); return e;
    }
    protected EditText moeda(LinearLayout pai, String label) {
        EditText e = campo(pai, label, InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        e.setKeyListener(DigitsKeyListener.getInstance("0123456789."));
        e.setHint("20.55"); return e;
    }
    protected Button botao(LinearLayout pai, String label, Runnable acao) {
        Button b = new Button(this); b.setText(label); b.setAllCaps(false);
        pai.addView(b); b.setOnClickListener(v -> acao.run()); return b;
    }
    protected Spinner opcoes(LinearLayout pai, List<String> nomes) {
        Spinner s = new Spinner(this);
        ArrayAdapter<String> a = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, nomes);
        a.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        s.setAdapter(a); pai.addView(s); return s;
    }
    protected LinearLayout grupo(LinearLayout pai) {
        LinearLayout l = new LinearLayout(this); l.setOrientation(LinearLayout.VERTICAL);
        l.setPadding(16,16,16,16); l.setBackgroundResource(R.drawable.bg_card);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1,-2); lp.setMargins(0,12,0,12);
        pai.addView(l,lp); return l;
    }
    protected void ouvir(ListenerRegistration l) { listeners.add(l); }
    protected void abrir(Class<?> tela) { startActivity(new Intent(this,tela)); }
    protected void aviso(String msg) { Toast.makeText(this,msg,Toast.LENGTH_LONG).show(); }
    protected void erro(Exception e) { aviso(e.getMessage() == null ? "Não foi possível concluir." : e.getMessage()); }
    protected static long numero(DocumentSnapshot d, String campo) { Long n = d.getLong(campo); return n == null ? 0 : n; }
    protected void sair() { auth.signOut(); startActivity(new Intent(this, MainActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK)); }
    @Override protected void onDestroy() { for (ListenerRegistration l : listeners) l.remove(); super.onDestroy(); }
}
