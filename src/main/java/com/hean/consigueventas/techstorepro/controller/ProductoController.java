package com.hean.consigueventas.techstorepro.controller;

import com.hean.consigueventas.techstorepro.dto.producto.ProductoCatalogoDTO;
import com.hean.consigueventas.techstorepro.dto.producto.ProductoDTO;
import com.hean.consigueventas.techstorepro.exception.custom.BusinessLogicException;
import com.hean.consigueventas.techstorepro.security.SecurityConstants;
import com.hean.consigueventas.techstorepro.service.ProductoService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
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


    // G1. Obtener todos los productos (USER-PUBLICO)
    @GetMapping("/catalogo")
    public ResponseEntity<Page<ProductoCatalogoDTO>> listarCatalogoPublico(
            @PageableDefault(page = 0, size = 8, sort = "id", direction = Sort.Direction.ASC) Pageable pageable) {
        // Protección CISO: Evitar que alguien pida una página de 10,000 items y sature el servidor
        if (pageable.getPageSize() > 50) {
            throw new BusinessLogicException("El tamaño de página solicitado (" + pageable.getPageSize() + ") excede el límite máximo permitido de 50.");
        }
        return ResponseEntity.ok(proSer.listarCatalogoPublico(pageable));
    }

    // G2. Inventario Total de Productos (ADMIN)
    @GetMapping
    @PreAuthorize(SecurityConstants.HAS_ROLE_ADMIN)
    public ResponseEntity<Page<ProductoDTO>> listarInventarioAdmin(
            @PageableDefault(size = 10, sort = "id", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(proSer.listarInventarioAdmin(pageable));
    }

    // G3. Obtener un Detalle Producto por ID (ADMIN)
    @GetMapping("/catalogo/{id}")
    public ResponseEntity<ProductoDTO> obtenerDetalleCatalogo(@PathVariable Long id) {
        return ResponseEntity.ok(proSer.obtenerPorIdDetalleCatalogo(id));
    }

    // G4. Obtener un Detalle Producto por ID (ADMIN)
    @GetMapping("/{id}")
    @PreAuthorize(SecurityConstants.HAS_ROLE_ADMIN)
    public ResponseEntity<ProductoDTO> obtenerDetalleInventario(@PathVariable Long id) {
        return ResponseEntity.ok(proSer.obtenerPorIdDetalleInventario(id));
    }

    // Po1. Crear un producto (ADMIN)
    @PostMapping(consumes = {"multipart/form-data"})
    @PreAuthorize(SecurityConstants.HAS_ROLE_ADMIN)
    public ResponseEntity<ProductoDTO> crear(@RequestPart("producto") ProductoDTO dto, @RequestPart(value = "archivos", required = false) List<MultipartFile> archivos) {
        ProductoDTO nuevoProducto = proSer.crear(dto, archivos);
        return new ResponseEntity<>(nuevoProducto, HttpStatus.CREATED);
    }

    // Po2. Subir imágenes adicionales (ADMIN)
    @PostMapping(value = "/{id}/imagenes", consumes = {"multipart/form-data"})
    @PreAuthorize(SecurityConstants.HAS_ROLE_ADMIN)
    public ResponseEntity<ProductoDTO> subirImagenesAdicionales(
            @PathVariable Long id,
            @RequestPart("archivos") List<MultipartFile> archivos) {
        return ResponseEntity.ok(proSer.agregarImagenes(id, archivos));
    }

    // Pa1. Actualizar un datos básicos (ADMIN)
    @PatchMapping("/{id}")
    @PreAuthorize(SecurityConstants.HAS_ROLE_ADMIN) // RF-BE-02: Solo Admin edita
    public ResponseEntity<ProductoDTO> actualizarSoloDatos(@PathVariable Long id, @RequestBody ProductoDTO dto) {
        return ResponseEntity.ok(proSer.actualizarSoloDatos(id, dto));
    }

    // Pa2. Desactivar producto (ADMIN)
    @PatchMapping("/{id}/estado")  //Ejemplo: /api/productos/1/estado?activo=false
    @PreAuthorize(SecurityConstants.HAS_ROLE_ADMIN)
    public ResponseEntity<Void> cambiarEstado(@PathVariable Long id, @RequestParam boolean activo) {
        proSer.cambiarEstado(id, activo);
        return ResponseEntity.noContent().build();
    }

    // D1. Eliminar un producto (ADMIN)
    @DeleteMapping("/{id}")
    @PreAuthorize(SecurityConstants.HAS_ROLE_ADMIN) // RF-BE-02: Solo Admin borra
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        proSer.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    // D2. Eliminar una imagen específica (ADMIN)
    @DeleteMapping("/{id}/imagenes/{imagenId}")
    @PreAuthorize(SecurityConstants.HAS_ROLE_ADMIN)
    public ResponseEntity<Void> eliminarImagen(@PathVariable Long id, @PathVariable Long imagenId) {
        proSer.eliminarImagenEspecifica(id, imagenId);
        return ResponseEntity.noContent().build();
    }
}