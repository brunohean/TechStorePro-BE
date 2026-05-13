package com.hean.consigueventas.techstorepro.dto.producto;

import com.hean.consigueventas.techstorepro.dto.CategoriaReadDTO;
import lombok.*;

import java.util.List;

@Getter @Setter
@AllArgsConstructor @NoArgsConstructor
public class ProductoAdminDetalleDTO {
    private Long id;
    private String nombre;
    private String descripcion;
    private Double precio;
    private Integer stock;
    private Boolean activo;
    private Long version;       // Para el Bloqueo Optimista
    private CategoriaReadDTO categoria;
    private List<ImagenProductoReadDTO> imagenes;   // Galería completa con IDs para sincronizar
}
