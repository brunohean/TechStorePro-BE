package com.hean.consigueventas.techstorepro.dto;

import jakarta.validation.constraints.Email;
import lombok.Data;

import java.util.Set;

@Data
public class AdminUserUpdateDTO {
    private String username;
    @Email(message = "Formato de correo inválido")
    private String email;
    private String nuevoPassword;
    private Set<String> roles; // Ej: ["ADMIN", "USER"]
}