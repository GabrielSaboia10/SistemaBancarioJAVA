package com.banco.model;

import com.banco.exception.RegraNegocioException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ContaBancariaTest {

    private ContaBancaria conta(String saldo, String limite) {
        ContaBancaria c = new ContaBancaria(1000, new BigDecimal(limite), new Pessoa("529.982.247-25", "Maria", 30),
                new AgenciaBancaria(1, "Rua A", "Rio"));
        if (new BigDecimal(saldo).signum() > 0) c.depositar(new BigDecimal(saldo));
        return c;
    }

    @Test
    void depositoSomaAoSaldo() {
        ContaBancaria c = conta("100.00", "0");
        c.depositar(new BigDecimal("0.10"));
        c.depositar(new BigDecimal("0.20"));
        assertThat(c.getSaldo()).isEqualByComparingTo("100.30"); // sem erro de ponto flutuante
    }

    @Test
    void saquePodeUsarOChequeEspecial() {
        ContaBancaria c = conta("100.00", "50.00");
        c.sacar(new BigDecimal("150.00"));
        assertThat(c.getSaldo()).isEqualByComparingTo("-50.00");
        assertThat(c.getSaldoDisponivel()).isEqualByComparingTo("0");
    }

    @Test
    void saqueAlemDoLimiteEhRecusado() {
        ContaBancaria c = conta("100.00", "50.00");
        assertThatThrownBy(() -> c.sacar(new BigDecimal("150.01")))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessage("Saldo insuficiente. Disponível: R$ 150,00");
        assertThat(c.getSaldo()).isEqualByComparingTo("100.00");
    }

    @Test
    void valoresZeroOuNegativosSaoRecusados() {
        ContaBancaria c = conta("100.00", "0");
        assertThatThrownBy(() -> c.depositar(BigDecimal.ZERO)).isInstanceOf(RegraNegocioException.class);
        assertThatThrownBy(() -> c.sacar(new BigDecimal("-1"))).isInstanceOf(RegraNegocioException.class);
    }

    @Test
    void naoPodeReduzirLimiteSeOSaldoJaUsaOChequeEspecial() {
        ContaBancaria c = conta("0", "100.00");
        c.sacar(new BigDecimal("80.00"));
        assertThatThrownBy(() -> c.alterarLimite(new BigDecimal("50.00"))).isInstanceOf(RegraNegocioException.class);
        c.alterarLimite(new BigDecimal("80.00"));
        assertThat(c.getLimiteChequeEspecial()).isEqualByComparingTo("80.00");
    }
}
