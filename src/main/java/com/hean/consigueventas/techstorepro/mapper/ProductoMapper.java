package com.hean.consigueventas.techstorepro.mapper;

import com.hean.consigueventas.techstorepro.dto.ProductoDTO;
import com.hean.consigueventas.techstorepro.entity.Producto;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ProductoMapper {
    ProductoDTO toDto(Producto producto);
    Producto toEntity(ProductoDTO productoDto);
    List<ProductoDTO> toDtoList(List<Producto> productos);
}
