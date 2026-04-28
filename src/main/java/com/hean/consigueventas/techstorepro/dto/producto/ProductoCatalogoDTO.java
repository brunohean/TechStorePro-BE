package com.hean.consigueventas.techstorepro.dto.producto;

import lombok.*;

/**
 * DTO de Proyección (Lectura Rápida)
 * CISO Check: Optimización de payload para evitar Over-fetching en la capa de presentación.
 */
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class ProductoCatalogoDTO {
    private Long id;
    private String nombre;
    private Double precio;
    private Integer stock; // Útil para mostrar etiqueta "Agotado"
    private String imagenPrincipalUrl; // La URL plana que el frontend pide
}
