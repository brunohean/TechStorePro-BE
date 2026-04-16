package com.hean.consigueventas.techstorepro.service;

import com.hean.consigueventas.techstorepro.dto.carrito.CarritoDTO;
import com.hean.consigueventas.techstorepro.dto.carrito.CarritoItemDTO;
import com.hean.consigueventas.techstorepro.dto.pedido.PedidoDTO;
import com.hean.consigueventas.techstorepro.entity.*;
import com.hean.consigueventas.techstorepro.entity.pedido.DetallePedido;
import com.hean.consigueventas.techstorepro.entity.pedido.EstadoPedido;
import com.hean.consigueventas.techstorepro.entity.pedido.Pedido;
import com.hean.consigueventas.techstorepro.entity.pedido.PedidoControl;
import com.hean.consigueventas.techstorepro.exception.custom.BusinessLogicException;
import com.hean.consigueventas.techstorepro.exception.custom.ResourceNotFoundException;
import com.hean.consigueventas.techstorepro.mapper.PedidoMapper;
import com.hean.consigueventas.techstorepro.repository.CarritoRepository;
import com.hean.consigueventas.techstorepro.repository.PedidoControlRepository;
import com.hean.consigueventas.techstorepro.repository.PedidoRepository;
import com.hean.consigueventas.techstorepro.repository.ProductoRepository;
import com.hean.consigueventas.techstorepro.security.SecurityUtils;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PedidoService {

    private final PedidoRepository pedidoRepo;
    private final CarritoRepository carritoRepo;
    private final CarritoService carritoService;
    private final ProductoRepository productoRepo;
    private final PedidoControlRepository controlRepo;
    private final PedidoMapper pedidoMapper;

    public PedidoService(PedidoRepository pedidoRepository, CarritoRepository carritoRepository,
                         ProductoRepository productoRepository, PedidoControlRepository controlRepository,
                         CarritoService carritoService, PedidoMapper pedidoMapper1) {
        this.pedidoRepo = pedidoRepository;
        this.carritoRepo = carritoRepository;
        this.carritoService = carritoService;
        this.productoRepo = productoRepository;
        this.controlRepo = controlRepository;
        this.pedidoMapper = pedidoMapper1;
    }

    @Transactional
    public PedidoDTO procesarCheckout() {
        Long usuarioId = SecurityUtils.getUsuarioIdAutenticado();

        // 1. Recuperar el Carrito (Aseguramos que el usuario existe y tiene items)
        Carrito carrito = carritoRepo.findByUsuarioId(usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Carrito no encontrado"));

        if (carrito.getItems().isEmpty()) {
            throw new BusinessLogicException("El carrito está vacío.");
        }

        // 2. Crear instancia de Pedido
        Pedido pedido = new Pedido();
        pedido.setUsuario(carrito.getUsuario()); // Usamos el usuario ya cargado en el carrito
        pedido.setFecha(LocalDateTime.now());
        pedido.setEstado(EstadoPedido.PENDIENTE);

        // 3. Configurar PedidoControl usando Builder (Gobernanza y Auditoría)
        // Gracias a @MapsId, la PK de este objeto será la misma que la del Pedido
        PedidoControl control = PedidoControl.builder()
                .pedido(pedido) // Importante para @MapsId
                .accion("CHECKOUT_EXITOSO")
                .detalle("Compra finalizada desde el carrito web.")
                .fechaUltimoCambioEstado(LocalDateTime.now())
                .ipRegistro("127.0.0.1")
                .visibleParaUsuario(true)
                .visibleParaAdmin(true)
                .build();

        pedido.setControl(control);

        // 4. Procesar Ítems y Snapshot de Precios
        List<DetallePedido> detalles = carrito.getItems().stream().map(item -> {
            Producto producto = item.getProducto();

            if (producto.getStock() < item.getCantidad()) {
                throw new BusinessLogicException("Stock insuficiente para: " + producto.getNombre());
            }

            // Descuento de inventario
            producto.setStock(producto.getStock() - item.getCantidad());
            productoRepo.save(producto);

            // Creamos el detalle (Asegúrate de tener un constructor o usar setters)
            DetallePedido detalle = new DetallePedido();
            detalle.setPedido(pedido);
            detalle.setProducto(producto);
            detalle.setCantidad(item.getCantidad());
            detalle.setPrecioUnitario(producto.getPrecio()); // Snapshot del precio actual
            return detalle;
        }).collect(Collectors.toList());

        pedido.setDetalles(detalles);
        pedido.setTotal(detalles.stream().mapToDouble(d -> d.getPrecioUnitario() * d.getCantidad()).sum());

        // 5. Guardar Pedido (Cascada guardará el Control y los Detalles)
        Pedido pedidoGuardado = pedidoRepo.save(pedido);

        // 6. Limpiar Carrito
        carritoService.limpiarCarrito();

        return pedidoMapper.toDto(pedidoGuardado);
    }

    // A.1 LISTAR TODOS (Solo para el Admin)
    @Transactional(readOnly = true)
    public List<PedidoDTO> listarTodos() {
        return pedidoRepo.findAllVisibleForAdmin().stream()
                .map(pedidoMapper::toDto)
                .collect(Collectors.toList());
    }

    // A.2 LISTAR TODOS (Solo Usuario)
    @Transactional(readOnly = true)
    public List<PedidoDTO> obtenerMisPedidos() {
        Long usuarioId = SecurityUtils.getUsuarioIdAutenticado();
        // Cambiamos el método genérico por tu nueva consulta filtrada
        List<Pedido> pedidos = pedidoRepo.findVisibleByUsuarioId(usuarioId);
        return pedidos.stream()
                .map(pedidoMapper::toDto)
                .collect(Collectors.toList());
    }

    // B. OBTENER POR ID (Con validación de propiedad)
    @Transactional(readOnly = true)
    public PedidoDTO obtenerPorId(Long id) {
        Pedido pedido = pedidoRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pedido no encontrado"));

        // CISO CHECK: O es Admin o es el dueño del pedido
        if (!SecurityUtils.esAdmin() && !SecurityUtils.esDueno(pedido.getUsuario().getId())) {
            throw new AccessDeniedException("No tienes permiso para ver este pedido.");
        }

        return pedidoMapper.toDto(pedido);
    }

    // C. ACTUALIZAR ESTADO (Solo Admin)
    @Transactional
    public PedidoDTO actualizarEstado(Long id, EstadoPedido nuevoEstado) {
        Pedido pedido = pedidoRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pedido no encontrado"));

        // Lógica de negocio: No se puede cancelar algo ya entregado
        if (pedido.getEstado() == EstadoPedido.ENTREGADO && nuevoEstado == EstadoPedido.CANCELADO) {
            throw new BusinessLogicException("No se puede cancelar un pedido que ya fue entregado.");
        }

        pedido.setEstado(nuevoEstado);
        return pedidoMapper.toDto(pedidoRepo.save(pedido));
    }

    // D. ELIMINAR (OCULTAR) PEDIDO (Solo Usuario)
    @Transactional
    public void ocultarPedidoParaUsuario(Long id) {
        Pedido pedido = pedidoRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pedido no encontrado"));

        // CISO Check: No puedes ocultar lo que no es tuyo
        if (!SecurityUtils.esDueno(pedido.getUsuario().getId())) {
            throw new AccessDeniedException("No tienes permiso para modificar este registro.");
        }

        // Cambiamos la visibilidad en la tabla de Control
        pedido.getControl().setVisibleParaUsuario(false);

        // Al ser una relación con CascadeType.ALL, se guarda automáticamente al terminar el método
        pedidoRepo.save(pedido);
    }

    // E. ELIMINAR (OCULTAR) PEDIDO (Solo Admin)
    @Transactional
    public void ocultarPedidoParaAdmin(Long id) {
        Pedido pedido = pedidoRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pedido no encontrado"));

        // Cambiamos la visibilidad para el reporte administrativo
        pedido.getControl().setVisibleParaAdmin(false);

        pedidoRepo.save(pedido);
    }

    @Transactional
    public PedidoDTO cancelarPedidoPropio(Long id, String motivo) {
        Pedido pedido = pedidoRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pedido no encontrado"));

        // CISO Check: Validar propiedad y estado
        if (!SecurityUtils.esDueno(pedido.getUsuario().getId())) {
            throw new AccessDeniedException("No tienes permiso para cancelar este pedido.");
        }

        if (pedido.getEstado() != EstadoPedido.PENDIENTE) {
            throw new BusinessLogicException("Solo se pueden cancelar pedidos en estado PENDIENTE.");
        }

        // Devolución de Stock (RF-BE-05)
        for (DetallePedido detalle : pedido.getDetalles()) {
            Producto producto = detalle.getProducto();
            producto.setStock(producto.getStock() + detalle.getCantidad());
            productoRepo.save(producto);
        }

        // Actualizar estados
        pedido.setEstado(EstadoPedido.CANCELADO);
        pedido.getControl().setMotivoCancelacion(motivo);
        pedido.getControl().setFechaUltimoCambioEstado(LocalDateTime.now());

        return pedidoMapper.toDto(pedidoRepo.save(pedido));
    }


    private void registrarAuditoria(Pedido pedido, String accion, String detalle) {
        PedidoControl control = PedidoControl.builder()
                .pedido(pedido)
                .accion(accion)
                .detalle(detalle)
                .fechaAccion(LocalDateTime.now())
                .ipRegistro("127.0.0.1") // En prod usaríamos HttpServletRequest
                .visibleParaAdmin(true)
                .visibleParaUsuario(true)
                .build();
        controlRepo.save(control);
    }
}
