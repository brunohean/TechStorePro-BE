package com.hean.consigueventas.techstorepro.dto.producto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter
@AllArgsConstructor @NoArgsConstructor
public class ImagenProductoReadDTO {
    private Long id;
    private String urlPublica;
    private Boolean esPrincipal;
}