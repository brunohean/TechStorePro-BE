package com.hean.consigueventas.techstorepro.service;

import com.hean.consigueventas.techstorepro.entity.Carrito;
import com.hean.consigueventas.techstorepro.entity.CarritoItem;
import com.hean.consigueventas.techstorepro.entity.Producto;
import com.hean.consigueventas.techstorepro.entity.User;
import com.hean.consigueventas.techstorepro.repository.CarritoRepository;
import com.hean.consigueventas.techstorepro.repository.ProductoRepository;
import com.hean.consigueventas.techstorepro.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CarritoService {

    private final CarritoRepository carritoRepo;
    private final ProductoRepository productoRepo;
    private final UserRepository userRepo;

    public CarritoService(CarritoRepository carritoRepository, ProductoRepository productoRepository, UserRepository userRepo) {
        this.carritoRepo = carritoRepository;
        this.productoRepo = productoRepository;
        this.userRepo =  userRepo;
    }

    @Transactional
    public Carrito agregarProducto(Long usuarioId, Long productoId, Integer cantidad) {

        // Buscar el carrito o crearlo vinculando al usuario
        Carrito carrito = carritoRepo.findByUsuarioId(usuarioId)
                .orElseGet(() -> {
                    User usuario = userRepo.findById(usuarioId)
                            .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

                    Carrito nuevoCarrito = new Carrito();
                    nuevoCarrito.setUsuario(usuario); // <--- VÍNCULO VITAL
                    return carritoRepo.save(nuevoCarrito);
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

    @Transactional(readOnly = true)
    public Carrito obtenerPorUsuarioId(Long usuarioId) {
        return carritoRepo.findByUsuarioId(usuarioId)
                .orElseGet(() -> {
                    // Si no tiene carrito, se le crea uno nuevo vinculado a su cuenta
                    // Necesitarás inyectar UserRepository para buscar la entidad User completa
                    return new Carrito();
                });
    }
}
