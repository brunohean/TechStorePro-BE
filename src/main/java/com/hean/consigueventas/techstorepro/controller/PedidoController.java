package com.hean.consigueventas.techstorepro.controller;

import com.hean.consigueventas.techstorepro.dto.MensajeResponse;
import com.hean.consigueventas.techstorepro.dto.pedido.PedidoDTO;
import com.hean.consigueventas.techstorepro.entity.pedido.EstadoPedido;
import com.hean.consigueventas.techstorepro.service.PedidoService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/pedidos")
@PreAuthorize("hasAnyRole('ADMIN', 'USER')")
public class PedidoController {

    private final PedidoService pedidoServ;

    public PedidoController( PedidoService pedidoService ) {
        this.pedidoServ = pedidoService;
    }

    // 1. REALIZAR PEDIDO (Usuario)
    @PostMapping("/checkout")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<PedidoDTO> realizarCheckout() {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(pedidoServ.procesarCheckout());
    }

    // 2. VER MIS PEDIDOS (Usuario)
    @GetMapping("/mis-pedidos")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<List<PedidoDTO>> verMisPedidos() {
        return ResponseEntity.ok(pedidoServ.obtenerMisPedidos());
    }

    // 3. VER TODOS LOS PEDIDOS (Admin)
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<PedidoDTO>> listarTodo() {
        return ResponseEntity.ok(pedidoServ.listarTodos());
    }

    // 4. VER DETALLE (Admin o Dueño)
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    public ResponseEntity<PedidoDTO> verDetalle(@PathVariable Long id) {
        return ResponseEntity.ok(pedidoServ.obtenerPorId(id));
    }

    // 5. CAMBIAR ESTADO (Admin)
    @PatchMapping("/{id}/estado")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PedidoDTO> cambiarEstado(
            @PathVariable Long id,
            @RequestParam EstadoPedido nuevoEstado) {
        return ResponseEntity.ok(pedidoServ.actualizarEstado(id, nuevoEstado));
    }

    // 6. CANCELAR PEDIDO (Usuario)
    @PatchMapping("/{id}/cancelar")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<PedidoDTO> cancelarPedido(
            @PathVariable Long id,
            @RequestParam(required = false, defaultValue = "Cancelado por el usuario") String motivo) {

        return ResponseEntity.ok(pedidoServ.cancelarPedidoPropio(id, motivo));
    }

    // 7. ELIMINAR PEDIDO (USUARIO)
    @DeleteMapping("/{id}/ocultar")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<MensajeResponse> ocultarPedido(@PathVariable Long id) {
        pedidoServ.ocultarPedidoParaUsuario(id);
        return ResponseEntity.ok(new MensajeResponse("Pedido eliminado de su historial."));
    }

    // 8. ELIMINAR PEDIDO (ADMIN)
    @DeleteMapping("/{id}/admin/ocultar")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MensajeResponse> ocultarPedidoAdmin(@PathVariable Long id) {
        pedidoServ.ocultarPedidoParaAdmin(id);
        return ResponseEntity.ok(new MensajeResponse("Pedido ocultado de los reportes administrativos exitosamente."));
    }
}
