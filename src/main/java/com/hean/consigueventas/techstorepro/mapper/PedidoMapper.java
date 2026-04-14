package com.hean.consigueventas.techstorepro.mapper;

import com.hean.consigueventas.techstorepro.dto.pedido.DetallePedidoDTO;
import com.hean.consigueventas.techstorepro.dto.pedido.PedidoDTO;
import com.hean.consigueventas.techstorepro.entity.pedido.DetallePedido;
import com.hean.consigueventas.techstorepro.entity.pedido.Pedido;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface PedidoMapper {

    @Mapping(target = "productoId", source = "producto.id")
    @Mapping(target = "productoNombre", source = "producto.nombre")
    @Mapping(target = "subtotal", expression = "java(detalle.getPrecioUnitario() * detalle.getCantidad())")
    DetallePedidoDTO toDto(DetallePedido detalle);

    @Mapping(target = "detalles", source = "detalles")
    PedidoDTO toDto(Pedido pedido);
}
