package com.hean.consigueventas.techstorepro.security;

public class SecurityConstants {
    // Nombres tal cual están en tu base de datos
    public static final String ADMIN = "ADMIN";
    public static final String USER = "USER";

    // Expresiones para @PreAuthorize
    public static final String HAS_ROLE_ADMIN = "hasRole('" + ADMIN + "')";
    public static final String HAS_ROLE_USER = "hasRole('" + USER + "')";
}
