package com.hean.consigueventas.techstorepro.controller;

import com.hean.consigueventas.techstorepro.dto.ProductoDTO;
import com.hean.consigueventas.techstorepro.entity.Producto;
import com.hean.consigueventas.techstorepro.service.ProductoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/productos")
public class ProductoController {

    private final ProductoService proSer;

    public ProductoController(ProductoService productoService) {
        this.proSer = productoService;
    }

    // 1. Obtener todos los productos (Catálogo)
    @GetMapping
    public ResponseEntity<List<ProductoDTO>> listarProductos() {
        List<ProductoDTO> productos = proSer.listarTodos().stream()
                .map(this::convertirADto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(productos);
    }

    // 2. Obtener un producto por ID (Detalle)
    @GetMapping("/{id}")
    public ResponseEntity<ProductoDTO> obtenerProducto(@PathVariable Long id) {
        Producto producto = proSer.obtenerPorId(id);
        return ResponseEntity.ok(convertirADto(producto));
    }

    // Metodo privado para mapear Entidad -> DTO (CISO Check: Capa de abstracción)
    private ProductoDTO convertirADto(Producto producto) {
        return new ProductoDTO(
                producto.getId(),
                producto.getNombre(),
                producto.getDescripcion(),
                producto.getPrecio(),
                producto.getImagenUrl(),
                producto.getStock()
        );
    }
}