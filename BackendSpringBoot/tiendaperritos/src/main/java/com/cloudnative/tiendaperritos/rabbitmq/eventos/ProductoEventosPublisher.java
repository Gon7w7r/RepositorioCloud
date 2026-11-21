package com.cloudnative.tiendaperritos.rabbitmq.eventos;

import com.cloudnative.tiendaperritos.entity.Producto;
import com.cloudnative.tiendaperritos.rabbitmq.documentos.DocumentoMensaje;
import com.cloudnative.tiendaperritos.rabbitmq.documentos.DocumentoProductor;
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
    private static final String FICHA_PRODUCTO = "FICHA_PRODUCTO";

    private final NotificacionProductor notificacionProductor;
    private final TicketProductor ticketProductor;
    private final DocumentoProductor documentoProductor;

    public ProductoEventosPublisher(NotificacionProductor notificacionProductor,
                                    TicketProductor ticketProductor,
                                    DocumentoProductor documentoProductor) {
        this.notificacionProductor = notificacionProductor;
        this.ticketProductor = ticketProductor;
        this.documentoProductor = documentoProductor;
    }

    public void productoCreado(Producto p, String usuario) {
        notificar("creado", usuario, "Producto creado",
                "Se creo el producto '" + p.getNombre() + "' (id " + p.getId() + ")");
        generarTicket(p);
        generarDocumento(p);
    }

    public void productoActualizado(Producto p, String usuario) {
        notificar("actualizado", usuario, "Producto actualizado",
                "Se actualizo el producto '" + p.getNombre() + "' (id " + p.getId() + ")");
    }

    public void productoEliminado(Long id, String usuario) {
        notificar("eliminado", usuario, "Producto eliminado",
                "Se elimino el producto con id " + id);
    }

    private void notificar(String evento, String usuario, String asunto, String contenido) {
        try {
            notificacionProductor.enviar(evento, new NotificacionMensaje(usuario, asunto, contenido));
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

    private void generarDocumento(Producto p) {
        try {
            documentoProductor.enviar(new DocumentoMensaje(p.getId(), p.getNombre(), FICHA_PRODUCTO));
        } catch (Exception e) {
            log.error("No se pudo publicar el documento del producto {}: {}", p.getId(), e.getMessage());
        }
    }
}