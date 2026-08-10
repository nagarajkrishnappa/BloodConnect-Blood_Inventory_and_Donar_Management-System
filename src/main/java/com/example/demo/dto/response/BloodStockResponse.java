package com.example.demo.dto.response;

import java.time.LocalDateTime;

import com.example.demo.enums.BloodGroup;

public class BloodStockResponse {

    private Long id;

    private BloodGroup bloodGroup;

    private Integer units;

    private LocalDateTime lastUpdated;

    public BloodStockResponse() {
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

    public Integer getUnits() {
        return units;
    }

    public void setUnits(Integer units) {
        this.units = units;
    }

    public LocalDateTime getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(LocalDateTime lastUpdated) {
        this.lastUpdated = lastUpdated;
    }
}