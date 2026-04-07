package com.hean.consigueventas.techstorepro.repository;

import com.hean.consigueventas.techstorepro.entity.Pedido;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PedidoRepository extends JpaRepository<Pedido,Long> {
    // Listar pedidos de un usuario ordenados por fecha descendente
    List<Pedido> findByUsuarioIdOrderByFechaDesc(Long usuarioId);
}
