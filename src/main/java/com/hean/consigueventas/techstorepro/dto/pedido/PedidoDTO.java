package com.hean.consigueventas.techstorepro.dto.pedido;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
public class PedidoDTO {
    private Long id;
    private LocalDateTime fecha;
    private Double total;
    private String estado;

    private String clienteNombre;
    private String celular;
    private String direccion;

    private List<DetallePedidoDTO> detalles;
}
