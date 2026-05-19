package com.hean.consigueventas.techstorepro.service;

import com.hean.consigueventas.techstorepro.entity.Categoria;
import com.hean.consigueventas.techstorepro.entity.Producto;
import com.hean.consigueventas.techstorepro.entity.Role;
import com.hean.consigueventas.techstorepro.entity.User;
import com.hean.consigueventas.techstorepro.entity.media.ImagenProducto;
import com.hean.consigueventas.techstorepro.entity.media.StorageProvider;
import com.hean.consigueventas.techstorepro.entity.pedido.PedidoControl;
import com.hean.consigueventas.techstorepro.entity.pedido.PedidoDetalle;
import com.hean.consigueventas.techstorepro.entity.pedido.EstadoPedido;
import com.hean.consigueventas.techstorepro.entity.pedido.Pedido;
import com.hean.consigueventas.techstorepro.repository.CategoriaRepository;
import com.hean.consigueventas.techstorepro.repository.ProductoRepository;
import com.hean.consigueventas.techstorepro.repository.RoleRepository;
import com.hean.consigueventas.techstorepro.repository.UserRepository;
import com.hean.consigueventas.techstorepro.repository.pedido.PedidoControlRepository;
import com.hean.consigueventas.techstorepro.repository.pedido.PedidoDetalleRepository;
import com.hean.consigueventas.techstorepro.repository.pedido.PedidoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Service
@RequiredArgsConstructor // Genera el constructor para los campos 'final'
public class DataSeederService {

    private final ProductoRepository prodRepo;
    private final RoleRepository roleRepo;
    private final UserRepository userRepo;
    private final CategoriaRepository catRepo;
    private final PedidoRepository pedidoRepo;
    private final PedidoDetalleRepository pedDetalleRepo;
    private final PedidoControlRepository pedControlRepo;

    /**
     * Inicializa roles de forma segura.
     */
    @Transactional
    public Role seedRole(String nombreRole) {
        return roleRepo.findByNombre(nombreRole)
                .orElseGet(() -> roleRepo.save(new Role(null, nombreRole)));
    }

    /**
     * Inicializa usuarios usando los hashes de BCrypt directos.
     */
    @Transactional
    public User seedUser(String username, String email, String bcryptPassword, Set<Role> roles) {
        return userRepo.findByUsername(username)
                .orElseGet(() -> {
                    User newUser = new User();
                    newUser.setUsername(username);
                    newUser.setEmail(email);
                    newUser.setPassword(bcryptPassword);
                    newUser.setRoles(new HashSet<>(roles));
                    newUser.setActivo(true);
                    System.out.println("👤 Usuario creado en el sembrado: " + username);
                    return userRepo.save(newUser);
                });
    }

    /**
     * Inicializa categorías de forma segura.
     */
    @Transactional
    public Categoria seedCategoria(String nombre, String descripcion) {
        return catRepo.findByNombre(nombre)
                .orElseGet(() -> catRepo.save(
                        Categoria.builder()
                                .nombre(nombre)
                                .descripcion(descripcion)
                                .activo(true)
                                .build()
                ));
    }

    /**
     * Inicializa productos asociándole su categoría e imagen principal.
     * Si ya existe, lo retorna para servir de referencia a los pedidos semilla.
     */
    @Transactional
    public Producto seedProducto(String nombre, String desc, Double precio, Integer stock, Categoria categoria,
                                 String nombreArchivo, String providerId, String urlPublica, boolean esPrincipal) {

        if (prodRepo.existsByNombre(nombre)) {
            return prodRepo.findByNombre(nombre).orElse(null);
        }

        Producto p = Producto.builder()
                .nombre(nombre)
                .descripcion(desc)
                .precio(precio)
                .stock(stock)
                .activo(true)
                .build();

        // Vinculación de la relación ManyToOne
        p.setCategoria(categoria);

        // Construcción de la imagen asociada
        ImagenProducto imagenSemilla = ImagenProducto.builder()
                .nombreArchivo(nombreArchivo)
                .urlPublica(urlPublica)
                .providerId(providerId)
                .storageProvider(StorageProvider.CLOUDINARY)
                .formato("webp")
                .esPrincipal(esPrincipal)
                .build();

        p.addImagen(imagenSemilla);
        System.out.println("📦 Producto creado con su imagen: " + nombre);
        return prodRepo.save(p);
    }

    /**
     * Añade imágenes adicionales a un producto existente de forma segura.
     * CISO & Performance Check: Busca el producto dentro de la sesión activa de persistencia.
     */
    @Transactional
    public void addAditionalImage(String nombreProducto, String nombreArchivo, String providerId, String urlPublica) {
        prodRepo.findByNombre(nombreProducto).ifPresent(producto -> {
            // Al estar dentro de @Transactional y recuperar el objeto del repo, la sesión está activa
            boolean existe = producto.getImagenes() != null && producto.getImagenes().stream()
                    .anyMatch(img -> img.getProviderId().equals(providerId));

            if (!existe) {
                ImagenProducto img = ImagenProducto.builder()
                        .nombreArchivo(nombreArchivo)
                        .urlPublica(urlPublica)
                        .providerId(providerId)
                        .storageProvider(StorageProvider.CLOUDINARY)
                        .formato("webp")
                        .esPrincipal(false)
                        .build();
                producto.addImagen(img);
                prodRepo.save(producto);
            }
        });
    }

    /**
     * Crea un Pedido maestro en la base de datos.
     */
    @Transactional
    public Pedido createPedido(EstadoPedido estado, LocalDateTime fecha, Double total, User usuario,
                               String celular, String clienteNombre, String direccion) {
        Pedido pedido = new Pedido();
        pedido.setEstado(estado);
        pedido.setFecha(fecha);
        pedido.setTotal(total);
        pedido.setUsuario(usuario);
        pedido.setCelular(celular != null ? celular : "999888777");
        pedido.setClienteNombre(clienteNombre != null ? clienteNombre : "Cliente Anónimo");
        pedido.setDireccion(direccion != null ? direccion : "Av. Sin Nombre 123, Lima");
        return pedidoRepo.save(pedido);
    }

    /**
     * Inserta los detalles del carrito correspondientes a un pedido.
     */
    @Transactional
    public void createPedidoDetalle(Integer cantidad, Double precioUnitario, Pedido pedido, Producto producto) {
        if (producto != null && pedido != null) {
            PedidoDetalle detalle = new PedidoDetalle();
            detalle.setCantidad(cantidad);
            detalle.setPrecioUnitario(precioUnitario);
            detalle.setPedido(pedido);
            detalle.setProducto(producto);
            pedDetalleRepo.save(detalle);
        }
    }

    /**
     * Registra el historial de auditoría y control de un pedido.
     */
    @Transactional
    public void createPedidoControl(Pedido pedido, String accion, String detalleTexto) {
        if (pedido != null) {
            PedidoControl control = new PedidoControl();
            control.setPedido(pedido);
            control.setAccion(accion);
            control.setDetalle(detalleTexto);
            control.setFechaUltimoCambioEstado(LocalDateTime.now());
            control.setIpRegistro("127.0.0.1");
            control.setVisibleParaAdmin(true);
            control.setVisibleParaUsuario(true);
            control.setVersion(0L);
            pedControlRepo.save(control);
        }
    }

    /**
     * Determina si la tabla de pedidos está vacía (Encapsulamiento del repositorio).
     */
    public boolean hasNoPedidos() {
        return pedidoRepo.count() == 0;
    }
}