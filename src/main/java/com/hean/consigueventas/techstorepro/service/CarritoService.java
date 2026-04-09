package com.hean.consigueventas.techstorepro.service;

import com.hean.consigueventas.techstorepro.dto.CarritoDTO;
import com.hean.consigueventas.techstorepro.entity.Carrito;
import com.hean.consigueventas.techstorepro.entity.CarritoItem;
import com.hean.consigueventas.techstorepro.entity.Producto;
import com.hean.consigueventas.techstorepro.entity.User;
import com.hean.consigueventas.techstorepro.mapper.CarritoMapper;
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
    private final CarritoMapper carritoMapper;

    public CarritoService(CarritoRepository carritoRepository, ProductoRepository productoRepository, UserRepository userRepo, CarritoMapper carritoMapper) {
        this.carritoRepo = carritoRepository;
        this.productoRepo = productoRepository;
        this.userRepo =  userRepo;
        this.carritoMapper = carritoMapper;
    }

    @Transactional
    public CarritoDTO agregarProducto(Long usuarioId, Long productoId, Integer cantidad) {

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

        Carrito carritoGuardado = carritoRepo.save(carrito);
        return carritoMapper.toDto(carritoGuardado);
    }

    @Transactional(readOnly = true)
    public CarritoDTO obtenerCarritoDto(Long usuarioId) {
        Carrito carrito = carritoRepo.findByUsuarioId(usuarioId)
                .orElseGet(() -> crearNuevoCarrito(usuarioId));
        return carritoMapper.toDto(carrito);
    }

    // Método privado para crear carrito si no existe
    private Carrito crearNuevoCarrito(Long usuarioId) {
        User usuario = userRepo.findById(usuarioId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        Carrito nuevo = new Carrito();
        nuevo.setUsuario(usuario);
        return carritoRepo.save(nuevo);
    }
}
