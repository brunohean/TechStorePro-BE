package com.hean.consigueventas.techstorepro.service;

import com.hean.consigueventas.techstorepro.dto.pedido.CambiarEstadoRequest;
import com.hean.consigueventas.techstorepro.dto.pedido.CancelarPedidoRequest;
import com.hean.consigueventas.techstorepro.dto.pedido.PedidoDTO;
import com.hean.consigueventas.techstorepro.dto.pedido.PedidoEstadoLogDTO;
import com.hean.consigueventas.techstorepro.entity.*;
import com.hean.consigueventas.techstorepro.entity.carrito.Carrito;
import com.hean.consigueventas.techstorepro.entity.carrito.CarritoEvento;
import com.hean.consigueventas.techstorepro.entity.carrito.CarritoItem;
import com.hean.consigueventas.techstorepro.entity.carrito.TipoEventoCarrito;
import com.hean.consigueventas.techstorepro.entity.pedido.*;
import com.hean.consigueventas.techstorepro.exception.custom.BusinessLogicException;
import com.hean.consigueventas.techstorepro.exception.custom.ResourceNotFoundException;
import com.hean.consigueventas.techstorepro.mapper.PedidoMapper;
import com.hean.consigueventas.techstorepro.repository.*;
import com.hean.consigueventas.techstorepro.repository.carrito.CarritoEventoRepository;
import com.hean.consigueventas.techstorepro.repository.carrito.CarritoRepository;
import com.hean.consigueventas.techstorepro.repository.pedido.PedidoEstadoLogRepository;
import com.hean.consigueventas.techstorepro.repository.pedido.PedidoRepository;
import com.hean.consigueventas.techstorepro.security.SecurityUtils;
import com.hean.consigueventas.techstorepro.utils.RequestUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor // Genera el constructor para los campos 'final'
@Slf4j
public class PedidoService {

    private final PedidoRepository pedidoRepo;
    private final PedidoEstadoLogRepository logRepo;
    private final CarritoRepository carritoRepo;
    private final CarritoEventoRepository carEventRepo;
    private final CarritoService carritoService;
    private final ProductoRepository productoRepo;
    private final PedidoMapper pedidoMapper;
    private final HttpServletRequest httpRequest;

    @Transactional
    public PedidoDTO procesarCheckout(PedidoDTO pedidoDTO) {
        Long usuarioId = SecurityUtils.getUsuarioIdAutenticado();

        // 1. Recuperar el Carrito
        Carrito carrito = carritoRepo.findByUsuarioId(usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Carrito no encontrado"));

        if (carrito.getItems().isEmpty()) {
            throw new BusinessLogicException("El carrito está vacío.");
        }

        // Registro de Analytics
        CarritoEvento evento = CarritoEvento.builder()
                .usuarioId(usuarioId)
                .tipoEvento(TipoEventoCarrito.INTENTO_CHECKOUT)
                .productoId(null)
                .cantidad(0)
                .fechaEvento(LocalDateTime.now())
                .ipOrigen(RequestUtils.getClientIp(httpRequest))
                .build();
        carEventRepo.save(evento);

        // 2. Instancia de Pedido y Datos de Envío
        Pedido pedido = new Pedido();
        pedido.setUsuario(carrito.getUsuario());
        pedido.setFecha(LocalDateTime.now());
        pedido.setEstado(EstadoPedido.PENDIENTE);
        pedido.setClienteNombre(pedidoDTO.getClienteNombre());
        pedido.setCelular(pedidoDTO.getCelular());
        pedido.setDireccion(pedidoDTO.getDireccion());

        // 3. Gobernanza (PedidoControl)
        PedidoControl control = PedidoControl.builder()
                .pedido(pedido)
                .accion("CHECKOUT_EXITOSO")
                .detalle("Compra finalizada desde el carrito web.")
                .fechaUltimoCambioEstado(LocalDateTime.now())
                .ipRegistro(RequestUtils.getClientIp(httpRequest))
                .visibleParaUsuario(true)
                .visibleParaAdmin(true)
                .build();
        pedido.setControl(control);

        // 4. Procesar Ítems con Auto-Saneamiento del Carrito
        List<DetallePedido> detalles = new ArrayList<>();

        // Iteramos sobre una copia para permitir modificaciones seguras
        for (CarritoItem item : new ArrayList<>(carrito.getItems())) {
            Producto producto = productoRepo.findById(item.getProducto().getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado"));

            // ESCENARIO 1: Producto Agotado o Inactivo -> Remover del carrito
            if (producto.getActivo() == null || !producto.getActivo() || producto.getStock() <= 0) {
                carrito.getItems().remove(item);
                carritoRepo.save(carrito);
                throw new BusinessLogicException("El producto '" + producto.getNombre() +
                        "' ya no está disponible y ha sido removido de su carrito.");
            }

            // ESCENARIO 2: Stock Insuficiente -> Ajustar cantidad en el carrito
            if (item.getCantidad() > producto.getStock()) {
                int stockDisponible = producto.getStock();
                item.setCantidad(stockDisponible);
                carritoRepo.save(carrito);
                throw new BusinessLogicException("Stock insuficiente para '" + producto.getNombre() +
                        "'. Hemos ajustado su carrito automáticamente al stock disponible: " +
                        stockDisponible + " unidades.");
            }

            // Si pasa validaciones: Descuento de inventario (Bloqueo Optimista @Version)
            producto.setStock(producto.getStock() - item.getCantidad());
            productoRepo.save(producto);

            // Crear Snapshot del Detalle
            DetallePedido detalle = new DetallePedido();
            detalle.setPedido(pedido);
            detalle.setProducto(producto);
            detalle.setCantidad(item.getCantidad());
            detalle.setPrecioUnitario(producto.getPrecio());
            detalles.add(detalle);
        }

        pedido.setDetalles(detalles);
        pedido.setTotal(detalles.stream().mapToDouble(d -> d.getPrecioUnitario() * d.getCantidad()).sum());

        // 5. Persistencia Final
        Pedido pedidoGuardado = pedidoRepo.save(pedido);

        // 6. Limpiar Carrito tras éxito total
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
    public PedidoDTO actualizarEstado(Long id, CambiarEstadoRequest request, String ipOrigen) {
        Pedido pedido = pedidoRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pedido no encontrado"));

        EstadoPedido estadoAnterior = pedido.getEstado();
        EstadoPedido estadoNuevo = request.getNuevoEstado();

        // 1. Actualizar el pedido
        pedido.setEstado(estadoNuevo);

        // 2. Actualizar PedidoControl (Última foto)
        PedidoControl control = pedido.getControl();
        control.setFechaUltimoCambioEstado(LocalDateTime.now());
        control.setDetalle(request.getMotivo());
        control.setAccion("CAMBIO_ESTADO: " + estadoNuevo);
        control.setIpRegistro(ipOrigen); // Actualizamos la última IP de gestión
        // No necesitamos llamar a controlRepo.save() si tenemos CascadeType.ALL en Pedido

        // 3. Crear el LOG HISTÓRICO (La huella digital/log historico)
        PedidoEstadoLog log = PedidoEstadoLog.builder()
                .pedido(pedido)
                .estadoAnterior(estadoAnterior)
                .estadoNuevo(estadoNuevo)
                .fechaCambio(LocalDateTime.now())
                .responsable(SecurityUtils.getUsernameAutenticado())
                .motivo(request.getMotivo())
                .ipOrigen(ipOrigen) // Se obtiene de HttpServletRequest
                .build();

        logRepo.save(log);

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
    public PedidoDTO cancelarPedidoPropio(Long id, CancelarPedidoRequest request, String ipOrigen) {
        Pedido pedido = pedidoRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pedido no encontrado"));

        // 1. CISO Check: Validar propiedad (Mantenemos tu método que está perfecto)
        if (!SecurityUtils.esDueno(pedido.getUsuario().getId())) {
            throw new AccessDeniedException("No tienes permiso para cancelar este pedido.");
        }

        // 2. Validar estado permitido
        if (pedido.getEstado() != EstadoPedido.PENDIENTE) {
            throw new BusinessLogicException("Solo se pueden cancelar pedidos en estado PENDIENTE.");
        }

        EstadoPedido estadoAnterior = pedido.getEstado();

        // 3. Devolución de Stock (RF-BE-05) - ¡Vital mantener tu lógica!
        for (DetallePedido detalle : pedido.getDetalles()) {
            Producto producto = detalle.getProducto();
            producto.setStock(producto.getStock() + detalle.getCantidad());
            productoRepo.save(producto); // Reabastecemos el inventario
        }

        // 4. Actualizar estado principal
        pedido.setEstado(EstadoPedido.CANCELADO);

        // 5. Actualizar PedidoControl (La "Foto Actual")
        PedidoControl control = pedido.getControl();
        control.setMotivoCancelacion(request.getMotivo());
        control.setFechaUltimoCambioEstado(LocalDateTime.now());
        // -> Añadimos estos dos campos nuevos para la auditoría
        control.setAccion("CANCELACION_USUARIO");
        control.setIpRegistro(ipOrigen);

        // 6. Registrar el Historial (La huella inmutable que creamos hoy)
        PedidoEstadoLog log = PedidoEstadoLog.builder()
                .pedido(pedido)
                .estadoAnterior(estadoAnterior)
                .estadoNuevo(EstadoPedido.CANCELADO)
                .fechaCambio(LocalDateTime.now())
                .responsable(SecurityUtils.getUsernameAutenticado()) // Registramos que fue el cliente
                .motivo(request.getMotivo())
                .ipOrigen(ipOrigen)
                .build();

        logRepo.save(log);

        return pedidoMapper.toDto(pedidoRepo.save(pedido));
    }

    @Transactional(readOnly = true)
    public List<PedidoEstadoLogDTO> obtenerHistorial(Long pedidoId) {
        if (!pedidoRepo.existsById(pedidoId)) {
            throw new ResourceNotFoundException("Pedido no encontrado");
        }
        List<PedidoEstadoLog> logs = logRepo.findByPedidoIdOrderByFechaCambioDesc(pedidoId);
        return pedidoMapper.toLogDtoList(logs); // Aquí se usa el método que causaba el warning
    }

    /**
     * Hito Logístico 1: Despacho desde el Almacén
     */
    @Transactional
    public PedidoDTO marcarComoEnviado(Long pedidoId) {
        Pedido pedido = pedidoRepo.findById(pedidoId)
                .orElseThrow(() -> new ResourceNotFoundException("Pedido no encontrado con ID: " + pedidoId));

        EstadoPedido estadoAnterior = pedido.getEstado();

        // Validación de Estado (Business Rule)
        if (estadoAnterior != EstadoPedido.PAGADO && estadoAnterior != EstadoPedido.PREPARANDO) {
            throw new BusinessLogicException("Solo los pedidos en estado PAGADO/PREPARANDO pueden ser despachados. Estado actual: " + estadoAnterior);
        }

        pedido.setEstado(EstadoPedido.ENVIADO);

        // Utilizamos el motor unificado (True para que el cliente lo vea en su perfil)
        registrarAuditoria(pedido, estadoAnterior, EstadoPedido.ENVIADO, "DESPACHO", "El pedido ha salido de nuestro almacén central.", true);
        log.info("📦 Pedido {} marcado como ENVIADO", pedidoId);
        return pedidoMapper.toDto(pedidoRepo.save(pedido));
    }

    /**
     * Hito Logístico 2: Entrega final al Cliente
     */
    @Transactional
    public PedidoDTO marcarComoEntregado(Long pedidoId) {
        Pedido pedido = pedidoRepo.findById(pedidoId)
                .orElseThrow(() -> new ResourceNotFoundException("Pedido no encontrado con ID: " + pedidoId));

        EstadoPedido estadoAnterior = pedido.getEstado();

        // Validación de Estado (Business Rule)
        if (estadoAnterior != EstadoPedido.ENVIADO) {
            throw new BusinessLogicException("Un pedido debe ser ENVIADO antes de poder ser ENTREGADO.");
        }

        pedido.setEstado(EstadoPedido.ENTREGADO);

        // Utilizamos el motor unificado
        registrarAuditoria(pedido, estadoAnterior, EstadoPedido.ENTREGADO, "ENTREGA_FINAL", "Pedido recibido conforme por el cliente.", true);
        log.info("✅ Pedido {} marcado como ENTREGADO.", pedidoId);
        return pedidoMapper.toDto(pedidoRepo.save(pedido));
    }

    /**
     * Motor de Trazabilidad Unificado
     * Actualiza el registro de control del pedido y guarda el historial inmutable.
     */
    private void registrarAuditoria(Pedido pedido, EstadoPedido estadoAnterior, EstadoPedido estadoNuevo, String accion, String detalle, boolean visibleParaUsuario) {

        // 1. Obtener la IP real de la petición usando el RequestUtils que ya tienes
        String ipReal = RequestUtils.getClientIp(httpRequest);

        // 2. Actualizar el registro "en vivo" (PedidoControl)
        PedidoControl control = pedido.getControl();
        control.setAccion(accion);
        control.setDetalle(detalle);
        control.setFechaAccion(LocalDateTime.now());
        control.setFechaUltimoCambioEstado(LocalDateTime.now());
        control.setIpRegistro(ipReal);
        control.setVisibleParaUsuario(visibleParaUsuario);
        // Al estar atado al pedido por la relación OneToOne, se guardará en cascada

        // 3. Crear la "fotografía inmutable" en el historial (PedidoEstadoLog)
        PedidoEstadoLog logHistorial = PedidoEstadoLog.builder()
                .pedido(pedido)
                .estadoAnterior(estadoAnterior)
                .estadoNuevo(estadoNuevo)
                .fechaCambio(LocalDateTime.now())
                .responsable(SecurityUtils.getUsernameAutenticado())
                .motivo(detalle)
                .ipOrigen(ipReal)
                .build();

        logRepo.save(logHistorial);
    }
}
