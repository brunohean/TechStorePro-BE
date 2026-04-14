package com.hean.consigueventas.techstorepro.entity.pedido;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "pedido_control")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class PedidoControl {

    @Id
    private Long pedidoId; // PK que también es FK hacia Pedido

    @OneToOne
    @MapsId
    @JoinColumn(name = "pedido_id")
    @JsonIgnore // Evita recursividad
    private Pedido pedido;

    private boolean visibleParaUsuario = true;
    private boolean visibleParaAdmin = true;

    private LocalDateTime fechaUltimoCambioEstado;
    private String motivoCancelacion;
    private String ipRegistro;

    @Version // Optimistic Locking para evitar colisiones entre admins
    private Long version;
}