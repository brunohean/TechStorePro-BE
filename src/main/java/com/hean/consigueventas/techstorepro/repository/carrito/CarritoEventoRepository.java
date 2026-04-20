package com.hean.consigueventas.techstorepro.repository.carrito;

import com.hean.consigueventas.techstorepro.entity.carrito.CarritoEvento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CarritoEventoRepository extends JpaRepository<CarritoEvento, Long> {
}
