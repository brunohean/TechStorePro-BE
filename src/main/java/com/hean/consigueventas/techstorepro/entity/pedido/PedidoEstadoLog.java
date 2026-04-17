package com.hean.consigueventas.techstorepro.entity.pedido;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "pedido_estado_historial")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class PedidoEstadoLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private EstadoPedido estadoAnterior;

    @Enumerated(EnumType.STRING)
    private EstadoPedido estadoNuevo;

    private LocalDateTime fechaCambio;
    private String responsable; // El username del Admin que hizo el cambio
    private String motivo;
    private String ipOrigen;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pedido_id", nullable = false)
    @JsonIgnore
    private Pedido pedido;
}
