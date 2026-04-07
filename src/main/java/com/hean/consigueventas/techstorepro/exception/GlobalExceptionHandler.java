package com.hean.consigueventas.techstorepro.exception;

import com.hean.consigueventas.techstorepro.dto.ErrorDetalles;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {
    // ERROR 404: Cuando algo no existe (Producto o Pedido no encontrado)
    // Para esto, en tus Service deberías lanzar una excepción específica o manejarla aquí
    @ExceptionHandler(EntityNotFoundException.class) // Puedes usar esta de Jakarta o una propia
    public ResponseEntity<ErrorDetalles> manejarNoEncontrado(EntityNotFoundException ex, WebRequest request) {
        ErrorDetalles error = new ErrorDetalles(LocalDateTime.now(), ex.getMessage(), request.getDescription(false));
        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }

    // ERROR 400: Errores de lógica (Stock insuficiente, datos inválidos)
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ErrorDetalles> manejarRuntime(RuntimeException ex, WebRequest request) {
        ErrorDetalles error = new ErrorDetalles(LocalDateTime.now(), ex.getMessage(), request.getDescription(false));
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    // ERROR 400: Validaciones de @Valid (Campos vacíos, precios negativos)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> manejarValidaciones(MethodArgumentNotValidException ex) {
        Map<String, String> errores = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error ->
                errores.put(error.getField(), error.getDefaultMessage()));
        return new ResponseEntity<>(errores, HttpStatus.BAD_REQUEST);
    }
}
