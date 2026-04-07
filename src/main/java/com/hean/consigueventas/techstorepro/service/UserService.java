package com.hean.consigueventas.techstorepro.service;

import com.hean.consigueventas.techstorepro.entity.Role;
import com.hean.consigueventas.techstorepro.entity.User;
import com.hean.consigueventas.techstorepro.repository.RoleRepository;
import com.hean.consigueventas.techstorepro.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;

@Service
public class UserService {

    private final UserRepository userRepo;
    private final RoleRepository roleRepo;
    private final PasswordEncoder passEnco; // Veremos esto en SecurityConfig

    public UserService(UserRepository userRepository, RoleRepository roleRepository, PasswordEncoder passwordEncoder) {
        this.userRepo = userRepository;
        this.roleRepo = roleRepository;
        this.passEnco = passwordEncoder;
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
}
