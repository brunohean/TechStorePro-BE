package com.hean.consigueventas.techstorepro.service;

import com.hean.consigueventas.techstorepro.dto.CategoriaReadDTO;
import com.hean.consigueventas.techstorepro.dto.CategoriaCreateDTO;
import com.hean.consigueventas.techstorepro.entity.Categoria;
import com.hean.consigueventas.techstorepro.mapper.CategoriaMapper;
import com.hean.consigueventas.techstorepro.repository.CategoriaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor // Genera el constructor para los campos 'final'
//@Slf4j // Uso de log y elementos de consola
public class CategoriaService {
    private final CategoriaRepository catRepo;
    private final CategoriaMapper catMapper;

    @Transactional(readOnly = true)
    public List<CategoriaReadDTO> listarCategoriasPublico() {
        /*   FORMA 1:
        List<Categoria> categoriasExistentes = catRepo.findByActivoTrue();
        return categoriasExistentes.stream().map(catMapper::toCategoriaReadDto).collect(Collectors.toList());
        */
        //   FORMA 2:
        return catMapper.toCategoriaReadDtoList(catRepo.findByActivoTrue());
    }

    @Transactional
    public CategoriaCreateDTO crearCategoria (CategoriaCreateDTO dto) {
        /*   FORMA 1 (Extensa)
        Categoria categoriaTransformada = catMapper.toCategoria(dto);
        Categoria categoriaGuardada = catRepo.save(categoriaTransformada);
        CategoriaCreateDTO dtoRespuesta = catMapper.toCategoriaCreateDto(categoriaGuardada);
        return dtoRespuesta;
         */
        //  FORMA 2
        Categoria categoriaGuardada = catRepo.save(catMapper.toCategoria(dto));
        return catMapper.toCategoriaCreateDto(categoriaGuardada);
    }
}
