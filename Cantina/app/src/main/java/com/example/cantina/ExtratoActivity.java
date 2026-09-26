package com.example.cantina;

import android.os.Bundle;
import android.widget.*;
import com.google.firebase.firestore.DocumentSnapshot;
import java.util.*;

public class ExtratoActivity extends TelaBase {
    @Override protected void onCreate(Bundle state) { super.onCreate(state); acesso("ALUNO","RESPONSAVEL","CANTINA"); }
    @Override protected void carregar() {
        String escolhido=getIntent().getStringExtra("alunoId");
        String alunoId=escolhido==null ? auth.getUid() : escolhido;
        titulo("Extrato do aluno");
        LinearLayout resumo=grupo(conteudo), lista=grupo(conteudo);
        ouvir(db.collection("usuarios").document(alunoId).addSnapshotListener((aluno,e) -> {
            if(e!=null) { erro(e); return; } resumo.removeAllViews();
            texto(resumo,aluno.getString("nome")); texto(resumo,"Limite mensal: "+Dinheiro.formatar(numero(aluno,"limiteMensalCentavos")));
        }));
        ouvir(db.collection("pedidos").whereEqualTo("alunoId",alunoId).addSnapshotListener((snap,e) -> {
            if(e!=null) { erro(e); return; } lista.removeAllViews();
            List<DocumentSnapshot> docs=new ArrayList<>(snap.getDocuments());
            docs.sort((a,b)->String.valueOf(b.getString("data")).compareTo(String.valueOf(a.getString("data"))));
            long total=0;
            for(DocumentSnapshot d:docs) if(Repositorio.mes().equals(d.getString("mesReferencia")) && "CONTA".equals(d.getString("formaPagamento"))) total+=numero(d,"totalCentavos");
            texto(lista,"Compras na conta em "+Repositorio.mes()+": "+Dinheiro.formatar(total));
            if(docs.isEmpty()) texto(lista,"Nenhuma compra registrada.");
            for(DocumentSnapshot d:docs) {
                LinearLayout card=grupo(lista);
                texto(card,d.getString("data")+" • "+Dinheiro.formatar(numero(d,"totalCentavos"))+" • "+d.getString("status"));
                texto(card,"Retirada: "+d.getString("horarioRetirada"));
                texto(card,"Pagamento: "+d.getString("formaPagamento")+" • "+d.getString("pagamentoStatus"));
                TextView codigo=texto(card,"Código: "+d.getId()); codigo.setTextIsSelectable(true);
                Object itens=d.get("itens");
                if(itens instanceof List) for(Object obj:(List<?>)itens) if(obj instanceof Map) {
                    Map<?,?> item=(Map<?,?>)obj;
                    texto(card,item.get("quantidade")+" × "+item.get("nome"));
                }
            }
        }));
    }
}
