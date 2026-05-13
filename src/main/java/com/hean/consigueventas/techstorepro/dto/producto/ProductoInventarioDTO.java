package com.hean.consigueventas.techstorepro.dto.producto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter
@AllArgsConstructor @NoArgsConstructor
public class ProductoInventarioDTO {
    private Long id;
    private String nombre;
    private Double precio;
    private Integer stock;
    private Boolean activo;
    private Long version;
    private Long categoriaId; // Solo aplicar filtros en la tabla por categoria
    private String imagenPrincipalUrl;
}
