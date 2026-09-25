package com.banco.exception;

/** O recurso já existe ou está em uso por outro registro (HTTP 409). */
public class ConflitoException extends RuntimeException {
    public ConflitoException(String mensagem) {
        super(mensagem);
    }
}
