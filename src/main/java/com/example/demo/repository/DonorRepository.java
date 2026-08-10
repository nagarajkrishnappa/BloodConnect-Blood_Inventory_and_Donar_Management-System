package com.example.demo.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.demo.entity.Donor;
import com.example.demo.entity.User;
import com.example.demo.enums.BloodGroup;

public interface DonorRepository extends JpaRepository<Donor, Long> {

    Optional<Donor> findByUser(User user);

    Optional<Donor> findByUserAndDeletedFalse(User user);

    boolean existsByUserAndDeletedFalse(User user);

    List<Donor> findAllByDeletedFalse();

    @EntityGraph(attributePaths = { "user" })
    Page<Donor> findAllByDeletedFalse(Pageable pageable);

    @EntityGraph(attributePaths = { "user" })
    Page<Donor> findByUser_FullNameContainingIgnoreCaseAndDeletedFalse(String fullName, Pageable pageable);

    @EntityGraph(attributePaths = { "user" })
    Page<Donor> findByUser_FullNameContainingIgnoreCaseOrCityContainingIgnoreCaseAndDeletedFalse(String fullName, String city, Pageable pageable);

    List<Donor> findByAvailableTrueAndDeletedFalse();

    List<Donor> findByBloodGroupAndDeletedFalse(BloodGroup bloodGroup);

    List<Donor> findByCityContainingIgnoreCaseAndDeletedFalse(String city);

    long countByDeletedFalse();

    @Query("SELECT d FROM Donor d WHERE d.deleted = false " +
            "AND (:bloodGroup IS NULL OR d.bloodGroup = :bloodGroup) " +
            "AND (:city IS NULL OR LOWER(d.city) LIKE LOWER(CONCAT('%', :city, '%')))")
    List<Donor> searchDonors(@Param("bloodGroup") BloodGroup bloodGroup, @Param("city") String city);

}
