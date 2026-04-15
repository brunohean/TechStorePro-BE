package com.hean.consigueventas.techstorepro.dto.user;

import jakarta.validation.constraints.Email;
import lombok.*;

@Getter @Setter
@AllArgsConstructor @NoArgsConstructor
public class UserSelfUpdateDTO {
    @Email(message = "Formato de correo inválido")
    private String email;
    private String nuevoPassword;
}