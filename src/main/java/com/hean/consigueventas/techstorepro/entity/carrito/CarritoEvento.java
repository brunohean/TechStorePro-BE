package com.hean.consigueventas.techstorepro.entity.carrito;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "carrito_evento_log")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class CarritoEvento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long usuarioId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoEventoCarrito tipoEvento;

    private Long productoId; // Null si es VACIAR_CARRITO

    private Integer cantidad; // Cantidad involucrada en el cambio

    @Column(nullable = false)
    private LocalDateTime fechaEvento;

    @Column(length = 45)
    private String ipOrigen; // Usando tu RequestUtils
}
