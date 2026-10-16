package com.cloudnative.tiendaperritos.rabbitmq.notificaciones;

public class ErrorRecuperableException extends RuntimeException {
    public ErrorRecuperableException(String message) {
        super(message);
    }
}