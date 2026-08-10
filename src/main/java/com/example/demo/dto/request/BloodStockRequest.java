package com.example.demo.dto.request;

import com.example.demo.enums.BloodGroup;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public class BloodStockRequest {

    @NotNull(message = "Blood group is required.")
    private BloodGroup bloodGroup;

    @Min(value = 0, message = "Units cannot be negative.")
    private Integer units;

    public BloodStockRequest() {
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

    public Integer getUnitsAvailable() {
        return units;
    }

    public void setUnitsAvailable(Integer unitsAvailable) {
        this.units = unitsAvailable;
    }

}