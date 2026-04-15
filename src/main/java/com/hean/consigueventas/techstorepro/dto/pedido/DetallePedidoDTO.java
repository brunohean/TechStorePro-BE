package com.hean.consigueventas.techstorepro.dto.pedido;

import lombok.*;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
public class DetallePedidoDTO {
    private Long productoId;
    private String productoNombre;
    private Integer cantidad;
    private Double precioUnitario;
    private Double subtotal; // cantidad * precioUnitario
}
