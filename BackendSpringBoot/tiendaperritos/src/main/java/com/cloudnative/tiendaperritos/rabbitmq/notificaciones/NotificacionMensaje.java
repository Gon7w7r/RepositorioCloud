package com.cloudnative.tiendaperritos.rabbitmq.notificaciones;

public record NotificacionMensaje(String destinatario, String asunto, String contenido) {}