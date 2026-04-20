package com.hean.consigueventas.techstorepro.dto.analytics;

import lombok.*;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class LogisticSlaDTO {
    private Double promedioHorasEntrega;
    private Long pedidosCompletados;
}
