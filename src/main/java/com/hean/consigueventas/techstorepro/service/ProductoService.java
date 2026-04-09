package com.hean.consigueventas.techstorepro.service;

import com.hean.consigueventas.techstorepro.dto.ProductoDTO;
import com.hean.consigueventas.techstorepro.entity.Producto;
import com.hean.consigueventas.techstorepro.mapper.ProductoMapper;
import com.hean.consigueventas.techstorepro.repository.ProductoRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ProductoService {

    private final ProductoRepository proRepo;
    private final ProductoMapper proMapper;

    // Inyección por constructor (Best Practice)
    public ProductoService(ProductoRepository productoRepository, ProductoMapper productoMapper) {
        this.proRepo = productoRepository;
        this.proMapper = productoMapper;
    }

    // METODOS

    // Listar todos los productos para el catálogo
    @Transactional(readOnly = true)
    public List<ProductoDTO> listarTodos() {
        return proMapper.toDtoList(proRepo.findAll());
    }

    // Buscar un producto específico (útil para el detalle del producto)
    @Transactional(readOnly = true)
    public ProductoDTO obtenerPorId(Long id) {
        Producto producto = proRepo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Producto no encontrado con ID: " + id));
        return proMapper.toDto(producto);
    }

    @Transactional
    public ProductoDTO crear(ProductoDTO dto) {
        Producto producto = proMapper.toEntity(dto);
        return proMapper.toDto(proRepo.save(producto));
    }

    @Transactional
    public ProductoDTO actualizar(Long id, ProductoDTO dto) {
        Producto existente = proRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado"));

        existente.setNombre(dto.getNombre());
        existente.setPrecio(dto.getPrecio());
        existente.setStock(dto.getStock()); // RF-BE-05: Gestión de stock
        // ... otros campos

        return proMapper.toDto(proRepo.save(existente));
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