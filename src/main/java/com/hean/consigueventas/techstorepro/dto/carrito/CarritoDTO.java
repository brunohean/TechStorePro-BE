package com.hean.consigueventas.techstorepro.dto.carrito;

import lombok.*;

import java.util.List;

@Getter @Setter
@AllArgsConstructor @NoArgsConstructor
public class CarritoDTO {
    private Long id;
    private String username; // Solo el nombre, nada de passwords
    private List<CarritoItemDTO> items;
    private Double total;
}
