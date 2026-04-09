package com.hean.consigueventas.techstorepro.dto;

import lombok.Data;
import java.util.List;

@Data
public class CarritoDTO {
    private Long id;
    private String username; // Solo el nombre, nada de passwords
    private List<CarritoItemDTO> items;
    private Double total;
}
