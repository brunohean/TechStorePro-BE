package com.hean.consigueventas.techstorepro.controller;

import com.hean.consigueventas.techstorepro.dto.CarritoDTO;
import com.hean.consigueventas.techstorepro.dto.CarritoItemDTO;
import com.hean.consigueventas.techstorepro.entity.Carrito;
import com.hean.consigueventas.techstorepro.security.SecurityConstants;
import com.hean.consigueventas.techstorepro.security.services.UserDetailsImpl;
import com.hean.consigueventas.techstorepro.service.CarritoService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

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
        return ResponseEntity.ok(carServ.obtenerCarritoDto(getUsuarioIdAutenticado()));
    }

    // 2. Agregar producto al carrito
    @PostMapping("/agregar")
    @PreAuthorize(SecurityConstants.HAS_ROLE_USER)
    public ResponseEntity<CarritoDTO> agregarProducto(
            @RequestParam Long productoId,
            @RequestParam Integer cantidad) {

        return ResponseEntity.ok(carServ.agregarProducto(getUsuarioIdAutenticado(), productoId, cantidad));
    }


    // Metodos Auxiliares

    // MÉTODO AUXILIAR DE SEGURIDAD (CISO Best Practice)
    private Long getUsuarioIdAutenticado() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        return userDetails.getId();
    }
}
