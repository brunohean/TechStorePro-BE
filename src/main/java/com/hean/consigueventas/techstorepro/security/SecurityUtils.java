package com.hean.consigueventas.techstorepro.security;

import com.hean.consigueventas.techstorepro.security.services.UserDetailsImpl;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class SecurityUtils {

    // Obtiene el ID de usuario autenticado desde SecurityContext (CISO: Centraliza la extracción de identidad)
    public static Long getUsuarioIdAutenticado() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth.getPrincipal().equals("anonymousUser")) {
            return null;
        }
        UserDetailsImpl userDetails = (UserDetailsImpl) auth.getPrincipal();
        return userDetails.getId();
    }

    // Para Auditoría y Trazabilidad (Username)
    public static String getUsernameAutenticado() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) return "SISTEMA";
        // En Spring Security con JWT, el principal suele ser el username
        return auth.getName();
    }

    // Verifica si el usuario autenticado es Admin
    public static boolean esAdmin() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null && auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_" + SecurityConstants.ADMIN));
    }

    // Verificación de Ownership (si el id buscado es el autenticado)
    public static boolean esDueno(Long idReferencia) {
        Long idAutenticado = getUsuarioIdAutenticado();
        return idAutenticado != null && idAutenticado.equals(idReferencia);
    }
}
