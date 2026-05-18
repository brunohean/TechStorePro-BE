package com.hean.consigueventas.techstorepro.mapper;

import com.hean.consigueventas.techstorepro.dto.pedido.DetallePedidoDTO;
import com.hean.consigueventas.techstorepro.dto.pedido.PedidoDTO;
import com.hean.consigueventas.techstorepro.dto.pedido.PedidoEstadoLogDTO;
import com.hean.consigueventas.techstorepro.entity.pedido.PedidoDetalle;
import com.hean.consigueventas.techstorepro.entity.pedido.Pedido;
import com.hean.consigueventas.techstorepro.entity.pedido.PedidoEstadoLog;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface PedidoMapper {

    @Mapping(target = "productoId", source = "producto.id")
    @Mapping(target = "productoNombre", source = "producto.nombre")
    @Mapping(target = "subtotal", expression = "java(detalle.getPrecioUnitario() * detalle.getCantidad())")
    DetallePedidoDTO toDto(PedidoDetalle detalle);

    @Mapping(target = "detalles", source = "detalles")
    PedidoDTO toDto(Pedido pedido);


    PedidoEstadoLogDTO toLogDto(PedidoEstadoLog log);

    List<PedidoEstadoLogDTO> toLogDtoList(List<PedidoEstadoLog> logs);
}
