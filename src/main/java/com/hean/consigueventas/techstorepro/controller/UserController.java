package com.hean.consigueventas.techstorepro.controller;

import com.hean.consigueventas.techstorepro.dto.AdminUserUpdateDTO;
import com.hean.consigueventas.techstorepro.dto.UserDTO;
import com.hean.consigueventas.techstorepro.dto.UserSelfUpdateDTO;
import com.hean.consigueventas.techstorepro.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/usuarios")
public class UserController {

    private final UserService userSer;

    public UserController(UserService userService) {
        this.userSer = userService;
    }

    // A. LISTAR TODOS (Solo ADMIN)
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UserDTO>> listarTodos() {
        return ResponseEntity.ok(userSer.listarTodos());
    }

    // B. BUSCAR POR ID (Solo ADMIN)
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserDTO> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(userSer.buscarPorId(id));
    }

    // C.1. RUTA PARA EL DUEÑO DE LA CUENTA
    @PutMapping("/mi-perfil/{id}")
    @PreAuthorize("#id == principal.id")
    public ResponseEntity<String> actualizarMiPerfil(@PathVariable Long id, @RequestBody UserSelfUpdateDTO dto) {
        userSer.actualizarMiPerfil(id, dto);
        return ResponseEntity.ok("Tu perfil ha sido actualizado.");
    }

    // C.2. RUTA PARA GESTIÓN ADMINISTRATIVA
    @PutMapping("/{id}/gestion")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> gestionAdmin(@PathVariable Long id, @RequestBody AdminUserUpdateDTO dto) {
        userSer.actualizarComoAdmin(id, dto);
        return ResponseEntity.ok("Usuario gestionado por el administrador.");
    }

    // D. ELIMINAR (Solo ADMIN)
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        userSer.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
