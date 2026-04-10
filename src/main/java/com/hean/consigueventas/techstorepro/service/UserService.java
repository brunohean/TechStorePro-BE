package com.hean.consigueventas.techstorepro.service;

import com.hean.consigueventas.techstorepro.dto.AdminUserUpdateDTO;
import com.hean.consigueventas.techstorepro.dto.UserDTO;
import com.hean.consigueventas.techstorepro.dto.UserSelfUpdateDTO;
import com.hean.consigueventas.techstorepro.entity.Role;
import com.hean.consigueventas.techstorepro.entity.User;
import com.hean.consigueventas.techstorepro.exception.custom.BusinessLogicException;
import com.hean.consigueventas.techstorepro.exception.custom.ResourceNotFoundException;
import com.hean.consigueventas.techstorepro.mapper.UserMapper;
import com.hean.consigueventas.techstorepro.repository.RoleRepository;
import com.hean.consigueventas.techstorepro.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class UserService {

    private final UserRepository userRepo;
    private final RoleRepository roleRepo;
    private final UserMapper userMapper;
    private final PasswordEncoder passEnco; // Veremos esto en SecurityConfig

    public UserService(UserRepository userRepository, RoleRepository roleRepository, UserMapper userMapper, PasswordEncoder passwordEncoder) {
        this.userRepo = userRepository;
        this.roleRepo = roleRepository;
        this.passEnco = passwordEncoder;
        this.userMapper = userMapper;
    }


    @Transactional
    public User registrarUsuario(User user) {
        // CISO CHECK: Nunca guardamos contraseñas en texto plano
        user.setPassword(passEnco.encode(user.getPassword()));

        // Asignar ROLE_USER por defecto
        Set<Role> roles = new HashSet<>();
        Role userRole = roleRepo.findByNombre("ROLE_USER")
                .orElseThrow(() -> new RuntimeException("Error: Rol no encontrado."));
        roles.add(userRole);

        user.setRoles(roles);
        return userRepo.save(user);
    }

    @Transactional
    public void actualizarMiPerfil(Long id, UserSelfUpdateDTO dto) {
        User user = userRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        if (dto.getEmail() != null) user.setEmail(dto.getEmail());
        if (dto.getNuevoPassword() != null && !dto.getNuevoPassword().isEmpty()) {
            user.setPassword(passEnco.encode(dto.getNuevoPassword()));
        }
        userRepo.save(user);
    }

    @Transactional
    public void actualizarComoAdmin(Long id, AdminUserUpdateDTO dto) {
        User user = userRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        if (dto.getUsername() != null) user.setUsername(dto.getUsername());
        if (dto.getEmail() != null) user.setEmail(dto.getEmail());
        if (dto.getNuevoPassword() != null && !dto.getNuevoPassword().isEmpty()) {
            user.setPassword(passEnco.encode(dto.getNuevoPassword()));
        }

        // Lógica de Roles: Transformamos Strings en Entidades Role
        if (dto.getRoles() != null && !dto.getRoles().isEmpty()) {
            Set<Role> nuevosRoles = new HashSet<>();
            for (String roleStr : dto.getRoles()) {
                Role role = roleRepo.findByNombre("ROLE_" + roleStr.toUpperCase())
                        .orElseThrow(() -> new BusinessLogicException("El rol " + roleStr + " no existe en el sistema."));
                nuevosRoles.add(role);
            }
            user.setRoles(nuevosRoles);
        }
        userRepo.save(user);
    }

    @Transactional(readOnly = true)
    public List<UserDTO> listarTodos() {
        return userMapper.toDtoList(userRepo.findAll());
    }

    @Transactional(readOnly = true)
    public UserDTO buscarPorId(Long id) {
        User user = userRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con ID: " + id));
        return userMapper.toDto(user);
    }

    @Transactional
    public void eliminar(Long id) {
        if (!userRepo.existsById(id)) {
            throw new ResourceNotFoundException("No se puede eliminar: Usuario no existe.");
        }
        userRepo.deleteById(id);
    }

    @Transactional
    public void eliminarLogico(Long id) {
        User user = userRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        user.setActivo(false); // La cuenta queda en el sistema, pero no puede loguearse
        userRepo.save(user);
        // CISO Note: Podrías añadir un log aquí para que el Admin revise la petición
    }
}
