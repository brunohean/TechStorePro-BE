package com.hean.consigueventas.techstorepro.entity.pedido;

import com.hean.consigueventas.techstorepro.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "pedidos")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class Pedido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDateTime fecha;

    @Column(nullable = false)
    private Double total;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoPedido estado;

    private String clienteNombre; // Nombre de quien recibe
    private String celular;       // Contacto para el repartidor
    private String direccion;     // Destino final

    @OneToMany(mappedBy = "pedido", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DetallePedido> detalles = new ArrayList<>();

    @ManyToOne
    @JoinColumn(name = "usuario_id", nullable = false)
    private User usuario;

    @OneToOne(mappedBy = "pedido", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private PedidoControl control; // Relación para acceder a los metadatos

    // Método helper para asegurar la relación bidireccional
    public void setControl(PedidoControl control) {
        this.control = control;
        if (control != null) {
            control.setPedido(this);
        }
    }

    @PrePersist
    protected void onCreate() {
        this.fecha = LocalDateTime.now();
        if (this.estado == null) {
            this.estado = EstadoPedido.PENDIENTE;
        }
    }
}
