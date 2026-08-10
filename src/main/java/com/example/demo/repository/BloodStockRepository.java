package com.example.demo.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.entity.BloodStock;
import com.example.demo.enums.BloodGroup;

public interface BloodStockRepository extends JpaRepository<BloodStock, Long> {

    Optional<BloodStock> findByBloodGroup(BloodGroup bloodGroup);

    List<BloodStock> findAllByOrderByBloodGroup();

    @Override
    Page<BloodStock> findAll(Pageable pageable);

}