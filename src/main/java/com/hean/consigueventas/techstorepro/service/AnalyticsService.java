package com.hean.consigueventas.techstorepro.service;

import com.hean.consigueventas.techstorepro.dto.analytics.FunnelStatsDTO;
import com.hean.consigueventas.techstorepro.dto.analytics.LogisticSlaDTO;
import com.hean.consigueventas.techstorepro.dto.analytics.ProductoAbandonadoDTO;
import com.hean.consigueventas.techstorepro.repository.AnalyticsRepository;
import com.hean.consigueventas.techstorepro.repository.pedido.PedidoEstadoLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor  // Genera el constructor para los campos 'final'
public class AnalyticsService {

    private final AnalyticsRepository analyticsRepo;
    private final PedidoEstadoLogRepository logRepo;

    public FunnelStatsDTO getConversiónStats() {
        List<Object[]> resultados = analyticsRepo.countEventosPorTipo();

        Long agregados = 0L;
        Long checkouts = 0L;

        for (Object[] fila : resultados) {
            String tipo = fila[0].toString();
            Long cantidad = ((Number) fila[1]).longValue(); // Casteo seguro

            if ("AGREGAR_PRODUCTO".equals(tipo)) agregados = cantidad;
            if ("INTENTO_CHECKOUT".equals(tipo)) checkouts = cantidad;
        }

        return new FunnelStatsDTO(agregados, checkouts);
    }

    public List<ProductoAbandonadoDTO> getTopAbandonados() {
        return analyticsRepo.getTopProductosAbandonados().stream()
                .map(fila -> new ProductoAbandonadoDTO(
                        fila[0].toString(),
                        ((Number) fila[1]).longValue()
                ))
                .toList();
    }

    public LogisticSlaDTO getLogisticSla() {
        List<Object[]> resultados = logRepo.getAverageDeliveryTimeData();

        if (resultados.isEmpty() || resultados.get(0)[0] == null) {
            return new LogisticSlaDTO(0.0, 0L);
        }

        Object[] fila = resultados.get(0);
        Double promedio = ((Number) fila[0]).doubleValue();
        Long total = ((Number) fila[1]).longValue();

        return new LogisticSlaDTO(promedio, total);
    }
}
