package com.hean.consigueventas.techstorepro.controller;

import com.hean.consigueventas.techstorepro.dto.carrito.CarritoDTO;
import com.hean.consigueventas.techstorepro.security.SecurityConstants;
import com.hean.consigueventas.techstorepro.security.SecurityUtils;
import com.hean.consigueventas.techstorepro.service.CarritoService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/carrito")
@PreAuthorize(SecurityConstants.HAS_ROLE_USER)
public class CarritoController {
    private final CarritoService carServ;

    public CarritoController(CarritoService carServ) {
        this.carServ = carServ;
    }

    // 1. Obtener el carrito del usuario autenticado
    @GetMapping
    public ResponseEntity<CarritoDTO> obtenerCarrito() {
        return ResponseEntity.ok(carServ.obtenerCarritoDto(SecurityUtils.getUsuarioIdAutenticado()));
    }

    // 2. Agregar producto al carrito
    @PostMapping("/agregar")
    @PreAuthorize(SecurityConstants.HAS_ROLE_USER)
    public ResponseEntity<CarritoDTO> agregarProducto(
            @RequestParam Long productoId,
            @RequestParam Integer cantidad) {

        return ResponseEntity.ok(carServ.agregarProducto(SecurityUtils.getUsuarioIdAutenticado(), productoId, cantidad));
    }
}
