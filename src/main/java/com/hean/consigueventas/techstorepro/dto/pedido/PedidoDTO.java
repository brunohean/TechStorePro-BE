package com.hean.consigueventas.techstorepro.dto.pedido;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PedidoDTO {
    private Long id;
    private LocalDateTime fecha;
    private Double total;
    private String estado;
    private List<DetallePedidoDTO> detalles;
}
