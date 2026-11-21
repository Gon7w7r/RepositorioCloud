package com.cloudnative.tiendaperritos.rabbitmq.eventos;

import com.cloudnative.tiendaperritos.entity.Producto;
import com.cloudnative.tiendaperritos.kafka.eventos.ProductoEvento;
import com.cloudnative.tiendaperritos.kafka.productor.ProductoKafkaPublisher;
import com.cloudnative.tiendaperritos.rabbitmq.notificaciones.NotificacionMensaje;
import com.cloudnative.tiendaperritos.rabbitmq.notificaciones.NotificacionProductor;
import com.cloudnative.tiendaperritos.rabbitmq.tickets.TicketMensaje;
import com.cloudnative.tiendaperritos.rabbitmq.tickets.TicketProductor;
import java.time.Instant;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class ProductoEventosPublisher {

    private static final Logger log = LoggerFactory.getLogger(ProductoEventosPublisher.class);

    private final NotificacionProductor notificacionProductor;
    private final TicketProductor ticketProductor;
    private final ProductoKafkaPublisher kafkaProductoPublisher;

    public ProductoEventosPublisher(NotificacionProductor notificacionProductor,
                                    TicketProductor ticketProductor,
                                    ProductoKafkaPublisher kafkaProductoPublisher) {
        this.notificacionProductor = notificacionProductor;
        this.ticketProductor = ticketProductor;
        this.kafkaProductoPublisher = kafkaProductoPublisher;
    }

    public void productoCreado(Producto producto, String usuario) {
        publicarEvento("PRODUCTO_CREADO", producto, usuario);
        notificar(usuario, "Producto creado",
                "Se creo el producto '" + producto.getNombre() + "' (id " + producto.getId() + ")");
        generarTicket(producto);
    }

    public void productoActualizado(Producto producto, String usuario) {
        publicarEvento("PRODUCTO_ACTUALIZADO", producto, usuario);
        notificar(usuario, "Producto actualizado",
                "Se actualizo el producto '" + producto.getNombre() + "' (id " + producto.getId() + ")");
    }

    public void productoEliminado(Long id, String usuario) {
        ProductoEvento evento = new ProductoEvento(
                UUID.randomUUID().toString(),
                "PRODUCTO_ELIMINADO",
                id,
                null,
                null,
                null,
                Instant.now().toString(),
                usuario);
        publicarEvento(evento);
        notificar(usuario, "Producto eliminado", "Se elimino el producto con id " + id);
    }

    private void publicarEvento(String tipo, Producto producto, String usuario) {
        publicarEvento(new ProductoEvento(
                UUID.randomUUID().toString(),
                tipo,
                producto.getId(),
                producto.getNombre(),
                producto.getPrecio(),
                producto.getStock(),
                Instant.now().toString(),
                usuario));
    }

    private void publicarEvento(ProductoEvento evento) {
        try {
            kafkaProductoPublisher.publicar(evento);
        } catch (Exception e) {
            log.error("No se pudo preparar el evento Kafka {}: {}", evento.eventoId(), e.getMessage(), e);
        }
    }

    private void notificar(String usuario, String asunto, String contenido) {
        try {
            notificacionProductor.enviar(new NotificacionMensaje(usuario, asunto, contenido));
        } catch (Exception e) {
            log.error("No se pudo publicar la notificacion '{}': {}", asunto, e.getMessage());
        }
    }

    private void generarTicket(Producto producto) {
        try {
            double total = producto.getPrecio().doubleValue() * producto.getStock();
            ticketProductor.enviar(new TicketMensaje(producto.getId(), producto.getNombre(), total));
        } catch (Exception e) {
            log.error("No se pudo publicar el ticket del producto {}: {}", producto.getId(), e.getMessage());
        }
    }
}
