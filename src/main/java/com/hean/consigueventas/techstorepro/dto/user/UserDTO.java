package com.hean.consigueventas.techstorepro.dto.user;

import lombok.*;

import java.time.LocalDateTime;
import java.util.Set;

@Getter @Setter
@AllArgsConstructor @NoArgsConstructor
public class UserDTO {
    private Long id;
    private String username;
    private String email;
    private Boolean activo;
    private LocalDateTime fechaDesactivacion;
    private Set<String> roles; // Solo nombres de los roles
}
