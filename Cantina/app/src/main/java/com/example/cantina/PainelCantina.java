package com.example.cantina;

import android.os.Bundle;
import android.content.Intent;

public class PainelCantina extends TelaBase {
    @Override protected void onCreate(Bundle state) { super.onCreate(state); acesso("CANTINA"); }
    @Override protected void carregar() {
        titulo("Painel da cantina");
        botao(conteudo,"Cadastrar e editar produtos", () -> abrir(ProdutosActivity.class));
        botao(conteudo,"Pedidos e retirada", () -> abrir(PedidoCantina.class));
        botao(conteudo,"Venda no balcão", () -> startActivity(new Intent(this,PedidoActivity.class).putExtra("balcao",true)));
        botao(conteudo,"Alunos e responsáveis", () -> abrir(VinculosActivity.class));
        botao(conteudo,"Fechamento mensal", () -> abrir(FechamentoActivity.class));
        botao(conteudo,"Sair",this::sair);
    }
}
