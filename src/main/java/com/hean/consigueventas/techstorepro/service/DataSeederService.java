package com.hean.consigueventas.techstorepro.service;

import com.hean.consigueventas.techstorepro.entity.Producto;
import com.hean.consigueventas.techstorepro.entity.Role;
import com.hean.consigueventas.techstorepro.entity.media.ImagenProducto;
import com.hean.consigueventas.techstorepro.entity.media.StorageProvider;
import com.hean.consigueventas.techstorepro.repository.ProductoRepository;
import com.hean.consigueventas.techstorepro.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor // Genera el constructor para los campos 'final'
public class DataSeederService {

    private final ProductoRepository prodRepo;
    private final RoleRepository roleRepo;

    /**
     * Crea un producto solo si el nombre no existe en la BD.
     * CISO Check: Garantiza que no haya colisiones de nombres únicos.
     */
    @Transactional
    public void seedProducto(String nombre, String desc, Double precio, Integer stock, String url) {
        if (!prodRepo.existsByNombre(nombre)) {
            Producto p = Producto.builder()
                    .nombre(nombre)
                    .descripcion(desc)
                    .precio(precio)
                    .stock(stock)
                    .activo(true)
                    .build();

            ImagenProducto imagenSemilla = ImagenProducto.builder()
                    .nombreArchivo("imagen_default_seeder.jpg")
                    .urlPublica(url)
                    .providerId("seeder_dummy_id_" + System.currentTimeMillis())
                    .storageProvider(StorageProvider.CLOUDINARY)
                    .formato("jpg")
                    .esPrincipal(true)
                    .build();

            p.addImagen(imagenSemilla);

            prodRepo.save(p);
            System.out.println("📦 Producto creado con su imagen: " + nombre);
        }
    }

    /**
     * Inicializa roles de forma segura.
     */
    @Transactional
    public Role seedRole(String nombreRole) {
        return roleRepo.findByNombre(nombreRole)
                .orElseGet(() -> roleRepo.save(new Role(null, nombreRole)));
    }
}