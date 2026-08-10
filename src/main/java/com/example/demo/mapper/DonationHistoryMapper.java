package com.example.demo.mapper;

import com.example.demo.dto.request.DonationHistoryRequest;
import com.example.demo.dto.response.DonationHistoryResponse;
import com.example.demo.entity.DonationHistory;
import com.example.demo.entity.Donor;

public class DonationHistoryMapper {

    private DonationHistoryMapper() {
    }

    public static DonationHistory toEntity(
            DonationHistoryRequest request,
            Donor donor) {

        DonationHistory history = new DonationHistory();

        history.setDonor(donor);
        history.setDonationDate(request.getDonationDate());
        history.setUnitsDonated(request.getUnitsDonated());
        history.setRemarks(request.getRemarks());
        if (donor != null && donor.getBloodGroup() != null) {
            history.setBloodGroup(donor.getBloodGroup().getValue());
        }

        return history;
    }

    public static DonationHistoryResponse toResponse(
            DonationHistory history) {

        DonationHistoryResponse response = new DonationHistoryResponse();

        response.setId(history.getId());
        if (history != null && history.getDonor() != null && history.getDonor().getUser() != null) {
            response.setDonorName(history.getDonor().getUser().getFullName());
        } else {
            response.setDonorName("Unknown Donor");
        }

        response.setBloodGroup(history != null ? history.getBloodGroup() : null);
        response.setUnitsDonated(history != null ? history.getUnitsDonated() : null);
        response.setDonationDate(history != null ? history.getDonationDate() : null);
        response.setRemarks(history != null ? history.getRemarks() : null);

        return response;
    }
}
