package com.hean.consigueventas.techstorepro.dto.pedido;

import com.hean.consigueventas.techstorepro.entity.pedido.EstadoPedido;
import lombok.*;

import java.time.LocalDateTime;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class PedidoEstadoLogDTO {
    private EstadoPedido estadoAnterior;
    private EstadoPedido estadoNuevo;
    private LocalDateTime fechaCambio;
    private String responsable; // El username que extraemos del Token
    private String motivo;
    private String ipOrigen; // La IP capturada por RequestUtils
}
