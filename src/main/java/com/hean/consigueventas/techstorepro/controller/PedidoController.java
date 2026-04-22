package com.hean.consigueventas.techstorepro.controller;

import com.hean.consigueventas.techstorepro.dto.MensajeResponse;
import com.hean.consigueventas.techstorepro.dto.pedido.CambiarEstadoRequest;
import com.hean.consigueventas.techstorepro.dto.pedido.CancelarPedidoRequest;
import com.hean.consigueventas.techstorepro.dto.pedido.PedidoDTO;
import com.hean.consigueventas.techstorepro.dto.pedido.PedidoEstadoLogDTO;
import com.hean.consigueventas.techstorepro.security.SecurityConstants;
import com.hean.consigueventas.techstorepro.service.PedidoService;
import com.hean.consigueventas.techstorepro.utils.RequestUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/pedidos")
@PreAuthorize("hasAnyRole('ADMIN', 'USER')")
@RequiredArgsConstructor
public class PedidoController {

    private final PedidoService pedidoServ;

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

    // 5. OBTENER LOG HISTORICO DE PEDIDO (ADMIN)
    @GetMapping("/{id}/historial")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<PedidoEstadoLogDTO>> obtenerHistorial(@PathVariable Long id) {
        return ResponseEntity.ok(pedidoServ.obtenerHistorial(id));
    }

    // 6. CAMBIAR ESTADO (Admin)
    @PatchMapping("/{id}/estado")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PedidoDTO> cambiarEstado(
            @PathVariable Long id, @RequestBody CambiarEstadoRequest request,
            HttpServletRequest servletRequest) {
        String ipCliente = RequestUtils.getClientIp(servletRequest); // Extraemos la IP del encabezado o de la conexión remota
        return ResponseEntity.ok(pedidoServ.actualizarEstado(id, request, ipCliente));
    }

    // 7. CANCELAR PEDIDO (Usuario)
    @PatchMapping("/{id}/cancelar")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<PedidoDTO> cancelarPedido(
            @PathVariable Long id, @Valid @RequestBody CancelarPedidoRequest request, // Usamos @Valid para que Spring revise el DTO
            HttpServletRequest servletRequest) {
        String ipCliente = RequestUtils.getClientIp(servletRequest);
        return ResponseEntity.ok(pedidoServ.cancelarPedidoPropio(id, request, ipCliente));
    }

    // 8. ELIMINAR PEDIDO (USUARIO)
    @DeleteMapping("/{id}/ocultar")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<MensajeResponse> ocultarPedido(@PathVariable Long id) {
        pedidoServ.ocultarPedidoParaUsuario(id);
        return ResponseEntity.ok(new MensajeResponse("Pedido eliminado de su historial."));
    }

    // 9. ELIMINAR PEDIDO (ADMIN)
    @DeleteMapping("/{id}/admin/ocultar")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MensajeResponse> ocultarPedidoAdmin(@PathVariable Long id) {
        pedidoServ.ocultarPedidoParaAdmin(id);
        return ResponseEntity.ok(new MensajeResponse("Pedido ocultado de los reportes administrativos exitosamente."));
    }

    // 10. ENDPOINT CAMBIAR ESTADO: ENVIADO (ADMIN/LOGISTICA) ALMACEN
    @PatchMapping("/{id}/enviar")
    @PreAuthorize(SecurityConstants.HAS_ROLE_ADMIN) // O HAS_ROLE_LOGISTICA si tienes ese rol
    public ResponseEntity<PedidoDTO> despacharPedido(@PathVariable Long id) {
        PedidoDTO pedidoActualizado = pedidoServ.marcarComoEnviado(id);
        return ResponseEntity.ok(pedidoActualizado);
    }

    // 11. ENDPOINT CAMBIAR ESTADO: ENTREGADO (ADMIN/LOGISTICA/REPARTIDOR) REPARTIDOR
    @PatchMapping("/{id}/entregar")
    @PreAuthorize(SecurityConstants.HAS_ROLE_ADMIN)
    public ResponseEntity<PedidoDTO> entregarPedido(@PathVariable Long id) {
        PedidoDTO pedidoActualizado = pedidoServ.marcarComoEntregado(id);
        return ResponseEntity.ok(pedidoActualizado);
    }

}
