package com.example.demo.dto.request;

import com.example.demo.enums.BloodGroup;

public class BloodRequestRequest {

    private String email;

    private BloodGroup bloodGroup;

    private Integer unitsRequired;

    private String reason;

    public BloodRequestRequest() {
    }

    public BloodRequestRequest(BloodGroup bloodGroup, Integer unitsRequired, String reason) {
        this.bloodGroup = bloodGroup;
        this.unitsRequired = unitsRequired;
        this.reason = reason;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public BloodGroup getBloodGroup() {
        return bloodGroup;
    }

    public void setBloodGroup(BloodGroup bloodGroup) {
        this.bloodGroup = bloodGroup;
    }

    public Integer getUnitsRequired() {
        return unitsRequired;
    }

    public void setUnitsRequired(Integer unitsRequired) {
        this.unitsRequired = unitsRequired;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public Integer getUnits() {
        return unitsRequired;
    }

    public void setUnits(Integer units) {
        this.unitsRequired = units;
    }
}
