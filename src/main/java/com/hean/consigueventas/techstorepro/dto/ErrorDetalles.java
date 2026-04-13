package com.hean.consigueventas.techstorepro.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ErrorDetalles {
    private LocalDateTime timestamp;
    private String mensaje;
    private String detalles;
}
