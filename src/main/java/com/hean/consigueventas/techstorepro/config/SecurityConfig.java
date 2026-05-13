package com.hean.consigueventas.techstorepro.config;

import com.hean.consigueventas.techstorepro.security.SecurityConstants;
import com.hean.consigueventas.techstorepro.security.jwt.AuthTokenFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

// CORS y Security
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    // Beans de Security

    @Bean
    public AuthTokenFilter authenticationJwtTokenFilter() {
        return new AuthTokenFilter();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(); // Esto soluciona el error en UserService
    }

        // Robust CORS Bean Configuration

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // CISO Insight: allowOriginPatterns("*") permite comodines + credenciales
        configuration.setAllowedOriginPatterns(Arrays.asList("*"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type", "Cache-Control"));
        configuration.setAllowCredentials(true); // Necesario para que Angular envíe el Token

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // Aplicamos la configuración de CORS antes de cualquier otra cosa
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                .csrf(csrf -> csrf.disable()) // Deshabilitado para desarrollo (CISO: se habilita con JWT/Tokens)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // SE BLOQUEA POR RUTAS, NO POR ROLES.
                .authorizeHttpRequests(auth -> auth
                        // Rutas Públicas
                        .requestMatchers("/api/auth/**","/api/productos/catalogo","/api/productos/catalogo/*", "/api/categorias/*").permitAll() // El catálogo, Registro y Login son público
                        // Rutas Protegidas por Filtro
                        .requestMatchers("/api/carrito/**").hasAnyRole(SecurityConstants.ADMIN,SecurityConstants.USER)
                        .requestMatchers("/api/pedidos/**").hasAnyRole(SecurityConstants.ADMIN,SecurityConstants.USER)
                        .requestMatchers("/api/usuarios/**").hasAnyRole(SecurityConstants.ADMIN,SecurityConstants.USER)
                        .requestMatchers("/api/admin/**").hasRole(SecurityConstants.ADMIN)

                        // Todo lo demas requiere Autorización (Tokens/Login)
                        .anyRequest().authenticated()
                );

        // Inyectamos nuestro filtro JWT antes del filtro de usuario/contraseña de Spring
        http.addFilterBefore(authenticationJwtTokenFilter(), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
