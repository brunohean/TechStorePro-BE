package com.hean.consigueventas.techstorepro.repository;

import com.hean.consigueventas.techstorepro.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User,Long> {
    // Para el Login
    Optional<User> findByUsername(String username);

    // Validaciones para el Registro (QA)
    Boolean existsByUsername(String username);
    Boolean existsByEmail(String email);
}
