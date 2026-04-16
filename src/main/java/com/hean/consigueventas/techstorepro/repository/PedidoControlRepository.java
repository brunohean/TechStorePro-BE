package com.hean.consigueventas.techstorepro.repository;

import com.hean.consigueventas.techstorepro.entity.pedido.PedidoControl;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PedidoControlRepository extends JpaRepository<PedidoControl, Long> {
}
