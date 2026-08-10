package com.example.demo.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.entity.BloodRequest;
import com.example.demo.entity.User;
import com.example.demo.enums.RequestStatus;

public interface BloodRequestRepository extends JpaRepository<BloodRequest, Long> {

    @EntityGraph(attributePaths = { "user" })
    List<BloodRequest> findByUserAndDeletedFalse(User user);

    @EntityGraph(attributePaths = { "user" })
    List<BloodRequest> findByStatusAndDeletedFalse(RequestStatus status);

    @EntityGraph(attributePaths = { "user" })
    List<BloodRequest> findAllByDeletedFalse();

    @EntityGraph(attributePaths = { "user" })
    Page<BloodRequest> findAllByDeletedFalse(Pageable pageable);

    @EntityGraph(attributePaths = { "user" })
    Page<BloodRequest> findByUser_FullNameContainingIgnoreCaseAndDeletedFalse(String keyword, Pageable pageable);

    long countByDeletedFalse();

    long countByStatusAndDeletedFalse(RequestStatus status);

    long countByStatus(RequestStatus status);

}