package com.hean.consigueventas.techstorepro.repository;

import com.hean.consigueventas.techstorepro.entity.Carrito;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CarritoRepository extends JpaRepository<Carrito,Long> {
    // Recuperar el carrito por el ID del usuario
    Optional<Carrito> findByUsuarioId(Long usuarioId);
}
