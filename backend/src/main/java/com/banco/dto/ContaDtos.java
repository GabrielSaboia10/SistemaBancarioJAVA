package com.banco.dto;

import com.banco.model.ContaBancaria;
import com.banco.model.Movimentacao;
import com.banco.model.TipoMovimentacao;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.Instant;

public final class ContaDtos {

    private ContaDtos() {}

    public record CriarContaRequest(
            @NotNull(message = "Informe o número da conta") @Min(value = 1000, message = "O número deve ser entre 1000 e 99999") @Max(value = 99999, message = "O número deve ser entre 1000 e 99999") Integer numero,
            @NotNull(message = "Informe o limite") @DecimalMin(value = "0", message = "O limite não pode ser negativo") @DecimalMax(value = "30000", message = "O limite máximo é R$ 30.000") @Digits(integer = 13, fraction = 2, message = "Use no máximo 2 casas decimais") BigDecimal limiteChequeEspecial,
            @DecimalMin(value = "0", message = "O saldo inicial não pode ser negativo") @Digits(integer = 13, fraction = 2, message = "Use no máximo 2 casas decimais") BigDecimal saldoInicial,
            @NotNull(message = "Informe o correntista") Long pessoaId,
            @NotNull(message = "Informe a agência") Long agenciaId
    ) {}

    public record AtualizarContaRequest(
            @NotNull(message = "Informe o limite") @DecimalMin(value = "0", message = "O limite não pode ser negativo") @DecimalMax(value = "30000", message = "O limite máximo é R$ 30.000") @Digits(integer = 13, fraction = 2, message = "Use no máximo 2 casas decimais") BigDecimal limiteChequeEspecial,
            @NotNull(message = "Informe a agência") Long agenciaId
    ) {}

    public record OperacaoRequest(
            @NotNull(message = "Informe o valor") @DecimalMin(value = "0.01", message = "O valor deve ser maior que zero") @DecimalMax(value = "1000000", message = "Valor acima do limite por operação") @Digits(integer = 13, fraction = 2, message = "Use no máximo 2 casas decimais") BigDecimal valor
    ) {}

    public record TransferenciaRequest(
            @NotNull(message = "Informe a conta de destino") Integer numeroContaDestino,
            @NotNull(message = "Informe o valor") @DecimalMin(value = "0.01", message = "O valor deve ser maior que zero") @DecimalMax(value = "1000000", message = "Valor acima do limite por operação") @Digits(integer = 13, fraction = 2, message = "Use no máximo 2 casas decimais") BigDecimal valor
    ) {}

    public record CorrentistaResumo(Long id, String nome, String cpf) {}

    public record AgenciaResumo(Long id, int numero, String cidade) {}

    public record ContaResponse(
            Long id,
            int numero,
            BigDecimal saldo,
            BigDecimal limiteChequeEspecial,
            BigDecimal saldoDisponivel,
            CorrentistaResumo correntista,
            AgenciaResumo agencia
    ) {
        public static ContaResponse de(ContaBancaria c) {
            return new ContaResponse(
                    c.getId(), c.getNumero(), c.getSaldo(), c.getLimiteChequeEspecial(), c.getSaldoDisponivel(),
                    new CorrentistaResumo(c.getCorrentista().getId(), c.getCorrentista().getNome(), c.getCorrentista().getCpf()),
                    new AgenciaResumo(c.getAgencia().getId(), c.getAgencia().getNumero(), c.getAgencia().getCidade()));
        }
    }

    public record MovimentacaoResponse(
            Long id,
            TipoMovimentacao tipo,
            BigDecimal valor,
            BigDecimal saldoApos,
            Integer numeroContaContraparte,
            Instant dataHora
    ) {
        public static MovimentacaoResponse de(Movimentacao m) {
            return new MovimentacaoResponse(m.getId(), m.getTipo(), m.getValor(), m.getSaldoApos(),
                    m.getNumeroContaContraparte(), m.getDataHora());
        }
    }

    /** Para CLIENTE, totalClientes e totalAgencias vêm nulos (só as contas dele contam). */
    public record StatsResponse(Long totalClientes, Long totalAgencias, long totalContas, BigDecimal saldoTotal) {}
}
