package com.hean.consigueventas.techstorepro.config;

import com.hean.consigueventas.techstorepro.entity.Role;
import com.hean.consigueventas.techstorepro.entity.User;
import com.hean.consigueventas.techstorepro.repository.UserRepository;
import com.hean.consigueventas.techstorepro.service.DataSeederService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;


@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner initData(
            DataSeederService seeder, // Inyectamos el Servicio
            UserRepository userRepo,
            PasswordEncoder passwordEncoder) {

        return args -> {
            // 1. Inicialización de Roles (usando el seeder)
            Role adminRole = seeder.seedRole("ROLE_ADMIN");
            seeder.seedRole("ROLE_USER");

            // 2. Usuario Administrador (mantenemos esta lógica aquí por el passwordEncoder)
            if (!userRepo.existsByUsername("admin_tech")) {
                User admin = new User();
                admin.setUsername("admin_tech");
                admin.setEmail("admin@techstore.com");
                admin.setPassword(passwordEncoder.encode("Admin123!"));
                admin.setRoles(new java.util.HashSet<>(java.util.List.of(adminRole)));
                userRepo.save(admin);
                System.out.println("✅ Usuario administrador creado.");
            }

            // 3. Inicialización de Productos (Limpio y profesional usando el seeder)
            seeder.seedProducto("Teclado Mecánico RGB", "Teclado switch blue con retroiluminación", 45.90, 15, "https://link-imagen.com/teclado.jpg");
            seeder.seedProducto("Mouse Gamer Pro", "Sensor óptico de 16000 DPI", 29.50, 20, "https://link-imagen.com/mouse.jpg");
            seeder.seedProducto("Audífonos Noise Cancelling", "Bluetooth 5.0 con cancelación activa", 89.00, 10, "https://link-imagen.com/audifonos.jpg");
            seeder.seedProducto("Monitor 27\" 144Hz", "Panel IPS resolución 2K", 299.00, 5, "https://link-imagen.com/monitor.jpg");

            System.out.println("✅ Verificación de integridad de datos completada.");
        };
    }
}
