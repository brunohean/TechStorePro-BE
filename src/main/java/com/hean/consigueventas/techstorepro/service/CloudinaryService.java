package com.hean.consigueventas.techstorepro.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Service
@Slf4j
public class CloudinaryService {

    private final Cloudinary cloudinary;

    // Inyectamos la URL desde el application.properties
    public CloudinaryService(@Value("${cloudinary.url}") String cloudinaryUrl) {
        this.cloudinary = new Cloudinary(cloudinaryUrl);
        this.cloudinary.config.secure = true; // Fuerza que las URLs generadas sean HTTPS
    }


    /**
     * USO 1: Imagenes (Optimización a WebP)
     */
    public Map<String, String> subirImagen(MultipartFile archivo, String nombreProducto) {
        try {
            log.info("Procesando imagen en Cloudinary: {}", archivo.getOriginalFilename());

            // Generamos un ID personalizado: nombre-limpio_milisegundos
            String publicId = nombreProducto.replaceAll("[^a-zA-Z0-9]", "-").toLowerCase()
                    + "_" + System.currentTimeMillis();

            // Parámetros de transformación (ETL)
            Map<String, Object> params = ObjectUtils.asMap(
                    "folder", "techstorepro/productos",
                    "public_id", publicId,
                    "resource_type", "image",          // Fuerzo a que Cloudinary lo trate como imagen
                    "format", "webp"                   // Transformación ETL activa
            );

            // Subida a la nube
            Map<String, Object> uploadResult = cloudinary.uploader().upload(archivo.getBytes(), params);

            // Devolvemos tanto la URL como el ID del proveedor
            return Map.of(
                    "url", uploadResult.get("secure_url").toString(),
                    "provider_id", uploadResult.get("public_id").toString()
            );
        }
        catch (IOException e) {
            log.error("Error crítico al subir a Cloudinary: {}", e.getMessage());
            // Lanzamos RuntimeException para que @Transactional haga Rollback
            throw new RuntimeException("Error en la comunicación con el servicio de imágenes", e);
        }
    }

    /**
     * USO 2: Documentos y Reportes (Sin alterar el formato original)
     */
    public String subirDocumento(MultipartFile archivo, String subCarpeta) throws IOException {
        log.info("Subiendo documento adjunto: {}", archivo.getOriginalFilename());

        Map<String, Object> params = ObjectUtils.asMap(
                "folder", "techstorepro/documentos/" + subCarpeta,
                "resource_type", "raw"             // 'raw' le dice a Cloudinary: "No lo toques, solo guárdalo"
        );

        Map<String, Object> uploadResult = cloudinary.uploader().upload(archivo.getBytes(), params);
        return uploadResult.get("secure_url").toString();
    }

    /**
     * USO 3: Borrado Ecológico  (Gestión de ciclo de vida)
     */
    public void eliminarImagen(String providerId) {
        try {
            log.info("Solicitando eliminación a Cloudinary del ID: {}", providerId);
            Map<String, Object> result = cloudinary.uploader().destroy(providerId, com.cloudinary.utils.ObjectUtils.emptyMap());
            log.info("Resultado de eliminación en la nube: {}", result.get("result"));
        }
        catch (Exception e) {
            log.error("No se pudo eliminar la imagen en la nube: {}", e.getMessage());
            throw new RuntimeException("Error al eliminar imagen antigua", e);
        }
    }
}
