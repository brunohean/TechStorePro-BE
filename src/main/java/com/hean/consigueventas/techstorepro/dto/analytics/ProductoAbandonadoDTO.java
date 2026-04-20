package com.hean.consigueventas.techstorepro.dto.analytics;

import lombok.*;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class ProductoAbandonadoDTO {
    private String nombreProducto;
    private Long cantidadAbandonos;
}
