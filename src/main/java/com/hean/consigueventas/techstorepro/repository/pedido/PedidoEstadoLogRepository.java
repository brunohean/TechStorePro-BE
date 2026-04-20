package com.hean.consigueventas.techstorepro.repository.pedido;

import com.hean.consigueventas.techstorepro.entity.pedido.PedidoEstadoLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PedidoEstadoLogRepository extends JpaRepository<PedidoEstadoLog, Long> {

    @Query(value = "SELECT " +
            "AVG(EXTRACT(EPOCH FROM (l2.fecha_cambio - l1.fecha_cambio)) / 3600) as promedio," +
            "COUNT(DISTINCT l1.pedido_id) as total " +
            "FROM pedido_estado_historial l1 " +
            "JOIN pedido_estado_historial l2 ON l1.pedido_id = l2.pedido_id " +
            "WHERE l1.estado_nuevo = 'PAGADO' AND l2.estado_nuevo = 'ENTREGADO'",
            nativeQuery = true)
    List<Object[]> getAverageDeliveryTimeData();

    List<PedidoEstadoLog> findByPedidoIdOrderByFechaCambioDesc(Long pedidoId);
}
