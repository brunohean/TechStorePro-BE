package com.hean.consigueventas.techstorepro.service;

import com.hean.consigueventas.techstorepro.entity.Carrito;
import com.hean.consigueventas.techstorepro.entity.CarritoItem;
import com.hean.consigueventas.techstorepro.entity.Producto;
import com.hean.consigueventas.techstorepro.repository.CarritoRepository;
import com.hean.consigueventas.techstorepro.repository.ProductoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CarritoService {

    private final CarritoRepository carritoRepo;
    private final ProductoRepository productoRepo;

    public CarritoService(CarritoRepository carritoRepository, ProductoRepository productoRepository) {
        this.carritoRepo = carritoRepository;
        this.productoRepo = productoRepository;
    }

    @Transactional
    public Carrito agregarProducto(Long usuarioId, Long productoId, Integer cantidad) {
        Carrito carrito = carritoRepo.findByUsuarioId(usuarioId)
                .orElseGet(() -> {
                    // Si no existe, deberíamos crearlo (Lógica simplificada)
                    return new Carrito();
                });

        Producto producto = productoRepo.findById(productoId)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado"));

        // Lógica para actualizar cantidad si ya existe el ítem
        carrito.getItems().stream()
                .filter(item -> item.getProducto().getId().equals(productoId))
                .findFirst()
                .ifPresentOrElse(
                        item -> item.setCantidad(item.getCantidad() + cantidad),
                        () -> {
                            CarritoItem nuevoItem = new CarritoItem(null, carrito, producto, cantidad);
                            carrito.getItems().add(nuevoItem);
                        }
                );

        return carritoRepo.save(carrito);
    }
}
