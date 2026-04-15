package com.hean.consigueventas.techstorepro.repository;

import com.hean.consigueventas.techstorepro.entity.Carrito;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface CarritoRepository extends JpaRepository<Carrito,Long> {
    // Recuperar el carrito por el ID del usuario
    Optional<Carrito> findByUsuarioId(Long usuarioId);
    // Busca carritos cuya última actualización sea anterior a una fecha dada
    List<Carrito> findByFechaUltimaActualizacionBefore(LocalDateTime fechaLimite);
}
