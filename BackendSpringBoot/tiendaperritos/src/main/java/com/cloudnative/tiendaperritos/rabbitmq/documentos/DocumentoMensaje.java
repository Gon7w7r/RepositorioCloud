package com.cloudnative.tiendaperritos.rabbitmq.documentos;

public record DocumentoMensaje(Long productoId, String nombre, String tipo) {}