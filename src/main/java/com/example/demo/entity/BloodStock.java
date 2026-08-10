package com.example.demo.entity;

import java.time.LocalDateTime;

import com.example.demo.enums.BloodGroup;

import jakarta.persistence.*;

@Entity
@Table(name = "blood_stock")
public class BloodStock {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "blood_group", nullable = false, unique = true)
    private BloodGroup bloodGroup;

    @Column(name = "units_available", nullable = false)
    private Integer unitsAvailable;

    @Column(name = "last_updated", nullable = false)
    private LocalDateTime lastUpdated;

    public BloodStock() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public BloodGroup getBloodGroup() {
        return bloodGroup;
    }

    public void setBloodGroup(BloodGroup bloodGroup) {
        this.bloodGroup = bloodGroup;
    }

    public Integer getUnitsAvailable() {
        return unitsAvailable;
    }

    public void setUnitsAvailable(Integer unitsAvailable) {
        this.unitsAvailable = unitsAvailable;
    }

    public Integer getUnits() {
        return unitsAvailable;
    }

    public void setUnits(Integer units) {
        this.unitsAvailable = units;
    }

    public LocalDateTime getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(LocalDateTime lastUpdated) {
        this.lastUpdated = lastUpdated;
    }
}