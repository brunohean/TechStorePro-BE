package com.hean.consigueventas.techstorepro.entity;

import com.hean.consigueventas.techstorepro.entity.media.ImagenProducto;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "productos")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class Producto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "El nombre del producto no puede estar vacío")
    @Column(nullable = false, unique = true, length = 100)
    private String nombre;

    @Column(length = 500)
    private String descripcion;

    @NotNull(message = "El precio es obligatorio")
    @DecimalMin(value = "0.01", message = "El precio debe ser mayor a cero")
    @Column(nullable = false)
    private Double precio;

    @NotNull(message = "El stock es obligatorio")
    @Min(value = 0, message = "El stock no puede ser negativo")
    @Column(nullable = false)
    private Integer stock;

    private boolean activo = true;

    // Mapeo bidireccional: Un producto puede tener muchas imágenes
    @OneToMany(mappedBy = "producto", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ImagenProducto> imagenes = new ArrayList<>();

    // Métodos utilitarios (Helper methods) para sincronizar la relación bidireccional
    public void addImagen(ImagenProducto imagen) {
        // Programación defensiva: Si MapStruct o Lombok volvieron la lista nula, la revivimos.
        if (this.imagenes == null) {
            this.imagenes = new ArrayList<>();
        }

        imagenes.add(imagen);       // Paso 1: El producto adopta la imagen
        imagen.setProducto(this);   // Paso 2: La imagen reconoce a su dueño (sincroniza el ID).
    }

    public void removeImagen(ImagenProducto imagen) {
        if (this.imagenes != null) {
            this.imagenes.remove(imagen);
            imagen.setProducto(null);
        }
    }
}