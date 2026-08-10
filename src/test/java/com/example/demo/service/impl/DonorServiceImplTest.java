
package com.example.demo.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import com.example.demo.dto.request.DonorRequest;
import com.example.demo.dto.response.DonorResponse;
import com.example.demo.entity.Donor;
import com.example.demo.entity.Role;
import com.example.demo.entity.User;
import com.example.demo.enums.BloodGroup;
import com.example.demo.enums.Gender;
import com.example.demo.repository.DonorRepository;
import com.example.demo.repository.RoleRepository;
import com.example.demo.repository.UserRepository;

import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class DonorServiceImplTest {

    @Mock
    private DonorRepository donorRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private DonorServiceImpl donorService;

    // =========================================================
    // TEST DATA HELPERS
    // =========================================================

    private User createUser() {

        User user = new User();

        user.setEmail("nagaraja@gmail.com");
        user.setFullName("Nagaraja");
        user.setEnabled(true);

        return user;
    }

    private Donor createDonor(User user) {

        Donor donor = new Donor();

        donor.setUser(user);
        donor.setBloodGroup(BloodGroup.O_POSITIVE);
        donor.setGender(Gender.MALE);
        donor.setDateOfBirth(LocalDate.of(1998, 1, 1));
        donor.setAddress("Bangalore");
        donor.setCity("Bangalore");
        donor.setState("Karnataka");
        donor.setPincode("560001");
        donor.setWeight(65.0);
        donor.setAvailable(true);
        donor.setDeleted(false);

        return donor;
    }

    private DonorRequest createDonorRequest() {

        DonorRequest request = new DonorRequest();

        request.setEmail("nagaraja@gmail.com");
        request.setBloodGroup(BloodGroup.O_POSITIVE);
        request.setGender(Gender.MALE);
        request.setDateOfBirth(LocalDate.of(1998, 1, 1));
        request.setAddress("Bangalore");
        request.setCity("Bangalore");
        request.setState("Karnataka");
        request.setPincode("560001");
        request.setWeight(65.0);

        return request;
    }

    // =========================================================
    // REGISTER DONOR
    // =========================================================

    @Test
    void registerDonor_shouldRegisterDonor_whenUserExists() {

        // Arrange

        String email = "nagaraja@gmail.com";

        User user = createUser();

        DonorRequest request = createDonorRequest();

        when(userRepository.findByEmail(email))
                .thenReturn(Optional.of(user));

        when(donorRepository.existsByUserAndDeletedFalse(user))
                .thenReturn(false);

        when(donorRepository.findByUser(user))
                .thenReturn(Optional.empty());

        // Act

        donorService.registerDonor(email, request);

        // Assert

        verify(userRepository, times(1))
                .findByEmail(email);

        verify(donorRepository, times(1))
                .existsByUserAndDeletedFalse(user);

        verify(donorRepository, times(1))
                .findByUser(user);

        verify(donorRepository, times(1))
                .save(any(Donor.class));
    }

    @Test
    void registerDonor_shouldThrowException_whenUserDoesNotExist() {

        // Arrange

        String email = "unknown@gmail.com";

        DonorRequest request = createDonorRequest();

        when(userRepository.findByEmail(email))
                .thenReturn(Optional.empty());

        // Act + Assert

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> donorService.registerDonor(email, request));

        assertEquals(
                "User not found",
                exception.getMessage());

        verify(donorRepository, never())
                .save(any(Donor.class));
    }

    @Test
    void registerDonor_shouldThrowException_whenUserAlreadyActiveDonor() {

        // Arrange

        String email = "nagaraja@gmail.com";

        User user = createUser();

        DonorRequest request = createDonorRequest();

        when(userRepository.findByEmail(email))
                .thenReturn(Optional.of(user));

        when(donorRepository.existsByUserAndDeletedFalse(user))
                .thenReturn(true);

        // Act + Assert

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> donorService.registerDonor(email, request));

        assertEquals(
                "You are already registered as an active donor.",
                exception.getMessage());

        verify(donorRepository, never())
                .save(any(Donor.class));
    }

    @Test
    void registerDonor_shouldReactivateDeletedDonor_whenPreviousRecordExists() {

        // Arrange

        String email = "nagaraja@gmail.com";

        User user = createUser();

        Donor existingDonor = createDonor(user);

        existingDonor.setDeleted(true);
        existingDonor.setAvailable(false);

        DonorRequest request = createDonorRequest();

        when(userRepository.findByEmail(email))
                .thenReturn(Optional.of(user));

        when(donorRepository.existsByUserAndDeletedFalse(user))
                .thenReturn(false);

        when(donorRepository.findByUser(user))
                .thenReturn(Optional.of(existingDonor));

        // Act

        donorService.registerDonor(email, request);

        // Assert

        assertFalse(existingDonor.getDeleted());
        assertTrue(existingDonor.getAvailable());

        assertEquals(
                BloodGroup.O_POSITIVE,
                existingDonor.getBloodGroup());

        verify(donorRepository, times(1))
                .save(existingDonor);
    }

    // =========================================================
    // GET DONOR PROFILE
    // =========================================================

    @Test
    void getDonorProfile_shouldReturnDonor_whenProfileExists() {

        // Arrange

        String email = "nagaraja@gmail.com";

        User user = createUser();

        Donor donor = createDonor(user);

        when(userRepository.findByEmail(email))
                .thenReturn(Optional.of(user));

        when(donorRepository.findByUserAndDeletedFalse(user))
                .thenReturn(Optional.of(donor));

        // Act

        DonorResponse result = donorService.getDonorProfile(email);

        // Assert

        assertNotNull(result);

        verify(userRepository, times(1))
                .findByEmail(email);

        verify(donorRepository, times(1))
                .findByUserAndDeletedFalse(user);
    }

    @Test
    void getDonorProfile_shouldThrowException_whenUserDoesNotExist() {

        // Arrange

        String email = "unknown@gmail.com";

        when(userRepository.findByEmail(email))
                .thenReturn(Optional.empty());

        // Act + Assert

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> donorService.getDonorProfile(email));

        assertEquals(
                "User not found",
                exception.getMessage());

        verify(donorRepository, never())
                .findByUserAndDeletedFalse(any(User.class));
    }

    @Test
    void getDonorProfile_shouldThrowException_whenDonorDoesNotExist() {

        // Arrange

        String email = "nagaraja@gmail.com";

        User user = createUser();

        when(userRepository.findByEmail(email))
                .thenReturn(Optional.of(user));

        when(donorRepository.findByUserAndDeletedFalse(user))
                .thenReturn(Optional.empty());

        // Act + Assert

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> donorService.getDonorProfile(email));

        assertEquals(
                "Donor profile not found",
                exception.getMessage());
    }

    // =========================================================
    // UPDATE DONOR
    // =========================================================

    @Test
    void updateDonor_shouldUpdateDonor_whenProfileExists() {

        // Arrange

        String email = "nagaraja@gmail.com";

        User user = createUser();

        Donor donor = createDonor(user);

        DonorRequest request = createDonorRequest();

        request.setCity("Mysore");
        request.setAddress("Mysore");
        request.setWeight(70.0);

        when(userRepository.findByEmail(email))
                .thenReturn(Optional.of(user));

        when(donorRepository.findByUserAndDeletedFalse(user))
                .thenReturn(Optional.of(donor));

        // Act

        DonorResponse result = donorService.updateDonor(email, request);

        // Assert

        assertNotNull(result);

        assertEquals(
                "Mysore",
                donor.getCity());

        assertEquals(
                "Mysore",
                donor.getAddress());

        assertEquals(
                70.0,
                donor.getWeight());

        verify(donorRepository, times(1))
                .save(donor);
    }

    @Test
    void updateDonor_shouldThrowException_whenUserDoesNotExist() {

        // Arrange

        String email = "unknown@gmail.com";

        DonorRequest request = createDonorRequest();

        when(userRepository.findByEmail(email))
                .thenReturn(Optional.empty());

        // Act + Assert

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> donorService.updateDonor(email, request));

        assertEquals(
                "User not found",
                exception.getMessage());

        verify(donorRepository, never())
                .save(any(Donor.class));
    }

    @Test
    void updateDonor_shouldThrowException_whenDonorDoesNotExist() {

        // Arrange

        String email = "nagaraja@gmail.com";

        User user = createUser();

        DonorRequest request = createDonorRequest();

        when(userRepository.findByEmail(email))
                .thenReturn(Optional.of(user));

        when(donorRepository.findByUserAndDeletedFalse(user))
                .thenReturn(Optional.empty());

        // Act + Assert

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> donorService.updateDonor(email, request));

        assertEquals(
                "Donor profile not found",
                exception.getMessage());

        verify(donorRepository, never())
                .save(any(Donor.class));
    }

    // =========================================================
    // GET DONOR FOR EDIT
    // =========================================================

    @Test
    void getDonorForEdit_shouldReturnRequest_whenDonorExists() {

        // Arrange

        String email = "nagaraja@gmail.com";

        User user = createUser();

        Donor donor = createDonor(user);

        when(userRepository.findByEmail(email))
                .thenReturn(Optional.of(user));

        when(donorRepository.findByUserAndDeletedFalse(user))
                .thenReturn(Optional.of(donor));

        // Act

        DonorRequest result = donorService.getDonorForEdit(email);

        // Assert

        assertNotNull(result);

        verify(userRepository, times(1))
                .findByEmail(email);

        verify(donorRepository, times(1))
                .findByUserAndDeletedFalse(user);
    }

    @Test
    void getDonorForEdit_shouldThrowException_whenUserDoesNotExist() {

        // Arrange

        String email = "unknown@gmail.com";

        when(userRepository.findByEmail(email))
                .thenReturn(Optional.empty());

        // Act + Assert

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> donorService.getDonorForEdit(email));

        assertEquals(
                "User not found",
                exception.getMessage());
    }

    @Test
    void getDonorForEdit_shouldThrowException_whenDonorDoesNotExist() {

        // Arrange

        String email = "nagaraja@gmail.com";

        User user = createUser();

        when(userRepository.findByEmail(email))
                .thenReturn(Optional.of(user));

        when(donorRepository.findByUserAndDeletedFalse(user))
                .thenReturn(Optional.empty());

        // Act + Assert

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> donorService.getDonorForEdit(email));

        assertEquals(
                "Donor profile not found",
                exception.getMessage());
    }

    // =========================================================
    // DELETE DONOR
    // =========================================================

    @Test
    void deleteDonor_shouldSoftDeleteDonor_whenDonorExists() {

        // Arrange

        String email = "nagaraja@gmail.com";

        User user = createUser();

        Donor donor = createDonor(user);

        when(userRepository.findByEmail(email))
                .thenReturn(Optional.of(user));

        when(donorRepository.findByUserAndDeletedFalse(user))
                .thenReturn(Optional.of(donor));

        // Act

        donorService.deleteDonor(email);

        // Assert

        assertTrue(donor.getDeleted());
        assertFalse(donor.getAvailable());

        verify(donorRepository, times(1))
                .save(donor);
    }

    @Test
    void deleteDonor_shouldThrowException_whenUserDoesNotExist() {

        // Arrange

        String email = "unknown@gmail.com";

        when(userRepository.findByEmail(email))
                .thenReturn(Optional.empty());

        // Act + Assert

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> donorService.deleteDonor(email));

        assertEquals(
                "User not found",
                exception.getMessage());

        verify(donorRepository, never())
                .save(any(Donor.class));
    }

    @Test
    void deleteDonor_shouldThrowException_whenDonorDoesNotExist() {

        // Arrange

        String email = "nagaraja@gmail.com";

        User user = createUser();

        when(userRepository.findByEmail(email))
                .thenReturn(Optional.of(user));

        when(donorRepository.findByUserAndDeletedFalse(user))
                .thenReturn(Optional.empty());

        // Act + Assert

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> donorService.deleteDonor(email));

        assertEquals(
                "Donor profile not found",
                exception.getMessage());

        verify(donorRepository, never())
                .save(any(Donor.class));
    }

    // =========================================================
    // GET DONOR BY ID
    // =========================================================

    @Test
    void getDonorById_shouldReturnDonor_whenDonorExists() {

        // Arrange

        Long donorId = 1L;

        User user = createUser();

        Donor donor = createDonor(user);

        when(donorRepository.findById(donorId))
                .thenReturn(Optional.of(donor));

        // Act

        DonorResponse result = donorService.getDonorById(donorId);

        // Assert

        assertNotNull(result);

        verify(donorRepository, times(1))
                .findById(donorId);
    }

    @Test
    void getDonorById_shouldThrowException_whenDonorDoesNotExist() {

        // Arrange

        Long donorId = 999L;

        when(donorRepository.findById(donorId))
                .thenReturn(Optional.empty());

        // Act + Assert

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> donorService.getDonorById(donorId));

        assertEquals(
                "Donor profile not found with ID: 999",
                exception.getMessage());

        verify(donorRepository, times(1))
                .findById(donorId);
    }

    // =========================================================
    // UPDATE DONOR BY ID
    // =========================================================

    @Test
    void updateDonorById_shouldUpdateDonor_whenDonorExists() {

        // Arrange

        Long donorId = 1L;

        User user = createUser();

        Donor donor = createDonor(user);

        DonorRequest request = createDonorRequest();

        request.setCity("Mysore");
        request.setState("Karnataka");
        request.setWeight(72.0);

        when(donorRepository.findById(donorId))
                .thenReturn(Optional.of(donor));

        // Act

        donorService.updateDonorById(donorId, request);

        // Assert

        assertEquals(
                "Mysore",
                donor.getCity());

        assertEquals(
                72.0,
                donor.getWeight());

        verify(donorRepository, times(1))
                .save(donor);
    }

    @Test
    void updateDonorById_shouldThrowException_whenDonorDoesNotExist() {

        // Arrange

        Long donorId = 999L;

        DonorRequest request = createDonorRequest();

        when(donorRepository.findById(donorId))
                .thenReturn(Optional.empty());

        // Act + Assert

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> donorService.updateDonorById(donorId, request));

        assertEquals(
                "Donor profile not found with ID: 999",
                exception.getMessage());

        verify(donorRepository, never())
                .save(any(Donor.class));
    }

    // =========================================================
    // DELETE DONOR BY ID
    // =========================================================

    @Test
    void deleteDonorById_shouldSoftDeleteDonor_whenDonorExists() {

        // Arrange

        Long donorId = 1L;

        User user = createUser();

        Donor donor = createDonor(user);

        when(donorRepository.findById(donorId))
                .thenReturn(Optional.of(donor));

        // Act

        donorService.deleteDonorById(donorId);

        // Assert

        assertTrue(donor.getDeleted());
        assertFalse(donor.getAvailable());

        verify(donorRepository, times(1))
                .save(donor);
    }

    @Test
    void deleteDonorById_shouldThrowException_whenDonorDoesNotExist() {

        // Arrange

        Long donorId = 999L;

        when(donorRepository.findById(donorId))
                .thenReturn(Optional.empty());

        // Act + Assert

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> donorService.deleteDonorById(donorId));

        assertEquals(
                "Donor profile not found with ID: 999",
                exception.getMessage());

        verify(donorRepository, never())
                .save(any(Donor.class));
    }

    // =========================================================
    // GET ALL DONORS
    // =========================================================

    @Test
    void getAllDonors_shouldReturnDonors_whenDonorsExist() {

        // Arrange

        User user = createUser();

        Donor donor1 = createDonor(user);
        Donor donor2 = createDonor(user);

        when(donorRepository.findAllByDeletedFalse())
                .thenReturn(Arrays.asList(donor1, donor2));

        // Act

        List<DonorResponse> result = donorService.getAllDonors();

        // Assert

        assertNotNull(result);
        assertEquals(2, result.size());

        verify(donorRepository, times(1))
                .findAllByDeletedFalse();
    }

    @Test
    void getAllDonors_shouldReturnEmptyList_whenNoDonorsExist() {

        // Arrange

        when(donorRepository.findAllByDeletedFalse())
                .thenReturn(Collections.emptyList());

        // Act

        List<DonorResponse> result = donorService.getAllDonors();

        // Assert

        assertNotNull(result);
        assertTrue(result.isEmpty());

        verify(donorRepository, times(1))
                .findAllByDeletedFalse();
    }

    // =========================================================
    // SEARCH BY BLOOD GROUP
    // =========================================================

    @Test
    void searchByBloodGroup_shouldReturnMatchingDonors_whenValidBloodGroupProvided() {

        // Arrange

        User user = createUser();

        Donor donor = createDonor(user);

        when(donorRepository.findByBloodGroupAndDeletedFalse(
                BloodGroup.O_POSITIVE))
                .thenReturn(Collections.singletonList(donor));

        // Act

        List<DonorResponse> result = donorService.searchByBloodGroup("O_POSITIVE");

        // Assert

        assertNotNull(result);
        assertEquals(1, result.size());

        verify(donorRepository, times(1))
                .findByBloodGroupAndDeletedFalse(
                        BloodGroup.O_POSITIVE);
    }

    @Test
    void searchByBloodGroup_shouldReturnAllDonors_whenInvalidBloodGroupProvided() {

        // Arrange

        User user = createUser();

        Donor donor = createDonor(user);

        when(donorRepository.findAllByDeletedFalse())
                .thenReturn(Collections.singletonList(donor));

        // Act

        List<DonorResponse> result = donorService.searchByBloodGroup("INVALID");

        // Assert

        assertNotNull(result);
        assertEquals(1, result.size());

        verify(donorRepository, times(1))
                .findAllByDeletedFalse();

        verify(donorRepository, never())
                .findByBloodGroupAndDeletedFalse(any(BloodGroup.class));
    }

    // =========================================================
    // SEARCH BY CITY
    // =========================================================

    @Test
    void searchByCity_shouldReturnMatchingDonors_whenCityProvided() {

        // Arrange

        User user = createUser();

        Donor donor = createDonor(user);

        when(donorRepository
                .findByCityContainingIgnoreCaseAndDeletedFalse("Bangalore"))
                .thenReturn(Collections.singletonList(donor));

        // Act

        List<DonorResponse> result = donorService.searchByCity("Bangalore");

        // Assert

        assertNotNull(result);
        assertEquals(1, result.size());

        verify(donorRepository, times(1))
                .findByCityContainingIgnoreCaseAndDeletedFalse(
                        "Bangalore");
    }

    @Test
    void searchByCity_shouldReturnAllDonors_whenCityIsEmpty() {

        // Arrange

        User user = createUser();

        Donor donor = createDonor(user);

        when(donorRepository.findAllByDeletedFalse())
                .thenReturn(Collections.singletonList(donor));

        // Act

        List<DonorResponse> result = donorService.searchByCity(" ");

        // Assert

        assertNotNull(result);
        assertEquals(1, result.size());

        verify(donorRepository, times(1))
                .findAllByDeletedFalse();
    }

    // =========================================================
    // SEARCH DONORS BY BLOOD GROUP AND CITY
    // =========================================================

    @Test
    void searchDonors_shouldReturnMatchingDonors_whenBloodGroupAndCityProvided() {

        // Arrange

        User user = createUser();

        Donor donor = createDonor(user);

        when(donorRepository.searchDonors(
                BloodGroup.O_POSITIVE,
                "Bangalore"))
                .thenReturn(Collections.singletonList(donor));

        // Act

        List<DonorResponse> result = donorService.searchDonors(
                "O_POSITIVE",
                "Bangalore");

        // Assert

        assertNotNull(result);
        assertEquals(1, result.size());

        verify(donorRepository, times(1))
                .searchDonors(
                        BloodGroup.O_POSITIVE,
                        "Bangalore");
    }

    @Test
    void searchDonors_shouldAllowNullBloodGroup() {

        // Arrange

        User user = createUser();

        Donor donor = createDonor(user);

        when(donorRepository.searchDonors(
                null,
                "Bangalore"))
                .thenReturn(Collections.singletonList(donor));

        // Act

        List<DonorResponse> result = donorService.searchDonors(
                "INVALID",
                "Bangalore");

        // Assert

        assertNotNull(result);
        assertEquals(1, result.size());

        verify(donorRepository, times(1))
                .searchDonors(
                        null,
                        "Bangalore");
    }

    // =========================================================
    // GET AVAILABLE DONORS
    // =========================================================

    @Test
    void getAvailableDonors_shouldReturnAvailableDonors() {

        // Arrange

        User user = createUser();

        Donor donor = createDonor(user);

        when(donorRepository
                .findByAvailableTrueAndDeletedFalse())
                .thenReturn(Collections.singletonList(donor));

        // Act

        List<DonorResponse> result = donorService.getAvailableDonors();

        // Assert

        assertNotNull(result);
        assertEquals(1, result.size());

        verify(donorRepository, times(1))
                .findByAvailableTrueAndDeletedFalse();
    }

    @Test
    void getAvailableDonors_shouldReturnEmptyList_whenNoAvailableDonorsExist() {

        // Arrange

        when(donorRepository
                .findByAvailableTrueAndDeletedFalse())
                .thenReturn(Collections.emptyList());

        // Act

        List<DonorResponse> result = donorService.getAvailableDonors();

        // Assert

        assertNotNull(result);
        assertTrue(result.isEmpty());

        verify(donorRepository, times(1))
                .findByAvailableTrueAndDeletedFalse();
    }

    // =========================================================
    // PAGINATION
    // =========================================================

    @Test
    void getDonors_shouldReturnPaginatedDonors_whenNoKeywordProvided() {

        // Arrange

        User user = createUser();

        Donor donor = createDonor(user);

        Page<Donor> donorPage = new PageImpl<>(Collections.singletonList(donor));

        when(donorRepository.findAllByDeletedFalse(
                any(Pageable.class)))
                .thenReturn(donorPage);

        // Act

        Page<DonorResponse> result = donorService.getDonors(
                0,
                10,
                null,
                "id",
                "asc");

        // Assert

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());

        verify(donorRepository, times(1))
                .findAllByDeletedFalse(
                        any(Pageable.class));
    }

    @Test
    void getDonors_shouldSearchByKeyword_whenKeywordProvided() {

        // Arrange

        User user = createUser();

        Donor donor = createDonor(user);

        Page<Donor> donorPage = new PageImpl<>(Collections.singletonList(donor));

        when(donorRepository
                .findByUser_FullNameContainingIgnoreCaseOrCityContainingIgnoreCaseAndDeletedFalse(
                        eq("Bangalore"),
                        eq("Bangalore"),
                        any(Pageable.class)))
                .thenReturn(donorPage);

        // Act

        Page<DonorResponse> result = donorService.getDonors(
                0,
                10,
                "Bangalore",
                "id",
                "asc");

        // Assert

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());

        verify(donorRepository, times(1))
                .findByUser_FullNameContainingIgnoreCaseOrCityContainingIgnoreCaseAndDeletedFalse(
                        eq("Bangalore"),
                        eq("Bangalore"),
                        any(Pageable.class));
    }

    @Test
    void getDonors_shouldUseDefaultPageAndSize_whenInvalidPaginationValuesProvided() {

        // Arrange

        User user = createUser();

        Donor donor = createDonor(user);

        Page<Donor> donorPage = new PageImpl<>(Collections.singletonList(donor));

        when(donorRepository.findAllByDeletedFalse(
                any(Pageable.class)))
                .thenReturn(donorPage);

        // Act

        Page<DonorResponse> result = donorService.getDonors(
                -1,
                0,
                null,
                null,
                null);

        // Assert

        assertNotNull(result);

        verify(donorRepository, times(1))
                .findAllByDeletedFalse(
                        any(Pageable.class));
    }
}
