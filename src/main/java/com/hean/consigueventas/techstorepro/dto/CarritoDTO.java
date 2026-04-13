package com.hean.consigueventas.techstorepro.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CarritoDTO {
    private Long id;
    private String username; // Solo el nombre, nada de passwords
    private List<CarritoItemDTO> items;
    private Double total;
}
