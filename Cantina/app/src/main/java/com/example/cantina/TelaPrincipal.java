package com.example.cantina;

import android.os.Bundle;
import android.widget.*;

public class TelaPrincipal extends TelaBase {
    @Override protected void onCreate(Bundle state) { super.onCreate(state); acesso("ALUNO"); }
    @Override protected void carregar() {
        titulo("Olá, " + perfil.getString("nome"));
        texto(conteudo,"O limite de compras na conta é definido pelo seu responsável.");
        TextView limite=texto(conteudo,"");
        TextView disponivel=texto(conteudo,"");
        long[] valores={0,0};
        ouvir(db.collection("usuarios").document(auth.getUid()).addSnapshotListener((d,e) -> {
            if(e!=null) { erro(e); return; }
            if(d!=null) { valores[0]=numero(d,"limiteMensalCentavos"); limite.setText("Limite mensal: " + Dinheiro.formatar(valores[0])); disponivel.setText("Disponível: "+Dinheiro.formatar(Math.max(0,valores[0]-valores[1]))); }
        }));
        TextView consumo=texto(conteudo,"");
        ouvir(db.collection("consumos").document(auth.getUid()+"_"+Repositorio.mes()).addSnapshotListener((d,e) -> {
            if(e!=null) { erro(e); return; }
            if(d!=null) { valores[1]=numero(d,"totalCentavos"); consumo.setText("Consumo deste mês: " + Dinheiro.formatar(valores[1])); disponivel.setText("Disponível: "+Dinheiro.formatar(Math.max(0,valores[0]-valores[1]))); }
        }));
        botao(conteudo,"Cardápio de hoje / fazer pedido", () -> abrir(PedidoActivity.class));
        botao(conteudo,"Extrato e código de retirada", () -> abrir(ExtratoActivity.class));
        botao(conteudo,"Sair",this::sair);
    }
}
