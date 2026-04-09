package com.hean.consigueventas.techstorepro.config;

import com.hean.consigueventas.techstorepro.entity.Producto;
import com.hean.consigueventas.techstorepro.entity.Role;
import com.hean.consigueventas.techstorepro.entity.User;
import com.hean.consigueventas.techstorepro.repository.ProductoRepository;
import com.hean.consigueventas.techstorepro.repository.RoleRepository;
import com.hean.consigueventas.techstorepro.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.HashSet;
import java.util.Set;

@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner initData(RoleRepository roleRepo, ProductoRepository prodRepo,
                               UserRepository userRepo, PasswordEncoder passwordEncoder) {
        return args -> {
            // 1. Inicialización de Roles con verificación de existencia
            Role adminRole = roleRepo.findByNombre("ROLE_ADMIN")
                    .orElseGet(() -> roleRepo.save(new Role(null, "ROLE_ADMIN")));
            roleRepo.findByNombre("ROLE_USER")
                    .orElseGet(() -> roleRepo.save(new Role(null, "ROLE_USER")));

            //  Usuario Administrador Inicial (CISO Path)
            if (!userRepo.existsByUsername("admin_tech")) {
                User admin = new User();
                admin.setUsername("admin_tech");
                admin.setEmail("admin@techstore.com");
                admin.setPassword(passwordEncoder.encode("Admin123!")); // Password fuerte

                Set<Role> roles = new HashSet<>();
                roles.add(adminRole);
                admin.setRoles(roles);

                userRepo.save(admin);
                System.out.println("✅ Usuario administrador creado por defecto.");
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
