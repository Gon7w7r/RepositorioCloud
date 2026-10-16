package com.cloudnative.tiendaperritos.rabbitmq.eventos;

import com.cloudnative.tiendaperritos.entity.Producto;
import com.cloudnative.tiendaperritos.rabbitmq.notificaciones.NotificacionMensaje;
import com.cloudnative.tiendaperritos.rabbitmq.notificaciones.NotificacionProductor;
import com.cloudnative.tiendaperritos.rabbitmq.tickets.TicketMensaje;
import com.cloudnative.tiendaperritos.rabbitmq.tickets.TicketProductor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class ProductoEventosPublisher {

    private static final Logger log = LoggerFactory.getLogger(ProductoEventosPublisher.class);

    private final NotificacionProductor notificacionProductor;
    private final TicketProductor ticketProductor;

    public ProductoEventosPublisher(NotificacionProductor notificacionProductor,
                                    TicketProductor ticketProductor) {
        this.notificacionProductor = notificacionProductor;
        this.ticketProductor = ticketProductor;
    }

    public void productoCreado(Producto p, String usuario) {
        notificar(usuario, "Producto creado", "Se creo el producto '" + p.getNombre() + "' (id " + p.getId() + ")");
        generarTicket(p);
    }

    public void productoActualizado(Producto p, String usuario) {
        notificar(usuario, "Producto actualizado", "Se actualizo el producto '" + p.getNombre() + "' (id " + p.getId() + ")");
    }

    public void productoEliminado(Long id, String usuario) {
        notificar(usuario, "Producto eliminado", "Se elimino el producto con id " + id);
    }

    private void notificar(String usuario, String asunto, String contenido) {
        try {
            notificacionProductor.enviar(new NotificacionMensaje(usuario, asunto, contenido));
        } catch (Exception e) {
            log.error("No se pudo publicar la notificacion '{}': {}", asunto, e.getMessage());
        }
    }

    private void generarTicket(Producto p) {
        try {
            double total = p.getPrecio().doubleValue() * p.getStock();
            ticketProductor.enviar(new TicketMensaje(p.getId(), p.getNombre(), total));
        } catch (Exception e) {
            log.error("No se pudo publicar el ticket del producto {}: {}", p.getId(), e.getMessage());
        }
    }
}