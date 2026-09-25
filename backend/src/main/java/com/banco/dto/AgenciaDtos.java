package com.banco.dto;

import com.banco.model.AgenciaBancaria;
import jakarta.validation.constraints.*;

public final class AgenciaDtos {

    private AgenciaDtos() {}

    public record AgenciaRequest(
            @NotNull(message = "Informe o número") @Min(value = 1, message = "Número deve ser entre 1 e 9999") @Max(value = 9999, message = "Número deve ser entre 1 e 9999") Integer numero,
            @NotBlank(message = "Informe o endereço") @Size(max = 100, message = "O endereço deve ter até 100 caracteres") String endereco,
            @NotBlank(message = "Informe a cidade") @Size(max = 25, message = "A cidade deve ter até 25 caracteres") String cidade
    ) {}

    public record AgenciaResponse(Long id, int numero, String endereco, String cidade) {
        public static AgenciaResponse de(AgenciaBancaria a) {
            return new AgenciaResponse(a.getId(), a.getNumero(), a.getEndereco(), a.getCidade());
        }
    }
}
