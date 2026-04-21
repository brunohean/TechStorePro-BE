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
    public String procesarYSubirImagen(MultipartFile archivo) throws IOException {
        log.info("Iniciando carga a Cloudinary para la imagen: {}", archivo.getOriginalFilename());

        // Parámetros de transformación al vuelo (ETL)
        Map<String, Object> params = ObjectUtils.asMap(
                "folder", "techstorepro/productos",
                "resource_type", "image",          // Fuerzo a que Cloudinary lo trate como imagen
                "format", "webp"                   // Transformación ETL activa
        );

        // Subida a la nube
        Map<String, Object> uploadResult = cloudinary.uploader().upload(archivo.getBytes(), params);

        String urlPublica = uploadResult.get("secure_url").toString();
        log.info("Carga exitosa. URL generada: {}", urlPublica);

        return urlPublica;
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
}
