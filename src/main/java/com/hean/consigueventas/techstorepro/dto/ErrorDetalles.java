package com.hean.consigueventas.techstorepro.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter @Setter
@AllArgsConstructor @NoArgsConstructor
public class ErrorDetalles {
    private LocalDateTime timestamp;
    private String mensaje;
    private String detalles;
}
