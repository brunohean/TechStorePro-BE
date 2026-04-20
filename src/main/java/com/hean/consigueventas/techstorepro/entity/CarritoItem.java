package com.hean.consigueventas.techstorepro.entity;

import com.hean.consigueventas.techstorepro.entity.carrito.Carrito;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import lombok.*;

@Entity
@Table(name = "carrito_items")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class CarritoItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "carrito_id", nullable = false)
    private Carrito carrito;

    @ManyToOne
    @JoinColumn(name = "producto_id", nullable = false)
    private Producto producto;

    @Min(value = 1, message = "La cantidad mínima debe ser 1")
    @Column(nullable = false)
    private Integer cantidad;
}
