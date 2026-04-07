package com.hean.consigueventas.techstorepro.service;

import com.hean.consigueventas.techstorepro.entity.Carrito;
import com.hean.consigueventas.techstorepro.entity.DetallePedido;
import com.hean.consigueventas.techstorepro.entity.EstadoPedido;
import com.hean.consigueventas.techstorepro.entity.Pedido;
import com.hean.consigueventas.techstorepro.repository.CarritoRepository;
import com.hean.consigueventas.techstorepro.repository.PedidoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class PedidoService {

    private final PedidoRepository pedidoRepo;
    private final CarritoRepository carritoRepo;

    public PedidoService(PedidoRepository pedidoRepository, CarritoRepository carritoRepository) {
        this.pedidoRepo = pedidoRepository;
        this.carritoRepo = carritoRepository;
    }

    @Transactional
    public Pedido procesarPedido(Long usuarioId) {
        Carrito carrito = carritoRepo.findByUsuarioId(usuarioId)
                .orElseThrow(() -> new RuntimeException("Carrito vacío o no encontrado"));

        Pedido pedido = new Pedido();
        pedido.setUsuario(carrito.getUsuario());

        // Convertir CarritoItem a DetallePedido
        List<DetallePedido> detalles = carrito.getItems().stream().map(item -> {
            DetallePedido detalle = new DetallePedido();
            detalle.setPedido(pedido);
            detalle.setProducto(item.getProducto());
            detalle.setCantidad(item.getCantidad());
            detalle.setPrecioUnitario(item.getProducto().getPrecio());
            return detalle;
        }).collect(Collectors.toList());

        pedido.setDetalles(detalles);
        pedido.setTotal(detalles.stream().mapToDouble(d -> d.getPrecioUnitario() * d.getCantidad()).sum());
        pedido.setEstado(EstadoPedido.PENDIENTE);

        // Limpiar el carrito después de la compra
        carrito.getItems().clear();
        carritoRepo.save(carrito);

        return pedidoRepo.save(pedido);
    }
}
