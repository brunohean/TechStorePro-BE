package com.hean.consigueventas.techstorepro.controller;

import com.hean.consigueventas.techstorepro.dto.CarritoDTO;
import com.hean.consigueventas.techstorepro.dto.CarritoItemDTO;
import com.hean.consigueventas.techstorepro.entity.Carrito;
import com.hean.consigueventas.techstorepro.security.services.UserDetailsImpl;
import com.hean.consigueventas.techstorepro.service.CarritoService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/carrito")
public class CarritoController {
    private final CarritoService carritoService;

    public CarritoController(CarritoService carritoService) {
        this.carritoService = carritoService;
    }

    // 1. Obtener el carrito del usuario autenticado
    @GetMapping
    public ResponseEntity<CarritoDTO> obtenerCarrito() {
        Long usuarioId = getUsuarioIdAutenticado();
        Carrito carrito = carritoService.obtenerPorUsuarioId(usuarioId);
        return ResponseEntity.ok(convertirADto(carrito));
    }

    // 2. Agregar producto al carrito
    @PostMapping("/agregar")
    public ResponseEntity<CarritoDTO> agregarProducto(
            @RequestParam Long productoId,
            @RequestParam Integer cantidad) {

        Long usuarioId = getUsuarioIdAutenticado();
        Carrito carritoActualizado = carritoService.agregarProducto(usuarioId, productoId, cantidad);
        return ResponseEntity.ok(convertirADto(carritoActualizado));
    }


    // Metodos Auxiliares

    // MÉTODO AUXILIAR DE SEGURIDAD (CISO Best Practice)
    private Long getUsuarioIdAutenticado() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        return userDetails.getId();
    }

    private CarritoDTO convertirADto(Carrito carrito) {
        CarritoDTO dto = new CarritoDTO();
        dto.setId(carrito.getId());
        dto.setUsername(carrito.getUsuario().getUsername());

        List<CarritoItemDTO> itemDtos = carrito.getItems().stream().map(item ->
                new CarritoItemDTO(
                        item.getId(),
                        item.getProducto().getId(),
                        item.getProducto().getNombre(),
                        item.getProducto().getPrecio(),
                        item.getCantidad()
                )
        ).collect(Collectors.toList());

        dto.setItems(itemDtos);
        // Cálculo rápido del total
        dto.setTotal(itemDtos.stream().mapToDouble(i -> i.getPrecioUnitario() * i.getCantidad()).sum());

        return dto;
    }
}
