package com.hean.consigueventas.techstorepro.dto.analytics;

import lombok.*;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class FunnelStatsDTO {
    private Long agregados;
    private Long intentosCheckout;
    // Puedes agregar más estados aquí en el futuro
}