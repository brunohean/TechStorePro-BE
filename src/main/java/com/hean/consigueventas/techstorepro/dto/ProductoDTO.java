package com.hean.consigueventas.techstorepro.dto;

import lombok.*;

@Getter @Setter
@AllArgsConstructor @NoArgsConstructor
public class ProductoDTO {
    private Long id;
    private String nombre;
    private String descripcion;
    private Double precio;
    private String imagenUrl;
    private Integer stock; // Lo incluimos para que Angular sepa si mostrar "Agotado"
    private boolean activo;
}
