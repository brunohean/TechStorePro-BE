package com.hean.consigueventas.techstorepro.dto.carrito;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
public class ActualizacionMasivaRequest {
    private List<ItemUpdateDTO> items;
}
