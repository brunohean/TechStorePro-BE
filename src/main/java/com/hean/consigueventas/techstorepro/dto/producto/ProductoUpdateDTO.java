package com.hean.consigueventas.techstorepro.dto.producto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter @Setter
@AllArgsConstructor @NoArgsConstructor
public class ProductoUpdateDTO {
    @NotNull private Long id;
    @NotNull private Long version;    // Bloqueo Optimista, solo necesario en métodos PUT/PATCH
    @NotBlank private String nombre;
    private String descripcion;
    @Positive private Double precio;
    @Min(0) private Integer stock;
    private Boolean activo;
    @NotNull private Long categoriaId;
    private List<Long> imagenesMantenerIds; // Sincronización: Lista de IDs de imágenes que el usuario decidió conservar
    private Long ImagenPrincipalId; // Identificador de cuál de las imágenes existentes será la principal
}
