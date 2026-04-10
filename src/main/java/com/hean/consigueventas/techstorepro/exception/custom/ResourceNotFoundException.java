package com.hean.consigueventas.techstorepro.exception.custom;

// Para errores 404
public class ResourceNotFoundException extends RuntimeException{
    public ResourceNotFoundException(String mensaje) { super(mensaje); }
}
