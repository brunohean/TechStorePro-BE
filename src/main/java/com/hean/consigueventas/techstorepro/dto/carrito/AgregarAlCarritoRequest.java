package com.hean.consigueventas.techstorepro.dto.carrito;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter
@AllArgsConstructor @NoArgsConstructor
public class AgregarAlCarritoRequest {
    private Long productoId;
    private Integer cantidad;
}
