package com.hean.consigueventas.techstorepro.service;

import com.hean.consigueventas.techstorepro.dto.ProductoDTO;
import com.hean.consigueventas.techstorepro.entity.Producto;
import com.hean.consigueventas.techstorepro.entity.media.ImagenProducto;
import com.hean.consigueventas.techstorepro.entity.media.StorageProvider;
import com.hean.consigueventas.techstorepro.exception.custom.ResourceNotFoundException;
import com.hean.consigueventas.techstorepro.mapper.ProductoMapper;
import com.hean.consigueventas.techstorepro.repository.ProductoRepository;
import com.hean.consigueventas.techstorepro.security.SecurityUtils;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

    // Listar todos los productos para el catálogo
    @Transactional(readOnly = true)
    public List<ProductoDTO> listarTodos() {
        // Obtenemos las autoridades del usuario autenticado
        List<Producto> productos = SecurityUtils.esAdmin()
                ? proRepo.findAll()     // Admin ve todos
                : proRepo.findByActivoTrue();   // User ve solo activos
        return proMapper.toDtoList(productos);
    }

    // Buscar un producto específico (útil para el detalle del producto)
    @Transactional(readOnly = true)
    public ProductoDTO obtenerPorId(Long id) {
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

    @Transactional
    public ProductoDTO actualizar(Long id, ProductoDTO dto) {
        Producto existente = proRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado"));

        existente.setNombre(dto.getNombre());
        existente.setPrecio(dto.getPrecio());
        existente.setStock(dto.getStock()); // RF-BE-05: Gestión de stock
        // ... otros campos

        return proMapper.toDto(proRepo.save(existente));
    }

    // Eliminar (Reservado para ADMIN)
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


    // Actualiza Estado de Producto
    @Transactional
    public void cambiarEstado(Long id, boolean estado) {
        Producto producto = proRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado con ID: " + id));
        producto.setActivo(estado);
        proRepo.save(producto);
    }

}