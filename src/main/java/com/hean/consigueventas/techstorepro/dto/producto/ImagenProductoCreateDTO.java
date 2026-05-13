package com.hean.consigueventas.techstorepro.dto.producto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter
@AllArgsConstructor @NoArgsConstructor
public class ImagenProductoCreateDTO {
    private Long id;
    private String nombreArchivo;
    private String urlPublica;
    private String providerId;
    private String storageProvider;
    private Boolean esPrincipal;
    private String formato;
}
