package com.hean.consigueventas.techstorepro.controller;

import com.hean.consigueventas.techstorepro.dto.analytics.FunnelStatsDTO;
import com.hean.consigueventas.techstorepro.dto.analytics.LogisticSlaDTO;
import com.hean.consigueventas.techstorepro.dto.analytics.ProductoAbandonadoDTO;
import com.hean.consigueventas.techstorepro.service.AnalyticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/analytics")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    @GetMapping("/funnel")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<FunnelStatsDTO> getFunnel() {
        return ResponseEntity.ok(analyticsService.getConversiónStats());
    }

    @GetMapping("/productos-abandonados")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<ProductoAbandonadoDTO>> getAbandonados() {
        return ResponseEntity.ok(analyticsService.getTopAbandonados());
    }

    @GetMapping("/sla-logistico")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<LogisticSlaDTO> getSlaLogistico() {
        return ResponseEntity.ok(analyticsService.getLogisticSla());
    }
}