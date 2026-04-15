package com.hean.consigueventas.techstorepro.dto.user;

import jakarta.validation.constraints.Email;
import lombok.*;

import java.util.Set;

@Getter @Setter
@AllArgsConstructor @NoArgsConstructor
public class AdminUserUpdateDTO {
    private String username;
    @Email(message = "Formato de correo inválido")
    private String email;
    private String nuevoPassword;
    private Boolean activo;
    private Set<String> roles; // Ej: ["ADMIN", "USER"]
}