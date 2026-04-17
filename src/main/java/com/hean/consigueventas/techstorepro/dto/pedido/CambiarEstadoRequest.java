package com.hean.consigueventas.techstorepro.dto.pedido;

import com.hean.consigueventas.techstorepro.entity.pedido.EstadoPedido;
import lombok.*;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class CambiarEstadoRequest {
    private EstadoPedido nuevoEstado;
    private String motivo; // Opcional pero recomendado para auditoría
}
