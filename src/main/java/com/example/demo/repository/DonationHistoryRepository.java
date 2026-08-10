package com.example.demo.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.entity.DonationHistory;

public interface DonationHistoryRepository extends JpaRepository<DonationHistory, Long> {

    List<DonationHistory> findAllByDeletedFalse();

    @EntityGraph(attributePaths = { "donor", "donor.user" })
    Page<DonationHistory> findAllByDeletedFalse(Pageable pageable);

    @EntityGraph(attributePaths = { "donor", "donor.user" })
    Page<DonationHistory> findByDonor_User_FullNameContainingIgnoreCaseAndDeletedFalse(String keyword, Pageable pageable);

    long countByDeletedFalse();

}