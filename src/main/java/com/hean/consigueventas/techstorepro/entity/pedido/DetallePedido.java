package com.hean.consigueventas.techstorepro.entity.pedido;

import com.hean.consigueventas.techstorepro.entity.Producto;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "pedido_detalles")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class DetallePedido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Double precioUnitario;

    @Column(nullable = false)
    private Integer cantidad;

    @ManyToOne
    @JoinColumn(name = "pedido_id", nullable = false)
    private Pedido pedido;

    @ManyToOne
    @JoinColumn(name = "producto_id", nullable = false)
    private Producto producto;
}