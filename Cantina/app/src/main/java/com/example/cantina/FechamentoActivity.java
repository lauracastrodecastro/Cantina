package com.example.cantina;

import android.os.Bundle;
import android.text.InputType;
import android.widget.*;
import com.google.firebase.firestore.*;
import java.time.YearMonth;
import java.util.*;

public class FechamentoActivity extends TelaBase {
    @Override protected void onCreate(Bundle state) { super.onCreate(state); acesso("CANTINA"); }
    @Override protected void carregar() {
        titulo("Fechamento mensal");
        EditText mes=campo(conteudo,"Mês (AAAA-MM)",InputType.TYPE_CLASS_DATETIME); mes.setText(YearMonth.parse(Repositorio.mes()).minusMonths(1).toString());
        LinearLayout lista=grupo(conteudo);
        botao(conteudo,"Consultar mês", () -> {
            String referencia=mes.getText().toString().trim();
            try { YearMonth.parse(referencia); } catch(Exception e) { aviso("Use AAAA-MM."); return; }
            db.collection("consumos").whereEqualTo("mesReferencia",referencia).get().addOnSuccessListener(snap -> {
                lista.removeAllViews(); if(snap.isEmpty()) texto(lista,"Nenhum consumo no mês.");
                for(DocumentSnapshot consumo:snap.getDocuments()) {
                    LinearLayout card=grupo(lista); TextView nome=texto(card,"Aluno: "+consumo.getString("alunoId"));
                    db.collection("usuarios").document(consumo.getString("alunoId")).get().addOnSuccessListener(a->nome.setText(a.getString("nome"))).addOnFailureListener(this::erro);
                    texto(card,Dinheiro.formatar(numero(consumo,"totalCentavos"))+" • "+consumo.getString("status"));
                    if(referencia.compareTo(Repositorio.mes())>=0) { texto(card,"Disponível para fechamento após o fim do mês."); continue; }
                    DocumentReference fechamento=db.collection("fechamentos").document(consumo.getId());
                    if(!"FECHADO".equals(consumo.getString("status"))) botao(card,"Fechar mês", () -> db.runTransaction(tx -> {
                        DocumentSnapshot atual=tx.get(consumo.getReference());
                        if("FECHADO".equals(atual.getString("status"))) throw new IllegalStateException("Mês já fechado.");
                        Map<String,Object> dados=new HashMap<>(); dados.put("alunoId",atual.getString("alunoId")); dados.put("mesReferencia",referencia);
                        dados.put("totalCentavos",numero(atual,"totalCentavos")); dados.put("status","ABERTO");
                        tx.set(fechamento,dados); tx.update(consumo.getReference(),"status","FECHADO"); return null;
                    }).addOnSuccessListener(ok->{ aviso("Mês fechado."); recreate(); }).addOnFailureListener(this::erro));
                    else fechamento.get().addOnSuccessListener(f -> {
                        texto(card,"Pagamento: "+f.getString("status"));
                        if("ABERTO".equals(f.getString("status"))) botao(card,"Registrar pagamento recebido", () -> new androidx.appcompat.app.AlertDialog.Builder(this)
                            .setTitle("Confirmar recebimento").setMessage("O valor integral já foi recebido?")
                            .setNegativeButton("Cancelar",null).setPositiveButton("Sim",(d,w)->fechamento.update("status","PAGO")
                                .addOnSuccessListener(ok->{ aviso("Pagamento registrado."); recreate(); }).addOnFailureListener(this::erro)).show());
                    }).addOnFailureListener(this::erro);
                }
            }).addOnFailureListener(this::erro);
        });
    }
}
