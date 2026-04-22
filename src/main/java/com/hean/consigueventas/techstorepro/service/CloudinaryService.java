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
    public Map<String, String> subirImagen(MultipartFile archivo) throws IOException {
        log.info("Procesando imagen en Cloudinary: {}", archivo.getOriginalFilename());

        // Parámetros de transformación al vuelo (ETL)
        Map<String, Object> params = ObjectUtils.asMap(
                "folder", "techstorepro/productos",
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
    public void eliminarImagen(String providerId) throws IOException {
        log.info("Solicitando eliminación a Cloudinary del ID: {}", providerId);

        // El SDK de Cloudinary requiere enviar un mapa vacío si no hay parámetros extra
        Map<String, Object> result = cloudinary.uploader().destroy(providerId, com.cloudinary.utils.ObjectUtils.emptyMap());

        log.info("Resultado de eliminación en la nube: {}", result.get("result"));
    }
}
