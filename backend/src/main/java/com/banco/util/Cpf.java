package com.banco.util;

/** Utilitários de CPF. O formato canônico salvo no banco é 000.000.000-00. */
public final class Cpf {

    private Cpf() {}

    public static String somenteDigitos(String cpf) {
        return cpf == null ? "" : cpf.replaceAll("\\D", "");
    }

    /** Aceita CPF com ou sem máscara. Devolve null se não tiver exatamente 11 dígitos. */
    public static String formatar(String cpf) {
        String d = somenteDigitos(cpf);
        if (d.length() != 11) return null;
        return d.substring(0, 3) + "." + d.substring(3, 6) + "." + d.substring(6, 9) + "-" + d.substring(9);
    }

    /** Valida os dois dígitos verificadores e rejeita sequências repetidas (111.111.111-11). */
    public static boolean isValido(String cpf) {
        String d = somenteDigitos(cpf);
        if (d.length() != 11 || d.chars().distinct().count() == 1) return false;
        return digitoVerificador(d, 9) == d.charAt(9) - '0'
                && digitoVerificador(d, 10) == d.charAt(10) - '0';
    }

    private static int digitoVerificador(String digitos, int quantidade) {
        int soma = 0;
        for (int i = 0; i < quantidade; i++) {
            soma += (digitos.charAt(i) - '0') * (quantidade + 1 - i);
        }
        int resto = (soma * 10) % 11;
        return resto == 10 ? 0 : resto;
    }
}
