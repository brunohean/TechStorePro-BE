package com.hean.consigueventas.techstorepro.controller;

import com.hean.consigueventas.techstorepro.dto.user.AdminUserUpdateDTO;
import com.hean.consigueventas.techstorepro.dto.user.UserDTO;
import com.hean.consigueventas.techstorepro.dto.user.UserSelfUpdateDTO;
import com.hean.consigueventas.techstorepro.security.SecurityUtils;
import com.hean.consigueventas.techstorepro.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/usuarios")
@PreAuthorize("hasAnyRole('USER','ADMIN')")
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
    @PutMapping("/actualizar-perfil/{id}")
    @PreAuthorize("#id == principal.id and principal.enabled")
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

    // E. OBTENER MI PERFIL (Uso de SecurityUtils - Sin ID en URL)
    @GetMapping("/me")
    @PreAuthorize("principal.enabled") // Si activo es false, principal.enabled es false -> 403
    public ResponseEntity<UserDTO> obtenerMisDatos() {
        Long miId = SecurityUtils.getUsuarioIdAutenticado(); // Extraemos la identidad directamente del Token JWT
        return ResponseEntity.ok(userSer.buscarPorId(miId));
    }

    // F. SOLICITAR BAJA (Soft Delete por el propio usuario)
    @DeleteMapping("/baja/{id}")
    @PreAuthorize("#id == principal.id")
    public ResponseEntity<String> darDeBaja(@PathVariable Long id) {
        userSer.desactivarCuenta(id);
        return ResponseEntity.ok("Tu cuenta ha sido desactivada. Tienes 30 días para solicitar reactivación.");
    }
}
