package com.cloudnative.tiendaperritos.kafka.eventos;

import java.math.BigDecimal;

public record ProductoEvento(
        String eventoId,
        String tipo,
        Long productoId,
        String nombre,
        BigDecimal precio,
        Integer stock,
        String fechaEvento,
        String usuario) {
}
