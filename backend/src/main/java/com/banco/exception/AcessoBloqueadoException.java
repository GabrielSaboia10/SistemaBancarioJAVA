package com.banco.exception;

public class AcessoBloqueadoException extends RuntimeException {
    public AcessoBloqueadoException() {
        super("Seu acesso está bloqueado. Procure sua agência.");
    }
}
