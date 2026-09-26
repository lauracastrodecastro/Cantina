package com.example.cantina;

import android.os.Bundle;
import android.content.Intent;
import android.widget.*;

public class ProdutosActivity extends TelaBase {
    @Override protected void onCreate(Bundle state) { super.onCreate(state); acesso("CANTINA"); }
    @Override protected void carregar() {
        titulo("Produtos da cantina");
        botao(conteudo,"Cadastrar produto", () -> abrir(EditarProdutoActivity.class));
        LinearLayout lista=grupo(conteudo);
        ouvir(db.collection("produtos").addSnapshotListener((snap,e) -> {
            if(e!=null) { erro(e); return; } lista.removeAllViews();
            if(snap.isEmpty()) texto(lista,"Cadastre o primeiro produto.");
            for(com.google.firebase.firestore.DocumentSnapshot doc:snap.getDocuments()) {
                Produto p=Produto.de(doc); LinearLayout card=grupo(lista);
                ImageView foto=new ImageView(this); foto.setAdjustViewBounds(true); foto.setMaxHeight(280); card.addView(foto); FotoProduto.mostrar(foto,p.fotoBase64);
                texto(card,p.nome + " • " + Dinheiro.formatar(p.precoCentavos));
                texto(card,"Estoque: " + p.estoque + " unidades • " + (p.disponivel ? "Disponível" : "Indisponível"));
                texto(card,"Cardápio: " + p.dataCardapio);
                botao(card,"Editar produto", () -> startActivity(new Intent(this,EditarProdutoActivity.class).putExtra("produtoId",p.id)));
            }
        }));
    }
}
