package com.hean.consigueventas.techstorepro.service;

import com.hean.consigueventas.techstorepro.dto.producto.*;
import com.hean.consigueventas.techstorepro.entity.Categoria;
import com.hean.consigueventas.techstorepro.entity.Producto;
import com.hean.consigueventas.techstorepro.entity.media.ImagenProducto;
import com.hean.consigueventas.techstorepro.entity.media.StorageProvider;
import com.hean.consigueventas.techstorepro.exception.custom.BusinessLogicException;
import com.hean.consigueventas.techstorepro.exception.custom.ResourceNotFoundException;
import com.hean.consigueventas.techstorepro.mapper.ProductoMapper;
import com.hean.consigueventas.techstorepro.repository.CategoriaRepository;
import com.hean.consigueventas.techstorepro.repository.ProductoRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor // Genera el constructor para los campos 'final'
@Slf4j // Uso de log y elementos de consola
public class ProductoService {

    private final ProductoRepository proRepo;
    private final CategoriaRepository catRepo;
    private final ProductoMapper proMapper;
    private final CloudinaryService cloudService;

    /*public ProductoService(ProductoRepository productoRepository, ProductoMapper productoMapper) {
        this.proRepo = productoRepository;
        this.proMapper = productoMapper;
    }*/

    // --- 1. ENDPOINT PÚBLICO: Catálogo Web Activos ---
    @Transactional(readOnly = true)
    public Page<ProductoCatalogoDTO> listarCatalogoPublico(Pageable pageable) {
        log.info("Consultando catálogo público paginado - Página: {}, Tamaño: {}",
                pageable.getPageNumber(), pageable.getPageSize()
        );
        Page<Producto> productosPaginados = proRepo.findByActivoTrue(pageable);
        return productosPaginados.map(proMapper::toCatalogoDto);
    }

    // --- 2. ENDPOINT PRIVADO: Panel de Administración ---
    @Transactional(readOnly = true)
    public Page<ProductoInventarioDTO> listarInventarioAdmin(Pageable pageable) {
        log.info("Consultando inventario completo para el panel de administración");
        //List<Producto> todosLosProductos = proRepo.findAll(); -> Eliminar línea en caso no ocurra error
        // findAll() ya soporta Pageable nativamente gracias a JpaRepository
        return proRepo.findAll(pageable).map(proMapper::toInventarioDto);
    }

    // Buscar un producto específico (PÚBLICO - SOLO ACTIVOS)
    @Transactional(readOnly = true)
    public ProductoUserDetalleDTO obtenerPorIdDetalleCatalogo(Long id) {
        Producto producto = proRepo.findByIdAndActivoTrue(id)
                .orElseThrow(() -> new EntityNotFoundException("Producto no encontrado o actualmente inactivo con ID: " + id));
        return proMapper.toUserDetalleDto(producto);
    }

    // Buscar un producto específico (ADMIN)
    @Transactional(readOnly = true)
    public ProductoAdminDetalleDTO obtenerPorIdDetalleInventario(Long id) {
        Producto producto = proRepo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Producto no encontrado con ID: " + id));
        return proMapper.toAdminDetalleDto(producto);
    }

    @Transactional
    public ProductoAdminDetalleDTO crear(ProductoCreateDTO dto, List<MultipartFile> archivos) {
        log.info("Iniciando creación del producto: {}", dto.getNombre());
        // 1. De DTO a Entidad
        Producto producto = proMapper.toEntity(dto);

        //  Asignación manual de la Categoría
        if (dto.getCategoriaId() != null) {
            Categoria cat = catRepo.findById(dto.getCategoriaId())
                    .orElseThrow(() -> new ResourceNotFoundException("Categoría no encontrada"));
            producto.setCategoria(cat);
        }

        // 2. Procesar la galería de imágenes si se enviaron archivos
        if (archivos != null && !archivos.isEmpty()) {
            boolean esPrimeraImagen = true;

            for (MultipartFile archivo : archivos) {
                    // Subimos archivos a Cloudinary usando el Service
                    Map<String, String> datosNube = cloudService.subirImagen(archivo, dto.getNombre());

                    ImagenProducto imagen = ImagenProducto.builder()
                            .nombreArchivo(archivo.getOriginalFilename())
                            .urlPublica(datosNube.get("url"))
                            .providerId(datosNube.get("provider_id"))
                            .storageProvider(StorageProvider.CLOUDINARY)
                            .formato("webp")
                            .esPrincipal(esPrimeraImagen) // Solo la primera será la portada
                            .build();

                    // Helper Method
                    producto.addImagen(imagen);
                    esPrimeraImagen = false;
            }
        }

        // 3. Guardar en Base de Datos (Hibernate guardará el producto y todas sus imágenes en cascada)
        Producto productoGuardado = proRepo.save(producto);
        // 4. Devolver al frontend el producto JSON
        return proMapper.toAdminDetalleDto(productoGuardado);
    }

    @Transactional
    public ProductoAdminDetalleDTO actualizar(ProductoUpdateDTO dto, List<MultipartFile> nuevosArchivos) {
        log.info("Actualizando producto ID: {} con sincronización de imágenes", dto.getId());
        Producto producto = proRepo.findById(dto.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado"));

        // 1. Sincronizar datos básicos (MapStruct) - Hibernate actualiza 'version' al comparar dto.version con producto.version
        proMapper.actualizarEntidadDesdeDto(dto, producto);

        // 2. Sincronizar Categoria por medio de categoriaId
        if (!producto.getCategoria().getId().equals(dto.getCategoriaId())) {
            Categoria nuevaCat = catRepo.findById(dto.getCategoriaId())
                    .orElseThrow(() -> new ResourceNotFoundException("Nueva categoría no encontrada"));
            producto.setCategoria(nuevaCat);
        }

        // 3. Sincronización de Imagenes (Borrado físico y lógico - Eliminar las que no están en la lista de "mantener")
        if (dto.getImagenesMantenerIds() != null) {
            producto.getImagenes().removeIf(img -> {
                boolean eliminar = !dto.getImagenesMantenerIds().contains(img.getId());
                if (eliminar) {
                    cloudService.eliminarImagen(img.getProviderId()); // Borrar de Cloudinary
                }
                return eliminar;
            });
        }

        // 4. Adicionar nuevas imagenes
        if (nuevosArchivos != null && !nuevosArchivos.isEmpty()) {
            for (MultipartFile archivo : nuevosArchivos) {
                Map<String, String> result = cloudService.subirImagen(archivo, dto.getNombre());
                producto.addImagen(ImagenProducto.builder()
                        .urlPublica(result.get("url"))
                        .providerId(result.get("provider_id"))
                        .storageProvider(StorageProvider.CLOUDINARY)
                        .formato("webp")
                        .esPrincipal(false)
                        .build());
            }
        }

        // 5. Establecer ImagenPrincipalId()
        producto.getImagenes().forEach(img ->
                img.setEsPrincipal(img.getId().equals(dto.getImagenPrincipalId()))
        );

        // 6. SaveAndFlush para retornar en el DTO la versión +1 (actualizada)
        return proMapper.toAdminDetalleDto(proRepo.saveAndFlush(producto));
    }

    // Eliminar Producto (Reservado para ADMIN)
    @Transactional
    public void eliminar(Long id) {
        // 1. Buscamos el producto con todas sus relaciones
        Producto producto = proRepo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("No se puede eliminar: Producto no encontrado con ID " + id));

        // 2. BORRADO ECOLÓGICO: Recorremos las imágenes para eliminarlas de la nube
        if (producto.getImagenes() != null) {
            producto.getImagenes().forEach(img -> cloudService.eliminarImagen(img.getProviderId()));
        }

        // 3. Eliminamos de PostgreSQL - (Gracias a CascadeType.ALL, Hibernate también borrará las filas de 'imagenes_producto')
        proRepo.delete(producto);
        log.info("Producto y sus dependencias eliminados con éxito. ID: {}", id);
    }

    /** Gestión C: Borrado de una Imagen en Cloudinary y BD (Ecofriendly) */
    @Transactional
    public void eliminarImagenEspecifica(Long productoId, Long imagenId) {
        Producto producto = proRepo.findById(productoId)
                .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado"));

        // Buscamos la imagen dentro de la lista del producto
        ImagenProducto imagen = producto.getImagenes().stream()
                .filter(img -> img.getId().equals(imagenId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("La imagen no pertenece a este producto."));

        // 2. Borrado en BD (Gracias a orphanRemoval=true en la entidad)
        cloudService.eliminarImagen(imagen.getProviderId());
        producto.removeImagen(imagen);
        proRepo.save(producto);
        log.info("Imagen {} eliminada del producto {}.", imagenId, productoId);
    }

    // Actualiza Estado de Producto
    @Transactional
    public void cambiarEstado(Long id, Boolean estado) {
        Producto producto = proRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado con ID: " + id));
        producto.setActivo(estado);
        proRepo.save(producto);
    }
}