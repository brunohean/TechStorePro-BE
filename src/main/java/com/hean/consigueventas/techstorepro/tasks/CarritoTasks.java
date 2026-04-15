package com.hean.consigueventas.techstorepro.tasks;

import com.hean.consigueventas.techstorepro.service.CarritoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@Slf4j // Para bitácora de auditoría
public class CarritoTasks {

    private final CarritoService carServ;

    public CarritoTasks(CarritoService carServ) {
        this.carServ = carServ;
    }

    /**
     * Se ejecuta cada medianoche (00:00:00).
     * Cron format: "segundo minuto hora día mes día-semana"
     */
    @Scheduled(cron = "0 0 0 * * ?")
    public void limpiarCarritosAbandonados() {
        log.info("TAREAS: Iniciando limpieza automática de carritos inactivos...");

        // Definimos 48 horas como estándar de la industria para abandono
        int total = carServ.limpiarCarritosInactivos(24);

        if (total > 0) {
            log.info("TAREAS: Se han limpiado {} carritos inactivos con éxito.", total);
        } else {
            log.info("TAREAS: No se encontraron carritos inactivos para limpiar.");
        }
    }
}
