package com.banco.exception;

public class MuitasTentativasException extends RuntimeException {
    public MuitasTentativasException(String mensagem) {
        super(mensagem);
    }
}
