package com.hean.consigueventas.techstorepro.repository.pedido;

import com.hean.consigueventas.techstorepro.entity.pedido.PedidoDetalle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PedidoDetalleRepository extends JpaRepository<PedidoDetalle, Long> {
}
