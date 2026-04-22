package com.hean.consigueventas.techstorepro.entity.media;

import com.hean.consigueventas.techstorepro.entity.Producto;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "imagenes_producto")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class ImagenProducto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nombre_archivo", nullable = false)
    private String nombreArchivo;

    // La interfaz universal: Frontend (Angular) solo consume este atributo
    @Column(name = "url_publica", nullable = false, length = 1000)
    private String urlPublica;

    // El ID interno del proveedor, vital para operaciones de borrado/actualización
    @Column(name = "provider_id", nullable = false)
    private String providerId;

    @Enumerated(EnumType.STRING)
    @Column(name = "storage_provider", nullable = false)
    private StorageProvider storageProvider;

    @Column(length = 20)
    private String formato; // ej: webp, pdf, png

    @Column(name = "es_principal")
    private boolean esPrincipal; // Para saber cuál mostrar en la cuadrícula de la tienda

    // Relación bidireccional con Producto
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "producto_id", nullable = false)
    private Producto producto;
}
