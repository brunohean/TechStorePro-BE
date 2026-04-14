package com.hean.consigueventas.techstorepro.service;

import com.hean.consigueventas.techstorepro.dto.carrito.CarritoDTO;
import com.hean.consigueventas.techstorepro.entity.Carrito;
import com.hean.consigueventas.techstorepro.entity.CarritoItem;
import com.hean.consigueventas.techstorepro.entity.Producto;
import com.hean.consigueventas.techstorepro.entity.User;
import com.hean.consigueventas.techstorepro.exception.custom.BusinessLogicException;
import com.hean.consigueventas.techstorepro.exception.custom.ResourceNotFoundException;
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

        // 1. Buscar producto con EXCEPCIÓN PERSONALIZADA
        Producto producto = productoRepo.findById(productoId)
                .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado con ID: " + productoId));

        // 2. VALIDACIÓN DE ESTADO (CISO Check: No permitimos productos desactivados)
        if (!producto.isActivo()) {
            throw new BusinessLogicException("El producto '" + producto.getNombre() + "' ya no está disponible para la venta.");
        }

        // 3. VALIDACIÓN DE STOCK (RF-BE-05) - Integridad Operativa
        if (producto.getStock() < cantidad) {
            throw new BusinessLogicException("Stock insuficiente para '" + producto.getNombre() + "'. Disponible: " + producto.getStock());
        }

        // 4. Buscar o crear el carrito (Usando ResourceNotFoundException)
        Carrito carrito = carritoRepo.findByUsuarioId(usuarioId)
                .orElseGet(() -> {
                    User usuario = userRepo.findById(usuarioId)
                            .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con ID: " + usuarioId));

                    Carrito nuevoCarrito = new Carrito();
                    nuevoCarrito.setUsuario(usuario);
                    return carritoRepo.save(nuevoCarrito);
                });

        // 5. Lógica para actualizar cantidad
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

    private Carrito crearNuevoCarrito(Long usuarioId) {
        User usuario = userRepo.findById(usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con ID: " + usuarioId));
        Carrito nuevo = new Carrito();
        nuevo.setUsuario(usuario);
        return carritoRepo.save(nuevo);
    }
}