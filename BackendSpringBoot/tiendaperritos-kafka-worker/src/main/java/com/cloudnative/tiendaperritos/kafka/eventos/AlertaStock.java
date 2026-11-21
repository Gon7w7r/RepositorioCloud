package com.cloudnative.tiendaperritos.kafka.eventos;

public record AlertaStock(
        String eventoId,
        Long productoId,
        String nombre,
        Integer stockDisponible,
        String severidad,
        String fechaEvento) {
}
