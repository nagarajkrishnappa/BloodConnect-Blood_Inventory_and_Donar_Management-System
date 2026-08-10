package com.example.demo.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.entity.User;

public interface UserRepository extends JpaRepository<User, Long> {

    boolean existsByEmail(String email);

    @EntityGraph(attributePaths = { "role" })
    Optional<User> findByEmail(String email);

    @Override
    @EntityGraph(attributePaths = { "role" })
    List<User> findAll();

    @Override
    @EntityGraph(attributePaths = { "role" })
    Page<User> findAll(Pageable pageable);

    @EntityGraph(attributePaths = { "role" })
    Page<User> findByFullNameContainingIgnoreCaseOrEmailContainingIgnoreCase(String fullName, String email, Pageable pageable);

    List<User> findByFullNameContainingIgnoreCase(String fullName);

    List<User> findByEmailContainingIgnoreCase(String email);

    List<User> findByPhoneContaining(String phone);

}