package com.cloudnative.tiendaperritos.repository;


import org.springframework.data.jpa.repository.JpaRepository;

import com.cloudnative.tiendaperritos.entity.Producto;

public interface ProductoRepository extends JpaRepository<Producto, Long> {
}