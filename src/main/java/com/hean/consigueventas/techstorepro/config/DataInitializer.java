package com.hean.consigueventas.techstorepro.config;

import com.hean.consigueventas.techstorepro.entity.Producto;
import com.hean.consigueventas.techstorepro.entity.Role;
import com.hean.consigueventas.techstorepro.repository.ProductoRepository;
import com.hean.consigueventas.techstorepro.repository.RoleRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner initData(RoleRepository roleRepo, ProductoRepository prodRepo) {
        return args -> {
            // 1. Inicialización de Roles con verificación de existencia
            if (roleRepo.findByNombre("ROLE_USER").isEmpty()) {
                roleRepo.save(new Role(null, "ROLE_USER"));
            }
            if (roleRepo.findByNombre("ROLE_ADMIN").isEmpty()) {
                roleRepo.save(new Role(null, "ROLE_ADMIN"));
            }

            // 2. Inicialización de Productos (Solo si la tabla está vacía)
            if (prodRepo.count() == 0) {
                prodRepo.save(new Producto(null, "Teclado Mecánico RGB", "Teclado switch blue con retroiluminación", 45.90, 15, "https://link-imagen.com/teclado.jpg"));
                prodRepo.save(new Producto(null, "Mouse Gamer Pro", "Sensor óptico de 16000 DPI", 29.50, 20, "https://link-imagen.com/mouse.jpg"));
                prodRepo.save(new Producto(null, "Audífonos Noise Cancelling", "Bluetooth 5.0 con cancelación activa", 89.00, 10, "https://link-imagen.com/audifonos.jpg"));
                prodRepo.save(new Producto(null, "Monitor 27\" 144Hz", "Panel IPS resolución 2K", 299.00, 5, "https://link-imagen.com/monitor.jpg"));

                System.out.println("Catálogo inicial de productos cargado.");
            }

            System.out.println("✅ Verificación de integridad de datos completada.");
        };
    }
}
