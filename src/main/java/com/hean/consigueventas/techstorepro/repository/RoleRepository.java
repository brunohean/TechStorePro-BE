package com.hean.consigueventas.techstorepro.repository;

import com.hean.consigueventas.techstorepro.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role,Long> {
    Optional<Role> findByNombre(String nombre);
}
