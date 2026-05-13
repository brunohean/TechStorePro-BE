package com.hean.consigueventas.techstorepro.mapper;

import com.hean.consigueventas.techstorepro.dto.producto.*;
import com.hean.consigueventas.techstorepro.entity.Producto;
import com.hean.consigueventas.techstorepro.entity.media.ImagenProducto;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ProductoMapper {

    // --- Mapeos de Salida (Entidad -> DTO) ---

    @Mapping(target = "categoriaNombre", source = "categoria.nombre")
    ProductoUserDetalleDTO toUserDetalleDto(Producto producto);

    ProductoAdminDetalleDTO toAdminDetalleDto(Producto producto);

    @Mapping(target = "categoriaNombre", source = "producto.categoria.nombre")
    @Mapping(target = "imagenPrincipalUrl", expression = "java(obtenerUrlPrincipal(producto))")
    ProductoCatalogoDTO toCatalogoDto(Producto producto);

    @Mapping(target = "categoriaId", source = "categoria.id")
    @Mapping(target = "imagenPrincipalUrl", expression = "java(obtenerUrlPrincipal(producto))")
    ProductoInventarioDTO toInventarioDto(Producto producto);

    ImagenProductoReadDTO toImagenProductoReadDto(ImagenProducto imagen);
    ImagenProductoCreateDTO toImagenProductoCreateDto(ImagenProducto imagen);

    List<ProductoAdminDetalleDTO> toDtoList(List<Producto> productos);
    List<ProductoCatalogoDTO> toCatalogoDtoList(List<Producto> productos);


    // --- Mapeos de Entrada (DTO -> Entidad) ---

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "activo", constant = "true") // Por defecto al crear
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "categoria", ignore = true) // Se asigna manualmente en el Service mediante el ID
    @Mapping(target = "imagenes", ignore = true)  // Se gestiona mediante el servicio de Cloudinary
    Producto toEntity(ProductoCreateDTO dto);


    // --- Metodos Utilitarios ---

    // Método utilitario para encontrar la foto de portada
    default String obtenerUrlPrincipal(Producto producto) {
        if (producto.getImagenes() == null || producto.getImagenes().isEmpty()) {
            return null; // O podrías retornar un String con la URL de una imagen "No Image Found"
        }

        // Buscamos la imagen marcada como principal
        return producto.getImagenes().stream()
                .filter(ImagenProducto::getEsPrincipal)
                .map(ImagenProducto::getUrlPublica)
                .findFirst()
                .orElse(producto.getImagenes().get(0).getUrlPublica()); // Fallback de seguridad: Si ninguna fue marcada como principal, tomamos la primera
    }

    // Metodo Utilitario para inyectar los datos del DTO, ignorando nulos y protegiendo campos críticos para productos existentes
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true) // NUNCA sobrescribir el ID
    @Mapping(target = "imagenes", ignore = true) // NUNCA tocar la galería de imágenes desde el JSON plano
    @Mapping(target = "categoria", ignore = true)
    @Mapping(target = "version", source = "version") // Mapeamos la versión para el bloqueo optimista
    void actualizarEntidadDesdeDto(ProductoUpdateDTO dto, @MappingTarget Producto entidad);
}
