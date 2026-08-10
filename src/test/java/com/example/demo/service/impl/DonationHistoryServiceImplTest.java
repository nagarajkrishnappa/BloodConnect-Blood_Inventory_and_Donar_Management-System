
package com.example.demo.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
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

import com.example.demo.dto.request.DonationHistoryRequest;
import com.example.demo.dto.response.DonationHistoryResponse;
import com.example.demo.entity.BloodStock;
import com.example.demo.entity.DonationHistory;
import com.example.demo.entity.Donor;
import com.example.demo.repository.BloodStockRepository;
import com.example.demo.repository.DonationHistoryRepository;
import com.example.demo.repository.DonorRepository;
import com.example.demo.service.AuditLogService;

@ExtendWith(MockitoExtension.class)
class DonationHistoryServiceImplTest {

    @Mock
    private DonationHistoryRepository donationHistoryRepository;

    @Mock
    private DonorRepository donorRepository;

    @Mock
    private BloodStockRepository bloodStockRepository;

    @Mock
    private AuditLogService auditLogService;

    @InjectMocks
    private DonationHistoryServiceImpl donationHistoryService;

    // =========================================================
    // TEST DATA
    // =========================================================

    private Donor createDonor() {

        Donor donor = new Donor();

        donor.setId(1L);
        donor.setBloodGroup(com.example.demo.enums.BloodGroup.O_POSITIVE);
        donor.setLastDonationDate(null);

        return donor;
    }

    private DonationHistory createDonationHistory() {

        DonationHistory history = new DonationHistory();

        history.setId(1L);
        history.setDonationDate(
                LocalDate.of(2026, 8, 1));
        history.setUnitsDonated(2);
        history.setRemarks("Regular donation");
        history.setDeleted(false);

        return history;
    }

    private BloodStock createBloodStock() {

        BloodStock stock = new BloodStock();

        stock.setId(1L);
        stock.setBloodGroup(
                com.example.demo.enums.BloodGroup.O_POSITIVE);
        stock.setUnitsAvailable(10);

        return stock;
    }

    private DonationHistoryRequest createRequest() {

        DonationHistoryRequest request = new DonationHistoryRequest();

        request.setDonorId(1L);

        request.setDonationDate(
                LocalDate.of(2026, 8, 1));

        request.setUnitsDonated(2);

        request.setRemarks(
                "Regular donation");

        return request;
    }

    // =========================================================
    // RECORD DONATION
    // =========================================================

    @Test
    void recordDonation_shouldRecordDonationAndUpdateStock() {

        // Arrange

        DonationHistoryRequest request = createRequest();

        Donor donor = createDonor();

        BloodStock stock = createBloodStock();

        when(donorRepository.findById(1L))
                .thenReturn(Optional.of(donor));

        when(bloodStockRepository
                .findByBloodGroup(
                        donor.getBloodGroup()))
                .thenReturn(Optional.of(stock));

        // Act

        donationHistoryService.recordDonation(
                request);

        // Assert

        verify(
                donorRepository,
                times(1)).findById(1L);

        verify(
                donationHistoryRepository,
                times(1)).save(any(DonationHistory.class));

        verify(
                donorRepository,
                times(1)).save(donor);

        verify(
                bloodStockRepository,
                times(1)).findByBloodGroup(
                        donor.getBloodGroup());

        verify(
                bloodStockRepository,
                times(1)).save(stock);

        verify(
                auditLogService,
                times(1)).saveLog(
                        "Admin",
                        "ADD",
                        "Donation",
                        "Donation recorded successfully.");

        // 10 existing + 2 donated
        assertEquals(
                12,
                stock.getUnitsAvailable());

        // Donor's last donation date updated
        assertEquals(
                request.getDonationDate(),
                donor.getLastDonationDate());
    }

    // =========================================================
    // RECORD DONATION - DONOR NOT FOUND
    // =========================================================

    @Test
    void recordDonation_shouldThrowException_whenDonorDoesNotExist() {

        // Arrange

        DonationHistoryRequest request = createRequest();

        when(donorRepository.findById(1L))
                .thenReturn(Optional.empty());

        // Act + Assert

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> donationHistoryService
                        .recordDonation(request));

        assertEquals(
                "Donor not found",
                exception.getMessage());

        verify(
                donationHistoryRepository,
                never()).save(any(DonationHistory.class));

        verify(
                donorRepository,
                never()).save(any(Donor.class));

        verify(
                bloodStockRepository,
                never()).save(any(BloodStock.class));

        verify(
                auditLogService,
                never()).saveLog(
                        anyString(),
                        anyString(),
                        anyString(),
                        anyString());
    }

    // =========================================================
    // RECORD DONATION - DONATION DATE NULL
    // =========================================================

    @Test
    void recordDonation_shouldNotUpdateLastDonationDate_whenDateIsNull() {

        // Arrange

        DonationHistoryRequest request = createRequest();

        request.setDonationDate(null);

        Donor donor = createDonor();

        BloodStock stock = createBloodStock();

        when(donorRepository.findById(1L))
                .thenReturn(Optional.of(donor));

        when(bloodStockRepository
                .findByBloodGroup(
                        donor.getBloodGroup()))
                .thenReturn(Optional.of(stock));

        // Act

        donationHistoryService.recordDonation(
                request);

        // Assert

        assertEquals(
                null,
                donor.getLastDonationDate());

        verify(
                donationHistoryRepository,
                times(1)).save(any(DonationHistory.class));

        verify(
                donorRepository,
                times(1)).save(donor);

        verify(
                bloodStockRepository,
                times(1)).save(stock);
    }

    // =========================================================
    // RECORD DONATION - DONOR WITHOUT BLOOD GROUP
    // =========================================================

    @Test
    void recordDonation_shouldNotUpdateBloodStock_whenBloodGroupIsNull() {

        // Arrange

        DonationHistoryRequest request = createRequest();

        Donor donor = createDonor();

        donor.setBloodGroup(null);

        when(donorRepository.findById(1L))
                .thenReturn(Optional.of(donor));

        // Act

        donationHistoryService.recordDonation(
                request);

        // Assert

        verify(
                donationHistoryRepository,
                times(1)).save(any(DonationHistory.class));

        verify(
                donorRepository,
                times(1)).save(donor);

        verify(
                bloodStockRepository,
                never()).findByBloodGroup(any());

        verify(
                bloodStockRepository,
                never()).save(any(BloodStock.class));

        verify(
                auditLogService,
                times(1)).saveLog(
                        "Admin",
                        "ADD",
                        "Donation",
                        "Donation recorded successfully.");
    }

    // =========================================================
    // RECORD DONATION - STOCK DOES NOT EXIST
    // =========================================================

    @Test
    void recordDonation_shouldCreateStock_whenBloodStockDoesNotExist() {

        // Arrange

        DonationHistoryRequest request = createRequest();

        Donor donor = createDonor();

        when(donorRepository.findById(1L))
                .thenReturn(Optional.of(donor));

        when(bloodStockRepository
                .findByBloodGroup(
                        donor.getBloodGroup()))
                .thenReturn(Optional.empty());

        BloodStock newStock = createBloodStock();

        newStock.setUnitsAvailable(0);

        when(bloodStockRepository.save(
                any(BloodStock.class)))
                .thenReturn(newStock);

        // Act

        donationHistoryService.recordDonation(
                request);

        // Assert

        verify(
                bloodStockRepository,
                times(2)).save(any(BloodStock.class));

        assertEquals(
                2,
                newStock.getUnitsAvailable());

        verify(
                donationHistoryRepository,
                times(1)).save(any(DonationHistory.class));

        verify(
                donorRepository,
                times(1)).save(donor);
    }

    // =========================================================
    // RECORD DONATION - NULL EXISTING STOCK
    // =========================================================

    @Test
    void recordDonation_shouldTreatNullStockUnitsAsZero() {

        // Arrange

        DonationHistoryRequest request = createRequest();

        Donor donor = createDonor();

        BloodStock stock = createBloodStock();

        stock.setUnitsAvailable(null);

        when(donorRepository.findById(1L))
                .thenReturn(Optional.of(donor));

        when(bloodStockRepository
                .findByBloodGroup(
                        donor.getBloodGroup()))
                .thenReturn(Optional.of(stock));

        // Act

        donationHistoryService.recordDonation(
                request);

        // Assert

        assertEquals(
                2,
                stock.getUnitsAvailable());

        verify(
                bloodStockRepository,
                times(1)).save(stock);
    }

    // =========================================================
    // RECORD DONATION - NULL DONATED UNITS
    // =========================================================

    @Test
    void recordDonation_shouldTreatNullDonatedUnitsAsZero() {

        // Arrange

        DonationHistoryRequest request = createRequest();

        request.setUnitsDonated(null);

        Donor donor = createDonor();

        BloodStock stock = createBloodStock();

        when(donorRepository.findById(1L))
                .thenReturn(Optional.of(donor));

        when(bloodStockRepository
                .findByBloodGroup(
                        donor.getBloodGroup()))
                .thenReturn(Optional.of(stock));

        // Act

        donationHistoryService.recordDonation(
                request);

        // Assert

        assertEquals(
                10,
                stock.getUnitsAvailable());

        verify(
                bloodStockRepository,
                times(1)).save(stock);
    }

    // =========================================================
    // SAVE DONATION
    // =========================================================

    @Test
    void saveDonation_shouldDelegateToRecordDonation() {

        // Arrange

        DonationHistoryRequest request = createRequest();

        Donor donor = createDonor();

        BloodStock stock = createBloodStock();

        when(donorRepository.findById(1L))
                .thenReturn(Optional.of(donor));

        when(bloodStockRepository
                .findByBloodGroup(
                        donor.getBloodGroup()))
                .thenReturn(Optional.of(stock));

        // Act

        donationHistoryService.saveDonation(
                request);

        // Assert

        verify(
                donorRepository,
                times(1)).findById(1L);

        verify(
                donationHistoryRepository,
                times(1)).save(any(DonationHistory.class));

        verify(
                auditLogService,
                times(1)).saveLog(
                        "Admin",
                        "ADD",
                        "Donation",
                        "Donation recorded successfully.");
    }

    // =========================================================
    // GET DONATION BY ID
    // =========================================================

    @Test
    void getDonationById_shouldReturnDonation_whenExists() {

        // Arrange

        Long id = 1L;

        DonationHistory history = createDonationHistory();

        when(
                donationHistoryRepository.findById(id)).thenReturn(Optional.of(history));

        // Act

        DonationHistoryResponse result = donationHistoryService
                .getDonationById(id);

        // Assert

        assertNotNull(result);

        verify(
                donationHistoryRepository,
                times(1)).findById(id);
    }

    @Test
    void getDonationById_shouldThrowException_whenNotFound() {

        // Arrange

        Long id = 999L;

        when(
                donationHistoryRepository.findById(id)).thenReturn(Optional.empty());

        // Act + Assert

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> donationHistoryService
                        .getDonationById(id));

        assertEquals(
                "Donation history record not found with ID: 999",
                exception.getMessage());
    }

    // =========================================================
    // UPDATE DONATION
    // =========================================================

    @Test
    void updateDonation_shouldUpdateAllProvidedFields() {

        // Arrange

        Long id = 1L;

        DonationHistory history = createDonationHistory();

        DonationHistoryRequest request = createRequest();

        request.setDonationDate(
                LocalDate.of(2026, 8, 5));

        request.setUnitsDonated(3);

        request.setRemarks(
                "Updated donation");

        when(
                donationHistoryRepository.findById(id)).thenReturn(Optional.of(history));

        // Act

        donationHistoryService.updateDonation(
                id,
                request);

        // Assert

        assertEquals(
                LocalDate.of(2026, 8, 5),
                history.getDonationDate());

        assertEquals(
                3,
                history.getUnitsDonated());

        assertEquals(
                "Updated donation",
                history.getRemarks());

        verify(
                donationHistoryRepository,
                times(1)).save(history);
    }

    @Test
    void updateDonation_shouldUpdateOnlyProvidedFields() {

        // Arrange

        Long id = 1L;

        DonationHistory history = createDonationHistory();

        LocalDate originalDate = history.getDonationDate();

        Integer originalUnits = history.getUnitsDonated();

        DonationHistoryRequest request = new DonationHistoryRequest();

        request.setDonationDate(null);
        request.setUnitsDonated(null);
        request.setRemarks(null);

        when(
                donationHistoryRepository.findById(id)).thenReturn(Optional.of(history));

        // Act

        donationHistoryService.updateDonation(
                id,
                request);

        // Assert

        assertEquals(
                originalDate,
                history.getDonationDate());

        assertEquals(
                originalUnits,
                history.getUnitsDonated());

        verify(
                donationHistoryRepository,
                times(1)).save(history);
    }

    @Test
    void updateDonation_shouldThrowException_whenDonationDoesNotExist() {

        // Arrange

        Long id = 999L;

        DonationHistoryRequest request = createRequest();

        when(
                donationHistoryRepository.findById(id)).thenReturn(Optional.empty());

        // Act + Assert

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> donationHistoryService
                        .updateDonation(
                                id,
                                request));

        assertEquals(
                "Donation history record not found with ID: 999",
                exception.getMessage());

        verify(
                donationHistoryRepository,
                never()).save(any(DonationHistory.class));
    }

    // =========================================================
    // DELETE DONATION
    // =========================================================

    @Test
    void deleteDonation_shouldSoftDeleteDonation() {

        // Arrange

        Long id = 1L;

        DonationHistory history = createDonationHistory();

        history.setDeleted(false);

        when(
                donationHistoryRepository.findById(id)).thenReturn(Optional.of(history));

        // Act

        donationHistoryService.deleteDonation(id);

        // Assert

        assertTrue(
                history.getDeleted());

        verify(
                donationHistoryRepository,
                times(1)).save(history);
    }

    @Test
    void deleteDonation_shouldThrowException_whenDonationDoesNotExist() {

        // Arrange

        Long id = 999L;

        when(
                donationHistoryRepository.findById(id)).thenReturn(Optional.empty());

        // Act + Assert

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> donationHistoryService
                        .deleteDonation(id));

        assertEquals(
                "Donation history record not found with ID: 999",
                exception.getMessage());

        verify(
                donationHistoryRepository,
                never()).save(any(DonationHistory.class));
    }

    // =========================================================
    // GET ALL DONATIONS
    // =========================================================

    @Test
    void getAllDonations_shouldReturnAllActiveDonations() {

        // Arrange

        DonationHistory history = createDonationHistory();

        when(
                donationHistoryRepository
                        .findAllByDeletedFalse())
                .thenReturn(List.of(history));

        // Act

        List<DonationHistoryResponse> result = donationHistoryService
                .getAllDonations();

        // Assert

        assertNotNull(result);

        assertEquals(
                1,
                result.size());

        verify(
                donationHistoryRepository,
                times(1)).findAllByDeletedFalse();
    }

    @Test
    void getAllDonations_shouldReturnEmptyList_whenNoDonationsExist() {

        // Arrange

        when(
                donationHistoryRepository
                        .findAllByDeletedFalse())
                .thenReturn(Collections.emptyList());

        // Act

        List<DonationHistoryResponse> result = donationHistoryService
                .getAllDonations();

        // Assert

        assertNotNull(result);

        assertTrue(
                result.isEmpty());
    }

    // =========================================================
    // PAGINATION
    // =========================================================

    @Test
    void getDonations_shouldReturnPaginatedResults_withoutKeyword() {

        // Arrange

        DonationHistory history = createDonationHistory();

        Page<DonationHistory> page = new PageImpl<>(
                List.of(history));

        when(
                donationHistoryRepository
                        .findAllByDeletedFalse(
                                any(Pageable.class)))
                .thenReturn(page);

        // Act

        Page<DonationHistoryResponse> result = donationHistoryService.getDonations(
                0,
                10,
                null,
                "id",
                "asc");

        // Assert

        assertNotNull(result);

        assertEquals(
                1,
                result.getTotalElements());

        verify(
                donationHistoryRepository,
                times(1)).findAllByDeletedFalse(
                        any(Pageable.class));
    }

    @Test
    void getDonations_shouldSearchByDonorName_whenKeywordProvided() {

        // Arrange

        DonationHistory history = createDonationHistory();

        Page<DonationHistory> page = new PageImpl<>(
                List.of(history));

        when(
                donationHistoryRepository
                        .findByDonor_User_FullNameContainingIgnoreCaseAndDeletedFalse(
                                eq("Test User"),
                                any(Pageable.class)))
                .thenReturn(page);

        // Act

        Page<DonationHistoryResponse> result = donationHistoryService.getDonations(
                0,
                10,
                "Test User",
                "id",
                "asc");

        // Assert

        assertNotNull(result);

        assertEquals(
                1,
                result.getTotalElements());

        verify(
                donationHistoryRepository,
                times(1)).findByDonor_User_FullNameContainingIgnoreCaseAndDeletedFalse(
                        eq("Test User"),
                        any(Pageable.class));
    }

    @Test
    void getDonations_shouldUseDefaultPageAndSize_whenInvalidValuesProvided() {

        // Arrange

        DonationHistory history = createDonationHistory();

        Page<DonationHistory> page = new PageImpl<>(
                List.of(history));

        when(
                donationHistoryRepository
                        .findAllByDeletedFalse(
                                any(Pageable.class)))
                .thenReturn(page);

        // Act

        Page<DonationHistoryResponse> result = donationHistoryService.getDonations(
                -1,
                0,
                null,
                null,
                null);

        // Assert

        assertNotNull(result);

        assertEquals(
                1,
                result.getTotalElements());

        verify(
                donationHistoryRepository,
                times(1)).findAllByDeletedFalse(
                        any(Pageable.class));
    }
}
