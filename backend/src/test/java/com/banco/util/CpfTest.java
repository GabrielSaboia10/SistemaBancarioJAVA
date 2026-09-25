package com.banco.util;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CpfTest {

    @Test
    void validaDigitosVerificadores() {
        assertThat(Cpf.isValido("529.982.247-25")).isTrue();
        assertThat(Cpf.isValido("52998224725")).isTrue();
        assertThat(Cpf.isValido("529.982.247-24")).isFalse();
        assertThat(Cpf.isValido("111.111.111-11")).isFalse();
        assertThat(Cpf.isValido("123")).isFalse();
    }

    @Test
    void formataComOuSemMascara() {
        assertThat(Cpf.formatar("52998224725")).isEqualTo("529.982.247-25");
        assertThat(Cpf.formatar("529.982.247-25")).isEqualTo("529.982.247-25");
        assertThat(Cpf.formatar("5299822472")).isNull();
    }
}
