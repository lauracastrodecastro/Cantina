package com.example.cantina;

import android.os.Bundle;
import android.text.InputType;
import android.widget.*;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import com.google.firebase.firestore.DocumentReference;
import java.time.LocalDate;
import java.util.*;

public class EditarProdutoActivity extends TelaBase {
    private String fotoBase64="";
    private ImageView imagem;
    private Button salvar;
    private boolean processandoFoto;
    private final ActivityResultLauncher<String> selecionar=registerForActivityResult(new ActivityResultContracts.GetContent(),uri -> {
        if(uri==null) return;
        processandoFoto=true; if(salvar!=null) salvar.setEnabled(false);
        new Thread(() -> {
            try {
                String foto=FotoProduto.ler(getContentResolver(),uri);
                runOnUiThread(() -> { fotoBase64=foto; FotoProduto.mostrar(imagem,foto); processandoFoto=false; salvar.setEnabled(true); });
            } catch(Exception e) { runOnUiThread(() -> { processandoFoto=false; salvar.setEnabled(true); erro(e); }); }
        }).start();
    });
    @Override protected void onCreate(Bundle state) {
        super.onCreate(state);
        if(state!=null) fotoBase64=state.getString("foto","");
        acesso("CANTINA");
    }
    @Override protected void onSaveInstanceState(Bundle state) { state.putString("foto",fotoBase64); super.onSaveInstanceState(state); }
    @Override protected void carregar() {
        String id=getIntent().getStringExtra("produtoId");
        DocumentReference ref=id==null ? db.collection("produtos").document() : db.collection("produtos").document(id);
        titulo(id==null ? "Cadastrar produto" : "Editar produto");
        EditText nome=campo(conteudo,"Nome",InputType.TYPE_CLASS_TEXT);
        EditText descricao=campo(conteudo,"Descrição",InputType.TYPE_CLASS_TEXT);
        EditText preco=moeda(conteudo,"Preço (R$)");
        EditText estoque=campo(conteudo,"Unidades em estoque",InputType.TYPE_CLASS_NUMBER);
        EditText data=campo(conteudo,"Data do cardápio (AAAA-MM-DD)",InputType.TYPE_CLASS_DATETIME);
        data.setText(Repositorio.hoje());
        CheckBox disponivel=new CheckBox(this); disponivel.setText("Disponível no cardápio"); disponivel.setChecked(true); conteudo.addView(disponivel);
        imagem=new ImageView(this); imagem.setAdjustViewBounds(true); imagem.setMaxHeight(400); conteudo.addView(imagem); FotoProduto.mostrar(imagem,fotoBase64);
        botao(conteudo,"Selecionar foto", () -> selecionar.launch("image/*"));
        salvar=botao(conteudo,"Salvar produto", () -> {});
        final long[] estoqueOriginal={0};
        if(id!=null) {
            salvar.setEnabled(false);
            ref.get().addOnSuccessListener(doc -> {
                if(!doc.exists()) { aviso("Produto não encontrado."); finish(); return; }
                Produto p=Produto.de(doc); nome.setText(p.nome); descricao.setText(p.descricao); preco.setText(Dinheiro.numero(p.precoCentavos));
                estoque.setText(String.valueOf(p.estoque)); estoqueOriginal[0]=p.estoque;
                data.setText(p.dataCardapio); disponivel.setChecked(p.disponivel);
                if(fotoBase64.isEmpty()) fotoBase64=p.fotoBase64==null ? "" : p.fotoBase64;
                FotoProduto.mostrar(imagem,fotoBase64); salvar.setEnabled(!processandoFoto);
            }).addOnFailureListener(this::erro);
        }
        salvar.setOnClickListener(v -> {
            try {
                String n=nome.getText().toString().trim(); long valor=Dinheiro.centavos(preco.getText().toString());
                long unidades=Long.parseLong(estoque.getText().toString());
                String dia=LocalDate.parse(data.getText().toString().trim()).toString();
                if(n.isEmpty() || valor<=0 || unidades<0 || unidades>1000000 || fotoBase64.isEmpty() || processandoFoto) throw new IllegalArgumentException("Informe nome, preço positivo, foto e estoque de 0 a 1000000.");
                Map<String,Object> dados=new HashMap<>(); dados.put("nome",n); dados.put("descricao",descricao.getText().toString().trim());
                dados.put("precoCentavos",valor); dados.put("estoque",unidades); dados.put("fotoBase64",fotoBase64);
                dados.put("dataCardapio",dia); dados.put("disponivel",disponivel.isChecked());
                salvar.setEnabled(false);
                db.runTransaction(tx -> {
                    com.google.firebase.firestore.DocumentSnapshot atual=tx.get(ref);
                    if(id!=null && (!atual.exists() || numero(atual,"estoque")!=estoqueOriginal[0])) throw new IllegalStateException("O estoque mudou durante a edição. Volte e abra o produto novamente.");
                    tx.set(ref,dados,com.google.firebase.firestore.SetOptions.merge()); return null;
                }).addOnSuccessListener(ok -> { aviso("Produto salvo."); finish(); })
                  .addOnFailureListener(e -> { salvar.setEnabled(true); erro(e); });
            } catch(Exception e) { erro(e); }
        });
    }
}
