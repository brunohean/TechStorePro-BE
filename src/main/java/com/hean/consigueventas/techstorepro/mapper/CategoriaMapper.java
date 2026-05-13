package com.hean.consigueventas.techstorepro.mapper;

import com.hean.consigueventas.techstorepro.dto.CategoriaReadDTO;
import com.hean.consigueventas.techstorepro.dto.CategoriaCreateDTO;
import com.hean.consigueventas.techstorepro.entity.Categoria;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface CategoriaMapper {

    // --- Mapeos de Salida (Entidad -> DTO) ---

    CategoriaReadDTO toCategoriaReadDto(Categoria categoria);
    CategoriaCreateDTO toCategoriaCreateDto(Categoria categoria); // Falta restricciones de relaciones OneToMany
    List<CategoriaReadDTO> toCategoriaReadDtoList(List<Categoria> categorias);

    // --- Mapeos de Entrada (DTO -> Entidad) ---

    Categoria toCategoria (CategoriaCreateDTO dto);

    // --- Metodos Utilitarios ---

    // Se Agregara @Mapping cuando haya relaciones OneToMany y Actualizar categorias existentes
}
