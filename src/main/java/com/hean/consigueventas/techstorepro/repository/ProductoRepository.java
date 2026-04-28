package com.hean.consigueventas.techstorepro.repository;

import com.hean.consigueventas.techstorepro.entity.Producto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductoRepository extends JpaRepository <Producto,Long> {
    boolean existsByNombre(String nombre);
    Optional<Producto> findByNombre(String nombre);
    List<Producto> findByNombreContainingIgnoreCase(String nombre);
    List<Producto> findByActivoTrue();
    Optional<Producto> findByIdAndActivoTrue(Long id);
}
