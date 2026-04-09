package com.hean.consigueventas.techstorepro.mapper;

import com.hean.consigueventas.techstorepro.dto.CarritoDTO;
import com.hean.consigueventas.techstorepro.dto.CarritoItemDTO;
import com.hean.consigueventas.techstorepro.entity.Carrito;
import com.hean.consigueventas.techstorepro.entity.CarritoItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.List;

@Mapper(componentModel = "spring") // Permite inyectarlo con @Autowired
public interface CarritoMapper {

    @Mapping(target = "username", source = "usuario.username")
    @Mapping(target = "total", source = "items", qualifiedByName = "calcularTotal")
    CarritoDTO toDto(Carrito carrito);

    @Mapping(target = "productoId", source = "producto.id")
    @Mapping(target = "productoNombre", source = "producto.nombre")
    @Mapping(target = "precioUnitario", source = "producto.precio")
    CarritoItemDTO toItemDto(CarritoItem item);

    @Named("calcularTotal")
    default Double calcularTotal(List<CarritoItem> items) {
        if (items == null) return 0.0;
        return items.stream()
                .mapToDouble(item -> item.getProducto().getPrecio() * item.getCantidad())
                .sum();
    }
}