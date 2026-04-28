package com.hean.consigueventas.techstorepro.service;

import com.hean.consigueventas.techstorepro.dto.carrito.ActualizarMasivoRequest;
import com.hean.consigueventas.techstorepro.dto.carrito.CarritoDTO;
import com.hean.consigueventas.techstorepro.dto.carrito.ItemUpdateDTO;
import com.hean.consigueventas.techstorepro.entity.carrito.Carrito;
import com.hean.consigueventas.techstorepro.entity.carrito.CarritoItem;
import com.hean.consigueventas.techstorepro.entity.Producto;
import com.hean.consigueventas.techstorepro.entity.User;
import com.hean.consigueventas.techstorepro.entity.carrito.CarritoEvento;
import com.hean.consigueventas.techstorepro.entity.carrito.TipoEventoCarrito;
import com.hean.consigueventas.techstorepro.exception.custom.BusinessLogicException;
import com.hean.consigueventas.techstorepro.exception.custom.ResourceNotFoundException;
import com.hean.consigueventas.techstorepro.mapper.CarritoMapper;
import com.hean.consigueventas.techstorepro.repository.carrito.CarritoEventoRepository;
import com.hean.consigueventas.techstorepro.repository.carrito.CarritoRepository;
import com.hean.consigueventas.techstorepro.repository.ProductoRepository;
import com.hean.consigueventas.techstorepro.repository.UserRepository;
import com.hean.consigueventas.techstorepro.security.SecurityUtils;
import com.hean.consigueventas.techstorepro.utils.RequestUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor // Genera el constructor para los campos 'final'
public class CarritoService {

    private final CarritoRepository carritoRepo;
    private final CarritoEventoRepository carEventRepo;
    private final ProductoRepository productoRepo;
    private final UserRepository userRepo;
    private final CarritoMapper carMapper;
    private final HttpServletRequest httpRequest;

    // MÉTODOS DE SOPORTE

    private void registrarEvento(Long usuarioId, TipoEventoCarrito tipo, Long productoId, Integer cantidad) {
        CarritoEvento evento = CarritoEvento.builder()
                .usuarioId(usuarioId)
                .tipoEvento(tipo)
                .productoId(productoId)
                .cantidad(cantidad)
                .fechaEvento(LocalDateTime.now())
                .ipOrigen(RequestUtils.getClientIp(httpRequest))
                .build();
        carEventRepo.save(evento);
    }

    // MÉTODOS PRINCIPALES

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

        // 5. Lógica para actualizarSoloDatos cantidad
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

        // Registro analítico
        registrarEvento(usuarioId, TipoEventoCarrito.AGREGAR_PRODUCTO, productoId, cantidad);

        return carMapper.toDto(carritoGuardado);
    }

    @Transactional(readOnly = true)
    public CarritoDTO obtenerCarritoDto(Long usuarioId) {
        Carrito carrito = carritoRepo.findByUsuarioId(usuarioId)
                .orElseGet(() -> crearNuevoCarrito(usuarioId));
        return carMapper.toDto(carrito);
    }

    private Carrito crearNuevoCarrito(Long usuarioId) {
        User usuario = userRepo.findById(usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con ID: " + usuarioId));
        Carrito nuevo = new Carrito();
        nuevo.setUsuario(usuario);
        return carritoRepo.save(nuevo);
    }

    @Transactional
    public CarritoDTO actualizarCantidad(Long productoId, Integer nuevaCantidad) {
        Long usuarioId = SecurityUtils.getUsuarioIdAutenticado();
        Carrito carrito = carritoRepo.findByUsuarioId(usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Carrito no encontrado"));

        // Buscar el ítem específico
        CarritoItem item = carrito.getItems().stream()
                .filter(i -> i.getProducto().getId().equals(productoId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("El producto no está en el carrito"));

        // Validación CISO: Cantidad positiva y Stock disponible
        if (nuevaCantidad <= 0) {
            throw new BusinessLogicException("La cantidad debe ser mayor a cero");
        }
        if (item.getProducto().getStock() < nuevaCantidad) {
            throw new BusinessLogicException("No hay suficiente stock para actualizarSoloDatos a esa cantidad");
        }

        item.setCantidad(nuevaCantidad);

        registrarEvento(usuarioId, TipoEventoCarrito.ACTUALIZAR_CANTIDAD, productoId, nuevaCantidad);

        return carMapper.toDto(carritoRepo.save(carrito));
    }

    // USER: Actualización Masiva
    @Transactional
    public CarritoDTO actualizarMasivo(ActualizarMasivoRequest request) {
        Long usuarioId = SecurityUtils.getUsuarioIdAutenticado();
        Carrito carrito = carritoRepo.findByUsuarioId(usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Carrito no encontrado"));

        for (ItemUpdateDTO update : request.getItems()) {
            CarritoItem item = carrito.getItems().stream()
                    .filter(i -> i.getProducto().getId().equals(update.getProductoId()))
                    .findFirst()
                    .orElseThrow(() -> new ResourceNotFoundException("Producto ID " + update.getProductoId() + " no está en el carrito"));

            if (update.getCantidad() <= 0) {
                carrito.getItems().remove(item); // Se aplica Orphan Removal
            } else {
                if (item.getProducto().getStock() < update.getCantidad()) {
                    throw new BusinessLogicException("Stock insuficiente para: " + item.getProducto().getNombre());
                }
                item.setCantidad(update.getCantidad());
            }
        }
        return carMapper.toDto(carritoRepo.save(carrito));
    }

    @Transactional
    public CarritoDTO eliminarProducto(Long productoId) {
        Long usuarioId = SecurityUtils.getUsuarioIdAutenticado();
        Carrito carrito = carritoRepo.findByUsuarioId(usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Carrito no encontrado"));

        // Eliminar el ítem de la lista (orphanRemoval = true se encargará de borrarlo de la BD)
        carrito.getItems().removeIf(item -> item.getProducto().getId().equals(productoId));

        registrarEvento(usuarioId, TipoEventoCarrito.QUITAR_PRODUCTO, productoId, 0);

        return carMapper.toDto(carritoRepo.save(carrito));
    }

    @Transactional
    public void limpiarCarrito() {
        Long usuarioId = SecurityUtils.getUsuarioIdAutenticado();
        Carrito carrito = carritoRepo.findByUsuarioId(usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Carrito no encontrado"));

        carrito.getItems().clear();

        // Registro analítico
        registrarEvento(usuarioId, TipoEventoCarrito.VACIAR_CARRITO, null, 0);

        carritoRepo.save(carrito);
    }

    // ADMIN: Listar todos los carritos (Monitoreo de inventario "bloqueado")
    @Transactional(readOnly = true)
    public List<CarritoDTO> listarTodosLosCarritos() {
        return carritoRepo.findAll().stream()
                .map(carMapper::toDto)
                .collect(Collectors.toList());
    }

    // ADMIN: Ver carrito de un usuario específico (Soporte técnico)
    @Transactional(readOnly = true)
    public CarritoDTO obtenerCarritoPorUsuarioAdmin(Long usuarioId) {
        Carrito carrito = carritoRepo.findByUsuarioId(usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("No se encontró carrito para el usuario con ID: " + usuarioId));
        return carMapper.toDto(carrito);
    }

    @Transactional
    public int limpiarCarritosInactivos(int horasInactividad) {
        LocalDateTime limite = LocalDateTime.now().minusHours(horasInactividad);
        List<Carrito> carritosObsoletos = carritoRepo.findByFechaUltimaActualizacionBefore(limite);

        List<Carrito> carritosALimpiar = carritosObsoletos.stream()
                .filter(c -> !c.getItems().isEmpty())
                .peek(c -> c.getItems().clear()) // Limpiamos la lista (aquí actúa el orphanRemoval)
                .collect(Collectors.toList());

        if (!carritosALimpiar.isEmpty()) {
            carritoRepo.saveAll(carritosALimpiar);
        }

        return carritosALimpiar.size();
    }
}