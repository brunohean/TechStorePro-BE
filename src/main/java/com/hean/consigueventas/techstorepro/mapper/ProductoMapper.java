package com.hean.consigueventas.techstorepro.mapper;

import com.hean.consigueventas.techstorepro.dto.producto.ImagenProductoDTO;
import com.hean.consigueventas.techstorepro.dto.producto.ProductoCatalogoDTO;
import com.hean.consigueventas.techstorepro.dto.producto.ProductoDTO;
import com.hean.consigueventas.techstorepro.entity.Producto;
import com.hean.consigueventas.techstorepro.entity.media.ImagenProducto;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ProductoMapper {
    ProductoDTO toDto(Producto producto);
    Producto toEntity(ProductoDTO productoDto);
    List<ProductoDTO> toDtoList(List<Producto> productos);
    ImagenProductoDTO toImagenDto(ImagenProducto imagen);

    // --- NUEVO: Mapeo para el Catálogo ---

    @Mapping(target = "imagenPrincipalUrl", expression = "java(obtenerUrlPrincipal(producto))")
    ProductoCatalogoDTO toCatalogoDto(Producto producto);

    List<ProductoCatalogoDTO> toCatalogoDtoList(List<Producto> productos);

    // Método utilitario para encontrar la foto de portada
    default String obtenerUrlPrincipal(Producto producto) {
        if (producto.getImagenes() == null || producto.getImagenes().isEmpty()) {
            return null; // O podrías retornar un String con la URL de una imagen "No Image Found"
        }

        // Buscamos la imagen marcada como principal
        return producto.getImagenes().stream()
                .filter(ImagenProducto::isEsPrincipal)
                .map(ImagenProducto::getUrlPublica)
                .findFirst()
                // Fallback de seguridad: Si ninguna fue marcada como principal, tomamos la primera
                .orElse(producto.getImagenes().get(0).getUrlPublica());
    }

    // Inyecta los datos del DTO, ignorando nulos y protegiendo campos críticos
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true) // NUNCA sobrescribir el ID
    @Mapping(target = "imagenes", ignore = true) // NUNCA tocar la galería de imágenes desde el JSON plano
    void actualizarEntidadDesdeDto(ProductoDTO dto, @MappingTarget Producto entidad);
}
