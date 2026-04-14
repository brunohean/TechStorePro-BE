package com.hean.consigueventas.techstorepro.repository;

import com.hean.consigueventas.techstorepro.entity.pedido.Pedido;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PedidoRepository extends JpaRepository<Pedido,Long> {
    // Listar pedidos de un usuario ordenados por fecha descendente
    List<Pedido> findByUsuarioIdOrderByFechaDesc(Long usuarioId);

    // Consulta para el Usuario: Solo los que él no ha "borrado"
    @Query("SELECT p FROM Pedido p JOIN p.control c WHERE p.usuario.id = :uid AND c.visibleParaUsuario = true ORDER BY p.fecha DESC")
    List<Pedido> findVisibleByUsuarioId(@Param("uid") Long uid);

    // Consulta para el Admin: Todos los que no han sido descartados por auditoría
    @Query("SELECT p FROM Pedido p JOIN p.control c WHERE c.visibleParaAdmin = true")
    List<Pedido> findAllVisibleForAdmin();
}
