package com.hean.consigueventas.techstorepro.repository;

import com.hean.consigueventas.techstorepro.entity.carrito.CarritoEvento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AnalyticsRepository extends JpaRepository<CarritoEvento, Long> {

    // Conteo para el Funnel: Agregados vs Intentos de Checkout
    @Query("SELECT e.tipoEvento, COUNT(e) FROM CarritoEvento e GROUP BY e.tipoEvento")
    List<Object[]> countEventosPorTipo();

    // Top Productos con más 'Fugas' (QUITAR_PRODUCTO)
    @Query(value = "SELECT p.nombre, COUNT(e.id) as total " +
            "FROM carrito_evento_log e " +
            "JOIN productos p ON e.producto_id = p.id " +
            "WHERE e.tipo_evento = 'QUITAR_PRODUCTO' " +
            "GROUP BY p.id ORDER BY total DESC LIMIT 5", nativeQuery = true)
    List<Object[]> getTopProductosAbandonados();
}
