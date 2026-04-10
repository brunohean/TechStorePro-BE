package com.hean.consigueventas.techstorepro.dto;

import jakarta.validation.constraints.Email;
import lombok.Data;

@Data
public class UserSelfUpdateDTO {
    @Email(message = "Formato de correo inválido")
    private String email;
    private String nuevoPassword;
}