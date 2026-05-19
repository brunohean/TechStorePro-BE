package com.hean.consigueventas.techstorepro.config;

import com.hean.consigueventas.techstorepro.entity.Categoria;
import com.hean.consigueventas.techstorepro.entity.Producto;
import com.hean.consigueventas.techstorepro.entity.Role;
import com.hean.consigueventas.techstorepro.entity.User;
import com.hean.consigueventas.techstorepro.entity.pedido.EstadoPedido;
import com.hean.consigueventas.techstorepro.entity.pedido.Pedido;
import com.hean.consigueventas.techstorepro.service.DataSeederService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDateTime;
import java.util.Set;


@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner initData(DataSeederService seeder) { // Inyección del servicio expert
        return args -> {
            // ==========================================
            // 1. SEMBRADO DE ROLES
            // ==========================================
            Role roleUser = seeder.seedRole("ROLE_USER");
            Role roleAdmin = seeder.seedRole("ROLE_ADMIN");

            // ==========================================
            // 2. SEMBRADO DE CATEGORÍAS
            // ==========================================
            Categoria catGeneral = seeder.seedCategoria("General", "Categoria por Defecto");
            Categoria catAuriculares = seeder.seedCategoria("Auriculares", "Auriculares");
            Categoria catLaptops = seeder.seedCategoria("Laptops", "Laptops");
            Categoria catMouses = seeder.seedCategoria("Mouses", "Mouse");
            Categoria catPerifericos = seeder.seedCategoria("Periféricos", "Periféricos");

            User user1 = seeder.seedUser("user1", "user1@techstore.com", "$2a$12$AFIuJXGEyDdr0doxnXCY6OENaVzy4xIbp0yMFDmnD.2HNZPrAvx4u", Set.of(roleUser));
            User user2 = seeder.seedUser("user2", "user2@techstore.com", "$2a$12$uJkwlBNbn9dIcxn6/UEwI.jTMvY3w1sbwgFGKNzslJUP0N5ntTT0u", Set.of(roleUser));
            User adminMain = seeder.seedUser("admin_main", "admin_main@techstore.com", "$2a$12$AsTDSzMGEa2rIb8ZWXqq6u78c4PITyybosuEF8fkRW.gXMy9W4JpS", Set.of(roleAdmin));
            User adminTech = seeder.seedUser("admin_tech", "admin_tech@techstore.com", "$2a$12$Hj08A1fkB32osLAyfGZ4C.giWzWZNo.91aI1vjwPINZfItMaqXo0K", Set.of(roleAdmin));

            // ==========================================
            // 4. SEMBRADO DE PRODUCTOS E IMÁGENES (Relaciones OO directas)
            // ==========================================
            Producto monitor = seeder.seedProducto("Monitor 27\" 144Hz", "Panel IPS resolución 2K", 299.0, 0, catGeneral,
                    "monitor-27-144Hz.webp", "techstorepro/productos/dof9tackkieclk3i4dec", "https://res.cloudinary.com/deeg5upfr/image/upload/v1777403338/techstorepro/productos/dof9tackkieclk3i4dec.webp", true);

            Producto audifonosNoise = seeder.seedProducto("Audífonos Noise Cancelling", "Bluetooth 5.0 con cancelación activa", 89.0, 0, catAuriculares,
                    "audifonos-noise-canceling.webp", "techstorepro/productos/jmnop4tm6m6qumov1p6f", "https://res.cloudinary.com/deeg5upfr/image/upload/v1777403204/techstorepro/productos/jmnop4tm6m6qumov1p6f.webp", true);

            Producto mouseGamer = seeder.seedProducto("Mouse Gamer Pro", "Sensor óptico de 16000 DPI", 29.5, 17, catMouses,
                    "Mouse-Gamer-Pro.webp", "techstorepro/productos/dfshaqksqmkcicdsgfyx", "https://res.cloudinary.com/deeg5upfr/image/upload/v1777403286/techstorepro/productos/dfshaqksqmkcicdsgfyx.webp", true);

            Producto laptopGaming = seeder.seedProducto("Laptop Gaming Pro X", "Intel i9, 32GB RAM, RTX 4080", 2499.99, 13, catLaptops,
                    "Laptop-Gaming-Pro-X.webp", "techstorepro/productos/korkzdszkuuwib3xoq3e", "https://res.cloudinary.com/deeg5upfr/image/upload/v1777403389/techstorepro/productos/korkzdszkuuwib3xoq3e.webp", true);
            seeder.addAditionalImage("Laptop Gaming Pro X", "Laptop-Gaming-Pro-X-Lateral.jpg", "techstorepro/productos/gh3ine5cpiqnmunmwamt", "https://res.cloudinary.com/deeg5upfr/image/upload/v1778508734/techstorepro/productos/gh3ine5cpiqnmunmwamt.webp");

            Producto sonyXM5 = seeder.seedProducto("Sony WH-1000XM5", "Audífonos con cancelación de ruido", 350.0, 15, catAuriculares,
                    "SonyAudifonos-Exhibicion.jpg", "techstorepro/productos/m5qyvzh7sqlnpngifgq7", "https://res.cloudinary.com/deeg5upfr/image/upload/v1778094537/techstorepro/productos/m5qyvzh7sqlnpngifgq7.webp", true);
            seeder.addAditionalImage("Sony WH-1000XM5", "SonyAudifonos-Frontal.webp", "techstorepro/productos/cdfzkd1wpecv6igojwma", "https://res.cloudinary.com/deeg5upfr/image/upload/v1778094540/techstorepro/productos/cdfzkd1wpecv6igojwma.webp");
            seeder.addAditionalImage("Sony WH-1000XM5", "SonyAudifonos-Adicional.jpg", "techstorepro/productos/cmkdsp9ydqezfcyeubes", "https://res.cloudinary.com/deeg5upfr/image/upload/v1778094698/techstorepro/productos/cmkdsp9ydqezfcyeubes.webp");

            Producto mouseRazer = seeder.seedProducto("Mouse Razer RGB Gaming", "Mouse con movilidad especial para el apuntado de hasta 3000 DPI", 90.0, 20, catMouses,
                    "Mouse Razer-lateral.jpg", "techstorepro/productos/tru9a8cajvjo43ej98vc", "https://res.cloudinary.com/deeg5upfr/image/upload/v1777312548/techstorepro/productos/tru9a8cajvjo43ej98vc.webp", true);
            seeder.addAditionalImage("Mouse Razer RGB Gaming", "Mouse Razer.jpg", "techstorepro/productos/smwcxqofvcbuuyvdjzb7", "https://res.cloudinary.com/deeg5upfr/image/upload/v177312549/techstorepro/productos/smwcxqofvcbuuyvdjzb7.webp");

            Producto teclado = seeder.seedProducto("Teclado Mecánico RGB", "Teclado switch blue con retroiluminación", 45.9, 13, catPerifericos,
                    "Teclado-RGB.webp", "techstorepro/productos/l8b9ethutblwsptdpaeh", "https://res.cloudinary.com/deeg5upfr/image/upload/v1777403442/techstorepro/productos/l8b9ethutblwsptdpaeh.webp", true);
            teclado.setActivo(false);

            Producto carpeta = seeder.seedProducto("Mi carpeta Especial de Ciencia", "Teclado switch blue con retroiluminación", 300.0, 22, catGeneral,
                    "Carpeta-de-Ciencia.jpg", "techstorepro/productos/qj92sm1d71thcgnxdc9l", "https://res.cloudinary.com/deeg5upfr/image/upload/v1777403117/techstorepro/productos/qj92sm1d71thcgnxdc9l.webp", true);
            carpeta.setActivo(false);

            // ==========================================
            // 5. SEMBRADO DE PEDIDOS DE PRUEBA (Usa la abstracción del servicio)
            // ==========================================
            if (seeder.hasNoPedidos()) {
                Pedido p1 = seeder.createPedido(EstadoPedido.ENTREGADO, LocalDateTime.now().minusDays(2), 10200.0, adminMain, "987654321", "Alfonso Papucho", "Av. Prolongación Primavera 2390, Surco");
                seeder.createPedidoDetalle(3, 300.0, p1, monitor);
                seeder.createPedidoControl(p1, "CAMBIO_ESTADO: ENTREGADO", "El cliente confirmo la recepción de su pedido");

                Pedido p2 = seeder.createPedido(EstadoPedido.PENDIENTE, LocalDateTime.now(), 2499.99, adminTech, "987654321", "Alfonso Jhonny", "Av. Prolongación Primavera 2390, Surco");
                seeder.createPedidoDetalle(1, 2499.99, p2, laptopGaming);
                seeder.createPedidoControl(p2, "CHECKOUT_EXITOSO", "Compra finalizada desde el carrito web.");
            }

            System.out.println("✅ Base de datos inicializada defensivamente y lista para entornos de producción.");
        };
    }
}