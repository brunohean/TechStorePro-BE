package com.hean.consigueventas.techstorepro.dto.pedido;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class CancelarPedidoRequest {

    // Validamos que el usuario no mande espacios en blanco y limitamos el tamaño
    @NotBlank(message = "Debe proporcionar un motivo para la cancelación")
    @Size(max = 255, message = "El motivo no puede exceder los 255 caracteres")
    private String motivo;

}