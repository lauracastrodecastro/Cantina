package com.example.cantina;

import org.junit.Test;
import java.util.Locale;
import static org.junit.Assert.*;

public class DinheiroTest {
    @Test public void pontoIndependeDoIdiomaDoAparelho() {
        Locale anterior=Locale.getDefault();
        try {
            for(Locale idioma:new Locale[]{Locale.US,Locale.forLanguageTag("pt-BR")}) {
                Locale.setDefault(idioma);
                assertEquals(2055L,Dinheiro.centavos("20.55"));
                assertEquals("R$20.55",Dinheiro.formatar(2055));
                assertEquals("R$0.30",Dinheiro.formatar(Dinheiro.centavos("0.10")+Dinheiro.centavos("0.20")));
            }
        } finally { Locale.setDefault(anterior); }
    }
    @Test public void rejeitaEntradasInvalidas() {
        for(String valor:new String[]{"20,55","-1","1.005","NaN","Infinity","1e3","","10000000"}) {
            try { Dinheiro.centavos(valor); fail("Aceitou "+valor); }
            catch(IllegalArgumentException expected) {}
        }
    }
}
