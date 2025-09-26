package com.nachapa.api.repository;

import com.nachapa.api.auth.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByEmail(String email);

    Optional<User> findByCpf(String cpf);

    Optional<User> findByIdAndIsActiveTrue(UUID id);

    boolean existsByEmailAndIdNot(String email, UUID userId);
}
