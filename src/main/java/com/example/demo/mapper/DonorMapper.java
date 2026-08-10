package com.example.demo.mapper;

import com.example.demo.dto.request.DonorRequest;
import com.example.demo.dto.response.DonorResponse;
import com.example.demo.entity.Donor;
import com.example.demo.entity.User;

public class DonorMapper {

    private DonorMapper() {
    }

    public static Donor toEntity(DonorRequest request, User user) {

        Donor donor = new Donor();

        donor.setUser(user);
        donor.setBloodGroup(request.getBloodGroup());
        donor.setGender(request.getGender());
        donor.setDateOfBirth(request.getDateOfBirth());
        donor.setAddress(request.getAddress());
        donor.setCity(request.getCity());
        donor.setState(request.getState());
        donor.setPincode(request.getPincode());
        donor.setWeight(request.getWeight());
        donor.setLastDonationDate(request.getLastDonationDate());
        donor.setAvailable(true);

        return donor;
    }

    public static DonorResponse toResponse(Donor donor) {

        DonorResponse response = new DonorResponse();

        response.setId(donor.getId());
        response.setFullName(donor.getUser().getFullName());
        response.setEmail(donor.getUser().getEmail());
        response.setPhone(donor.getUser().getPhone());
        response.setBloodGroup(donor.getBloodGroup());
        response.setCity(donor.getCity());
        response.setAvailable(donor.getAvailable());

        return response;
    }

    public static DonorRequest toRequest(Donor donor) {

        DonorRequest request = new DonorRequest();

        request.setBloodGroup(donor.getBloodGroup());
        request.setGender(donor.getGender());
        request.setDateOfBirth(donor.getDateOfBirth());
        request.setAddress(donor.getAddress());
        request.setCity(donor.getCity());
        request.setState(donor.getState());
        request.setPincode(donor.getPincode());
        request.setWeight(donor.getWeight());
        request.setLastDonationDate(donor.getLastDonationDate());

        return request;
    }
}