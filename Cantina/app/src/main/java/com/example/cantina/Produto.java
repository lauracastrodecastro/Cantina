package com.example.cantina;

import com.google.firebase.firestore.DocumentSnapshot;

public class Produto {
    public String id, nome, descricao, fotoBase64, dataCardapio;
    public long precoCentavos, estoque;
    public boolean disponivel;
    public Produto() {}
    public static Produto de(DocumentSnapshot doc) {
        Produto p=doc.toObject(Produto.class);
        if(p==null) p=new Produto();
        p.id=doc.getId(); return p;
    }
}
