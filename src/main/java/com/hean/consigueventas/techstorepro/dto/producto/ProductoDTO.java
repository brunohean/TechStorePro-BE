package com.hean.consigueventas.techstorepro.dto.producto;

import lombok.*;

import java.util.List;

@Getter @Setter
@AllArgsConstructor @NoArgsConstructor
public class ProductoDTO {
    private Long id;
    private String nombre;
    private String descripcion;
    private Double precio;
    private Integer stock; // Lo incluimos para que Angular sepa si mostrar "Agotado"
    private boolean activo;
    // La galería de imágenes optimizada para el frontend
    private List<ImagenProductoDTO> imagenes;
}
