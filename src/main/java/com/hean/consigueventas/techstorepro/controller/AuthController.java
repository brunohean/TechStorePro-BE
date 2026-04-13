package com.hean.consigueventas.techstorepro.controller;

import com.hean.consigueventas.techstorepro.dto.security.JwtResponse;
import com.hean.consigueventas.techstorepro.dto.security.LoginRequest;
import com.hean.consigueventas.techstorepro.dto.MensajeResponse;
import com.hean.consigueventas.techstorepro.dto.user.RegistroRequest;
import com.hean.consigueventas.techstorepro.entity.User;
import com.hean.consigueventas.techstorepro.repository.UserRepository;
import com.hean.consigueventas.techstorepro.security.jwt.JwtUtils;
import com.hean.consigueventas.techstorepro.security.services.UserDetailsImpl;
import com.hean.consigueventas.techstorepro.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final UserService userService;
    private final JwtUtils jwtUtils;

    public AuthController(AuthenticationManager authenticationManager, UserRepository userRepository,
                          UserService userService, JwtUtils jwtUtils) {
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.userService = userService;
        this.jwtUtils = jwtUtils;
    }

    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        // 1. Autenticar las credenciales enviadas
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));

        // 2. Establecer el contexto de seguridad
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // 3. Generar el Token JWT
        String jwt = jwtUtils.generarJwtToken(authentication);

        // 4. Obtener los detalles del usuario autenticado
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        List<String> roles = userDetails.getAuthorities().stream()
                .map(item -> item.getAuthority())
                .collect(Collectors.toList());

        return ResponseEntity.ok(new JwtResponse(jwt,
                userDetails.getId(),
                userDetails.getUsername(),
                userDetails.getEmail(),
                roles));
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@Valid @RequestBody RegistroRequest registroRequest) {
        // CISO CHECK: Validar que el usuario o email no existan
        if (userRepository.existsByUsername(registroRequest.getUsername())) {
            return ResponseEntity.badRequest().body(new MensajeResponse("Error: El nombre de usuario ya está en uso."));
        }

        if (userRepository.existsByEmail(registroRequest.getEmail())) {
            return ResponseEntity.badRequest().body(new MensajeResponse("Error: El email ya está registrado."));
        }

        // Crear la nueva cuenta
        User user = new User();
        user.setUsername(registroRequest.getUsername());
        user.setEmail(registroRequest.getEmail());
        user.setPassword(registroRequest.getPassword()); // El service se encarga de cifrarla

        userService.registrarUsuario(user);

        return ResponseEntity.ok(new MensajeResponse("¡Usuario registrado con éxito!"));
    }
}
