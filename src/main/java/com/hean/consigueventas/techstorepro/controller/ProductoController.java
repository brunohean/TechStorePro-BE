package com.hean.consigueventas.techstorepro.controller;

import com.hean.consigueventas.techstorepro.dto.ProductoDTO;
import com.hean.consigueventas.techstorepro.security.SecurityConstants;
import com.hean.consigueventas.techstorepro.service.ProductoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/productos")
@RequiredArgsConstructor
public class ProductoController {

    private final ProductoService proSer;


    // 1. Obtener todos los productos (Catálogo)
    @GetMapping
    public ResponseEntity<List<ProductoDTO>> listarProductos() {
        return ResponseEntity.ok(proSer.listarTodos());
    }

    // 2. Obtener un producto por ID (Detalle)
    @GetMapping("/{id}")
    public ResponseEntity<ProductoDTO> obtenerProducto(@PathVariable Long id) {
        return ResponseEntity.ok(proSer.obtenerPorId(id));
    }

    // 3. Crear un producto (ADMIN)
    @PostMapping(consumes = {"multipart/form-data"})
    @PreAuthorize(SecurityConstants.HAS_ROLE_ADMIN)
    public ResponseEntity<ProductoDTO> crear(@RequestPart("producto") ProductoDTO dto, @RequestPart(value = "archivos", required = false) List<MultipartFile> archivos) {
        ProductoDTO nuevoProducto = proSer.crear(dto, archivos);
        return new ResponseEntity<>(nuevoProducto, HttpStatus.CREATED);
    }

    // 4. Actualizar un producto (ADMIN)
    @PutMapping("/{id}")
    @PreAuthorize(SecurityConstants.HAS_ROLE_ADMIN) // RF-BE-02: Solo Admin edita
    public ResponseEntity<ProductoDTO> actualizar(@PathVariable Long id, @RequestBody ProductoDTO dto) {
        return ResponseEntity.ok(proSer.actualizar(id, dto));
    }

    // 5. Eliminar un producto (ADMIN)
    @DeleteMapping("/{id}")
    @PreAuthorize(SecurityConstants.HAS_ROLE_ADMIN) // RF-BE-02: Solo Admin borra
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        proSer.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    // 6. Desactivar producto (ADMIN)
    @PatchMapping("/{id}/estado")  //Ejemplo: /api/productos/1/estado?activo=false
    @PreAuthorize(SecurityConstants.HAS_ROLE_ADMIN)
    public ResponseEntity<Void> cambiarEstado(@PathVariable Long id, @RequestParam boolean activo) {
        proSer.cambiarEstado(id, activo);
        return ResponseEntity.noContent().build();
    }
}