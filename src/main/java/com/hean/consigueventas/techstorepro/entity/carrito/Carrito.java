package com.hean.consigueventas.techstorepro.entity.carrito;

import com.hean.consigueventas.techstorepro.entity.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "carritos")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class Carrito {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "usuario_id", nullable = false)
    private User usuario;

    @Column(name = "fecha_ultima_actualizacion")
    @UpdateTimestamp // <--- Se actualiza automáticamente en cada cambio
    private LocalDateTime fechaUltimaActualizacion;

    @Column(updatable = false)
    @CreationTimestamp // <--- Se queda fija con la fecha de creación
    private LocalDateTime fechaCreacion;

    @OneToMany(mappedBy = "carrito", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CarritoItem> items = new ArrayList<>();
}
