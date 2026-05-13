package com.hean.consigueventas.techstorepro.dto;

import lombok.*;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
public class CategoriaCreateDTO {
    private Long id;
    private String nombre;
    private String descripcion;
    private Boolean activo;
}
