package com.hean.consigueventas.techstorepro.service;

import com.hean.consigueventas.techstorepro.entity.Producto;
import com.hean.consigueventas.techstorepro.repository.ProductoRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ProductoService {

    private final ProductoRepository proRepo;

    // Inyección por constructor (Best Practice)
    public ProductoService(ProductoRepository productoRepository) {
        this.proRepo = productoRepository;
    }

    // METODOS

    // Listar todos los productos para el catálogo
    @Transactional(readOnly = true)
    public List<Producto> listarTodos() {
        return proRepo.findAll();
    }

    // Buscar un producto específico (útil para el detalle del producto)
    @Transactional(readOnly = true)
    public Producto obtenerPorId(Long id) {
        return proRepo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Producto no encontrado con ID: " + id));
    }

    // Guardar o actualizar (Reservado para ADMIN)
    @Transactional
    public Producto guardar(Producto producto) {
        return proRepo.save(producto);
    }

    // Eliminar (Reservado para ADMIN)
    @Transactional
    public void eliminar(Long id) {
        if (!proRepo.existsById(id)) {
            throw new EntityNotFoundException("No se puede eliminar: Producto no encontrado");
        }
        proRepo.deleteById(id);
    }
}