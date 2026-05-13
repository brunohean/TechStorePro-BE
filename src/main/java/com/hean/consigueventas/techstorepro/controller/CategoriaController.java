package com.hean.consigueventas.techstorepro.controller;

import com.hean.consigueventas.techstorepro.dto.CategoriaReadDTO;
import com.hean.consigueventas.techstorepro.dto.CategoriaCreateDTO;
import com.hean.consigueventas.techstorepro.security.SecurityConstants;
import com.hean.consigueventas.techstorepro.service.CategoriaService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categorias")
@RequiredArgsConstructor
public class CategoriaController {
    private final CategoriaService catServ;

    @GetMapping("/listado")
    public List<CategoriaReadDTO> listarCategoriasBase () {
         return catServ.listarCategoriasPublico();
    }

    @PostMapping
    @PreAuthorize(SecurityConstants.HAS_ROLE_ADMIN)
    public CategoriaCreateDTO crearCategoria (@RequestBody CategoriaCreateDTO dto) {
        return catServ.crearCategoria(dto);
    }
}
