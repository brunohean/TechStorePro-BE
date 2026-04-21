package com.hean.consigueventas.techstorepro.controller;

import com.hean.consigueventas.techstorepro.service.CloudinaryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("/api/media")
@RequiredArgsConstructor
@Slf4j
public class MediaController {

    private final CloudinaryService cloudinaryService;

    @PostMapping("/upload/imagen")
    public ResponseEntity<Map<String, String>> subirImagen(@RequestParam("archivo") MultipartFile archivo) {
        try {
            String urlPublica = cloudinaryService.procesarYSubirImagen(archivo);
            return ResponseEntity.ok(Map.of("url", urlPublica));
        } catch (IOException e) {
            log.error("Error al procesar la imagen: {}", e.getMessage());
            return ResponseEntity.internalServerError().body(Map.of("error", "Fallo al subir el archivo a la nube"));
        }
    }
}
