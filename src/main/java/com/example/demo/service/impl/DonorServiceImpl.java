package com.example.demo.service.impl;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.demo.dto.request.DonorRequest;
import com.example.demo.dto.response.DonorResponse;
import com.example.demo.entity.Donor;
import com.example.demo.entity.User;
import com.example.demo.mapper.DonorMapper;
import com.example.demo.entity.Role;
import com.example.demo.repository.DonorRepository;
import com.example.demo.repository.RoleRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.DonorService;

import java.util.List;
import java.util.stream.Collectors;

import com.example.demo.enums.BloodGroup;

@Service
public class DonorServiceImpl implements DonorService {

    @Autowired
    private DonorRepository donorRepository;

    @Autowired
    private UserRepository userRepository;

    @Override
    public void registerDonor(String email, DonorRequest request) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (donorRepository.existsByUserAndDeletedFalse(user)) {
            throw new RuntimeException("You are already registered as an active donor.");
        }

        // If user previously deleted their donor profile, re-activate and update
        // existing record
        Optional<Donor> existingDonorOpt = donorRepository.findByUser(user);
        Donor donor;
        if (existingDonorOpt.isPresent()) {
            donor = existingDonorOpt.get();
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
            donor.setDeleted(false);
        } else {
            donor = DonorMapper.toEntity(request, user);
        }

        donorRepository.save(donor);
    }

    @Override
    public DonorResponse getDonorProfile(String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Donor donor = donorRepository.findByUserAndDeletedFalse(user)
                .orElseThrow(() -> new RuntimeException("Donor profile not found"));

        return DonorMapper.toResponse(donor);
    }

    @Override
    public DonorResponse updateDonor(String email, DonorRequest request) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Donor donor = donorRepository.findByUserAndDeletedFalse(user)
                .orElseThrow(() -> new RuntimeException("Donor profile not found"));

        donor.setBloodGroup(request.getBloodGroup());
        donor.setGender(request.getGender());
        donor.setDateOfBirth(request.getDateOfBirth());
        donor.setAddress(request.getAddress());
        donor.setCity(request.getCity());
        donor.setState(request.getState());
        donor.setPincode(request.getPincode());
        donor.setWeight(request.getWeight());
        donor.setLastDonationDate(request.getLastDonationDate());

        donorRepository.save(donor);

        return DonorMapper.toResponse(donor);
    }

    @Override
    public DonorRequest getDonorForEdit(String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Donor donor = donorRepository.findByUserAndDeletedFalse(user)
                .orElseThrow(() -> new RuntimeException("Donor profile not found"));

        return DonorMapper.toRequest(donor);
    }

    @Override
    public void deleteDonor(String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Donor donor = donorRepository.findByUserAndDeletedFalse(user)
                .orElseThrow(() -> new RuntimeException("Donor profile not found"));

        donor.setDeleted(true);
        donor.setAvailable(false);

        donorRepository.save(donor);
    }

    @Override
    public DonorResponse getDonorById(Long id) {
        Donor donor = donorRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Donor profile not found with ID: " + id));
        return DonorMapper.toResponse(donor);
    }

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;

    @Override
    public void saveDonor(DonorRequest request) {
        Donor donor = new Donor();

        String email = (request.getEmail() != null && !request.getEmail().isBlank())
                ? request.getEmail().trim().toLowerCase()
                : "donor_" + System.currentTimeMillis() + "@bloodbank.com";

        User user = userRepository.findByEmail(email).orElse(null);

        // Every Donor in MySQL requires a UNIQUE user_id. If user doesn't exist or already has a Donor profile, create a new User account.
        if (user == null || donorRepository.findByUser(user).isPresent()) {
            User newUser = new User();
            newUser.setEmail(email);
            if (userRepository.existsByEmail(newUser.getEmail())) {
                newUser.setEmail("donor_" + System.currentTimeMillis() + "@bloodbank.com");
            }
            newUser.setFullName("Donor " + (request.getCity() != null ? request.getCity() : "User"));
            newUser.setPassword(passwordEncoder.encode("Password@123"));
            newUser.setCreatedAt(java.time.LocalDateTime.now());
            newUser.setEnabled(true);

            Role role = roleRepository.findByRoleNameIgnoreCase("USER")
                    .orElseGet(() -> roleRepository.findByRoleNameIgnoreCase("ROLE_USER")
                            .orElseGet(() -> {
                                Role r = new Role();
                                r.setRoleName("USER");
                                return roleRepository.save(r);
                            }));
            newUser.setRole(role);
            user = userRepository.save(newUser);
        }

        donor.setUser(user);
        donor.setBloodGroup(request.getBloodGroup() != null ? request.getBloodGroup() : com.example.demo.enums.BloodGroup.O_POSITIVE);
        donor.setGender(request.getGender() != null ? request.getGender() : com.example.demo.enums.Gender.MALE);
        donor.setDateOfBirth(request.getDateOfBirth() != null ? request.getDateOfBirth() : java.time.LocalDate.of(1998, 1, 1));
        donor.setAddress(request.getAddress() != null && !request.getAddress().isBlank() ? request.getAddress() : "N/A");
        donor.setCity(request.getCity() != null && !request.getCity().isBlank() ? request.getCity() : "Bangalore");
        donor.setState(request.getState() != null && !request.getState().isBlank() ? request.getState() : "Karnataka");
        donor.setPincode(request.getPincode() != null && !request.getPincode().isBlank() ? request.getPincode() : "560001");
        donor.setWeight(request.getWeight() != null ? request.getWeight() : 65.0);
        donor.setLastDonationDate(request.getLastDonationDate());
        donor.setAvailable(true);
        donor.setDeleted(false);
        donorRepository.save(donor);
    }

    @Override
    public void updateDonorById(Long id, DonorRequest request) {
        Donor donor = donorRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Donor profile not found with ID: " + id));

        donor.setBloodGroup(request.getBloodGroup());
        donor.setGender(request.getGender());
        donor.setDateOfBirth(request.getDateOfBirth());
        donor.setAddress(request.getAddress());
        donor.setCity(request.getCity());
        donor.setState(request.getState());
        donor.setPincode(request.getPincode());
        donor.setWeight(request.getWeight());
        if (request.getLastDonationDate() != null) {
            donor.setLastDonationDate(request.getLastDonationDate());
        }

        donorRepository.save(donor);
    }

    @Override
    public void deleteDonorById(Long id) {
        Donor donor = donorRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Donor profile not found with ID: " + id));

        donor.setDeleted(true);
        donor.setAvailable(false);
        donorRepository.save(donor);
    }

    @Override
    public List<DonorResponse> getAllDonors() {

        return donorRepository.findAllByDeletedFalse()
                .stream()
                .map(DonorMapper::toResponse)
                .toList();
    }

    @Override
    public org.springframework.data.domain.Page<DonorResponse> getDonors(int page, int size, String keyword, String sortBy, String direction) {
        String validSortBy = (sortBy == null || sortBy.isBlank()) ? "id" : sortBy;
        org.springframework.data.domain.Sort sort = "desc".equalsIgnoreCase(direction)
                ? org.springframework.data.domain.Sort.by(validSortBy).descending()
                : org.springframework.data.domain.Sort.by(validSortBy).ascending();

        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(page < 0 ? 0 : page, size <= 0 ? 10 : size, sort);

        org.springframework.data.domain.Page<Donor> donorPage;
        if (keyword == null || keyword.isBlank()) {
            donorPage = donorRepository.findAllByDeletedFalse(pageable);
        } else {
            String trimmed = keyword.trim();
            donorPage = donorRepository.findByUser_FullNameContainingIgnoreCaseOrCityContainingIgnoreCaseAndDeletedFalse(trimmed, trimmed, pageable);
        }

        return donorPage.map(DonorMapper::toResponse);
    }

    private BloodGroup parseBloodGroup(String input) {
        if (input == null || input.isBlank()) {
            return null;
        }
        String trimmed = input.trim();
        for (BloodGroup bg : BloodGroup.values()) {
            if (bg.name().equalsIgnoreCase(trimmed) || bg.getValue().equalsIgnoreCase(trimmed)) {
                return bg;
            }
        }
        return null;
    }

    @Override
    public List<DonorResponse> searchByBloodGroup(String bloodGroupStr) {
        BloodGroup bloodGroup = parseBloodGroup(bloodGroupStr);
        if (bloodGroup == null) {
            return getAllDonors();
        }
        return donorRepository
                .findByBloodGroupAndDeletedFalse(bloodGroup)
                .stream()
                .map(DonorMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<DonorResponse> searchByCity(String city) {
        if (city == null || city.isBlank()) {
            return getAllDonors();
        }
        return donorRepository
                .findByCityContainingIgnoreCaseAndDeletedFalse(city.trim())
                .stream()
                .map(DonorMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<DonorResponse> searchByBloodGroupAndCity(String bloodGroupStr, String city) {
        return searchDonors(bloodGroupStr, city);
    }

    @Override
    public List<DonorResponse> searchDonors(String bloodGroupStr, String city) {
        BloodGroup bloodGroup = parseBloodGroup(bloodGroupStr);
        String trimmedCity = (city != null && !city.isBlank()) ? city.trim() : null;

        return donorRepository.searchDonors(bloodGroup, trimmedCity)
                .stream()
                .map(DonorMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<DonorResponse> getAvailableDonors() {

        return donorRepository
                .findByAvailableTrueAndDeletedFalse()
                .stream()
                .map(DonorMapper::toResponse)
                .toList();

    }

}
