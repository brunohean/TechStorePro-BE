package com.hean.consigueventas.techstorepro.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CarritoItemDTO {
    private Long id;
    private Long productoId;
    private String productoNombre;
    private Double precioUnitario;
    private Integer cantidad;
}