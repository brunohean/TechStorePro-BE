package com.hean.consigueventas.techstorepro.controller;

import com.hean.consigueventas.techstorepro.dto.MensajeResponse;
import com.hean.consigueventas.techstorepro.dto.carrito.ActualizarCantidadRequest;
import com.hean.consigueventas.techstorepro.dto.carrito.ActualizarMasivoRequest;
import com.hean.consigueventas.techstorepro.dto.carrito.AgregarAlCarritoRequest;
import com.hean.consigueventas.techstorepro.dto.carrito.CarritoDTO;
import com.hean.consigueventas.techstorepro.security.SecurityConstants;
import com.hean.consigueventas.techstorepro.security.SecurityUtils;
import com.hean.consigueventas.techstorepro.service.CarritoService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/carrito")
@PreAuthorize("hasAnyRole('ADMIN','USER')")
public class CarritoController {
    private final CarritoService carServ;

    public CarritoController(CarritoService carServ) {
        this.carServ = carServ;
    }

    // A. OBTENER EL CARRITO DEL USUARIO AUTENTICADO
    @GetMapping
    @PreAuthorize(SecurityConstants.HAS_ROLE_USER)
    public ResponseEntity<CarritoDTO> obtenerCarrito() {
        return ResponseEntity.ok(carServ.obtenerCarritoDto(SecurityUtils.getUsuarioIdAutenticado()));
    }

    // B. AGREGAR PRODUCTO AL CARRITO
    @PostMapping("/agregar")
    @PreAuthorize(SecurityConstants.HAS_ROLE_USER)
    public ResponseEntity<CarritoDTO> agregarProducto(@RequestBody AgregarAlCarritoRequest request) {
        return ResponseEntity.ok(carServ.agregarProducto(SecurityUtils.getUsuarioIdAutenticado(), request.getProductoId(), request.getCantidad()));
    }

    // C. ACTUALIZAR CANTIDAD DE UN ITEM
    @PutMapping("/item/{productoId}")
    @PreAuthorize(SecurityConstants.HAS_ROLE_USER)
    public ResponseEntity<CarritoDTO> actualizarCantidad(
            @PathVariable Long productoId,
            @RequestBody ActualizarCantidadRequest request) {
        return ResponseEntity.ok(carServ.actualizarCantidad(productoId, request.getCantidad()));
    }

    // D. ELIMINAR UN PRODUCTO DE UN CARRITO
    @DeleteMapping("/item/{productoId}")
    @PreAuthorize(SecurityConstants.HAS_ROLE_USER)
    public ResponseEntity<CarritoDTO> eliminarProducto(@PathVariable Long productoId) {
        return ResponseEntity.ok(carServ.eliminarProducto(productoId));
    }

    // E. VACIAR TODO EL CARRITO
    @DeleteMapping("/limpiar")
    @PreAuthorize(SecurityConstants.HAS_ROLE_USER)
    public ResponseEntity<MensajeResponse> limpiarCarrito() {
        carServ.limpiarCarrito();
        return ResponseEntity.ok(new MensajeResponse("Carrito vaciado correctamente"));
    }

    // F. Actualización masiva de ítems (Usuario)
    @PutMapping("/masivo")
    @PreAuthorize(SecurityConstants.HAS_ROLE_USER)
    public ResponseEntity<CarritoDTO> actualizarMasivo(@RequestBody ActualizarMasivoRequest request) {
        return ResponseEntity.ok(carServ.actualizarMasivo(request));
    }

    // G. Listar todos los carritos activos (Admin)
    @GetMapping("/admin/todos")
    @PreAuthorize(SecurityConstants.HAS_ROLE_ADMIN)
    public ResponseEntity<List<CarritoDTO>> listarTodosCarritos() {
        return ResponseEntity.ok(carServ.listarTodosLosCarritos());
    }

    // H. Ver carrito de un usuario por ID (Admin)
    @GetMapping("/admin/usuario/{usuarioId}")
    @PreAuthorize(SecurityConstants.HAS_ROLE_ADMIN)
    public ResponseEntity<CarritoDTO> verCarritoUsuarioAdmin(@PathVariable Long usuarioId) {
        return ResponseEntity.ok(carServ.obtenerCarritoPorUsuarioAdmin(usuarioId));
    }
}
