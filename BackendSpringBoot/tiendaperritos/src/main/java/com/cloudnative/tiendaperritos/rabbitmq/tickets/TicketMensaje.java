package com.cloudnative.tiendaperritos.rabbitmq.tickets;

public record TicketMensaje(Long pedidoId, String cliente, Double total) {}