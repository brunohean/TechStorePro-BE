package com.hean.consigueventas.techstorepro.service;

import com.hean.consigueventas.techstorepro.dto.producto.ProductoCatalogoDTO;
import com.hean.consigueventas.techstorepro.dto.producto.ProductoDTO;
import com.hean.consigueventas.techstorepro.entity.Producto;
import com.hean.consigueventas.techstorepro.entity.media.ImagenProducto;
import com.hean.consigueventas.techstorepro.entity.media.StorageProvider;
import com.hean.consigueventas.techstorepro.exception.custom.ResourceNotFoundException;
import com.hean.consigueventas.techstorepro.mapper.ProductoMapper;
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
    public Page<ProductoDTO> listarInventarioAdmin(Pageable pageable) {
        log.info("Consultando inventario completo para el panel de administración");
        List<Producto> todosLosProductos = proRepo.findAll();
        // findAll() ya soporta Pageable nativamente gracias a JpaRepository
        return proRepo.findAll(pageable).map(proMapper::toDto);
    }

    // Buscar un producto específico (PÚBLICO - SOLO ACTIVOS)
    @Transactional(readOnly = true)
    public ProductoDTO obtenerPorIdDetalleCatalogo(Long id) {
        Producto producto = proRepo.findByIdAndActivoTrue(id)
                .orElseThrow(() -> new EntityNotFoundException("Producto no encontrado o actualmente inactivo con ID: " + id));
        return proMapper.toDto(producto);
    }

    // Buscar un producto específico (ADMIN)
    @Transactional(readOnly = true)
    public ProductoDTO obtenerPorIdDetalleInventario(Long id) {
        Producto producto = proRepo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Producto no encontrado con ID: " + id));
        return proMapper.toDto(producto);
    }

    @Transactional
    public ProductoDTO crear(ProductoDTO dto, List<MultipartFile> archivos) {
        log.info("Iniciando creación del producto: {}", dto.getNombre());

        // 1. Convertir DTO a Entidad
        Producto producto = proMapper.toEntity(dto);

        // 2. Procesar la galería de imágenes si se enviaron archivos
        if (archivos != null && !archivos.isEmpty()) {
            boolean esPrimeraImagen = true;

            for (MultipartFile archivo : archivos) {
                try {
                    // ETL: Subir a la nube
                    Map<String, String> datosNube = cloudService.subirImagen(archivo);

                    // Construir el metadato
                    ImagenProducto imagen = ImagenProducto.builder()
                            .nombreArchivo(archivo.getOriginalFilename())
                            .urlPublica(datosNube.get("url"))
                            .providerId(datosNube.get("provider_id"))
                            .storageProvider(StorageProvider.CLOUDINARY)
                            .formato("webp")
                            .esPrincipal(esPrimeraImagen) // Solo la primera será la portada
                            .build();

                    // ¡AQUÍ ESTÁ EL HELPER METHOD EN ACCIÓN!
                    producto.addImagen(imagen);
                    esPrimeraImagen = false;

                } catch (IOException e) {
                    log.error("Fallo al subir la imagen {}: {}", archivo.getOriginalFilename(), e.getMessage());
                    // Dependiendo de tus reglas de negocio, puedes lanzar una excepción y abortar todo
                    // throw new RuntimeException("Error al procesar las imágenes del producto", e);
                }
            }
        }

        // 3. Guardar en Base de Datos (Hibernate guardará el producto y todas sus imágenes en cascada)
        Producto productoGuardado = proRepo.save(producto);

        // 4. Devolver al frontend el producto con sus URLs ya estructuradas
        return proMapper.toDto(productoGuardado);
    }

    /** Gestión A: Actualización Quirúrgica de Datos (JSON - Solo campos informativos) */
    @Transactional
    public ProductoDTO actualizarSoloDatos(Long id, ProductoDTO dto) {
        Producto productoExistente = proRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado con ID: " + id));

        // MapStruct hace el trabajo sucio: inyecta los datos de 'dto' en 'productoExistente'
        proMapper.actualizarEntidadDesdeDto(dto, productoExistente);

        log.info("Datos básicos del producto {} actualizados.", id);
        return proMapper.toDto(proRepo.save(productoExistente));
    }

    /** Gestión B: Inyección de Nuevas Imágenes a producto existente (Multipart) */
    @Transactional
    public ProductoDTO agregarImagenes(Long id, List<MultipartFile> archivos) {
        Producto producto = proRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado con ID: " + id));

        if (archivos != null && !archivos.isEmpty()) {
            for (MultipartFile archivo : archivos) {
                try {
                    Map<String, String> datosNube = cloudService.subirImagen(archivo);

                    ImagenProducto nuevaImagen = ImagenProducto.builder()
                            .nombreArchivo(archivo.getOriginalFilename())
                            .urlPublica(datosNube.get("url"))
                            .providerId(datosNube.get("provider_id"))
                            .storageProvider(StorageProvider.CLOUDINARY)
                            .formato("webp")
                            .esPrincipal(false) // Por defecto no son portada
                            .build();

                    producto.addImagen(nuevaImagen);
                } catch (IOException e) {
                    log.error("Error al subir imagen adicional: {}", e.getMessage());
                }
            }
        }
        return proMapper.toDto(proRepo.save(producto));
    }

    // Eliminar Producto (Reservado para ADMIN)
    @Transactional
    public void eliminar(Long id) {
        // 1. Buscamos el producto con todas sus relaciones
        Producto producto = proRepo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("No se puede eliminar: Producto no encontrado con ID " + id));

        // 2. BORRADO ECOLÓGICO: Recorremos las imágenes para eliminarlas de la nube
        if (producto.getImagenes() != null && !producto.getImagenes().isEmpty()) {
            for (ImagenProducto imagen : producto.getImagenes()) {

                // Verificamos que sea una imagen de Cloudinary y tenga un ID válido
                if (imagen.getStorageProvider() == StorageProvider.CLOUDINARY && imagen.getProviderId() != null) {
                    try {
                        cloudService.eliminarImagen(imagen.getProviderId());
                    } catch (IOException e) {
                        // Registramos el error pero NO detenemos la ejecución.
                        // El producto debe borrarse de nuestra BD de todas formas.
                        log.error("ALERTA: No se pudo eliminar la imagen {} de Cloudinary. Error: {}",
                                imagen.getProviderId(), e.getMessage());
                    }
                }
            }
        }

        // 3. Eliminamos de PostgreSQL
        // (Gracias a CascadeType.ALL, Hibernate también borrará las filas de 'imagenes_producto')
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

        // 1. Borrado en la nube
        try {
            if (imagen.getProviderId() != null) {
                cloudService.eliminarImagen(imagen.getProviderId());
            }
        } catch (IOException e) {
            log.error("No se pudo borrar de Cloudinary, pero procedemos con la BD: {}", e.getMessage());
        }

        // 2. Borrado en BD (Gracias a orphanRemoval=true en la entidad)
        producto.removeImagen(imagen);
        proRepo.save(producto);
        log.info("Imagen {} eliminada del producto {}.", imagenId, productoId);
    }

    // Actualiza Estado de Producto
    @Transactional
    public void cambiarEstado(Long id, boolean estado) {
        Producto producto = proRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado con ID: " + id));
        producto.setActivo(estado);
        proRepo.save(producto);
    }

}