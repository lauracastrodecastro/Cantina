package com.example.cantina;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Locale;

public final class Dinheiro {
    private Dinheiro() {}
    public static long centavos(String texto) {
        String valor = texto.trim();
        if (!valor.matches("[0-9]{1,7}(\\.[0-9]{1,2})?")) {
            throw new IllegalArgumentException("Use ponto e até duas casas decimais. Exemplo: 20.55");
        }
        return new BigDecimal(valor).movePointRight(2).setScale(0, RoundingMode.UNNECESSARY).longValueExact();
    }
    public static String numero(long centavos) {
        return String.format(Locale.US, "%.2f", BigDecimal.valueOf(centavos, 2));
    }
    public static String formatar(long centavos) {
        return "R$" + numero(centavos);
    }
}
