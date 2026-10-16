package com.cloudnative.tiendaperritos.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.cloudnative.tiendaperritos.entity.Producto;
import com.cloudnative.tiendaperritos.repository.ProductoRepository;
import com.cloudnative.tiendaperritos.rabbitmq.eventos.ProductoEventosPublisher;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.List;

@RestController
@RequestMapping("/api/data")
@CrossOrigin
public class ProductoController {

   private final ProductoRepository productoRepository;
   private final ProductoEventosPublisher eventos;

    public ProductoController(ProductoRepository productoRepository,
                          ProductoEventosPublisher eventos) {
    this.productoRepository = productoRepository;
    this.eventos = eventos;
}
    private String usuarioDe(Jwt jwt) {
    String usuario = jwt.getClaimAsString("unique_name");
    if (usuario == null) usuario = jwt.getClaimAsString("preferred_username");
    return usuario == null ? "desconocido" : usuario;
}
    @GetMapping
    public List<Producto> obtenerProductos() {
        return productoRepository.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Producto> obtenerProducto(@PathVariable Long id) {

        return productoRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<?> crearProducto(@RequestBody Producto producto,
                                       @AuthenticationPrincipal Jwt jwt) {

        if (producto.getNombre() == null || producto.getNombre().isBlank()
                || producto.getPrecio() == null
                || producto.getStock() == null) {

            return ResponseEntity.badRequest()
                    .body("Nombre, precio y stock son obligatorios.");
        }

        Producto nuevoProducto = productoRepository.save(producto);
        eventos.productoCreado(nuevoProducto, usuarioDe(jwt));
        return ResponseEntity.status(201).body(nuevoProducto);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> actualizarProducto(
            @PathVariable Long id,
            @RequestBody Producto producto,@AuthenticationPrincipal Jwt jwt) {

        if (producto.getNombre() == null || producto.getNombre().isBlank()
                || producto.getPrecio() == null
                || producto.getStock() == null) {

            return ResponseEntity.badRequest()
                    .body("Nombre, Precio y Stock son obligatorios.");
        }

        return productoRepository.findById(id)
                .map(productoExistente -> {

                    productoExistente.setNombre(producto.getNombre());
                    productoExistente.setDescripcion(producto.getDescripcion());
                    productoExistente.setPrecio(producto.getPrecio());
                    productoExistente.setStock(producto.getStock());
                    Producto actualizado = productoRepository.save(productoExistente);
                    eventos.productoActualizado(actualizado, usuarioDe(jwt));
                    return ResponseEntity.ok(actualizado);
                })
                .orElseGet(() ->
                        ResponseEntity.notFound().build()
                );
    }



    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminarProducto(@PathVariable Long id,@AuthenticationPrincipal Jwt jwt) {

        if (!productoRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }

        productoRepository.deleteById(id);
        eventos.productoEliminado(id, usuarioDe(jwt));
        return ResponseEntity.ok(
                java.util.Map.of("message", "Producto eliminado correctamente.")
        );
    }
}