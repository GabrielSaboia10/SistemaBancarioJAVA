package com.banco.exception;

/** Operação válida sintaticamente, mas proibida pelas regras do banco (HTTP 422). */
public class RegraNegocioException extends RuntimeException {
    public RegraNegocioException(String mensagem) {
        super(mensagem);
    }
}
