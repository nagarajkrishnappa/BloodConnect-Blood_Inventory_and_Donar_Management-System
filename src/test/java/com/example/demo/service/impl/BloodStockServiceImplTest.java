
package com.example.demo.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
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

import com.example.demo.dto.request.BloodStockRequest;
import com.example.demo.dto.response.BloodStockResponse;
import com.example.demo.entity.BloodStock;
import com.example.demo.enums.BloodGroup;
import com.example.demo.repository.BloodStockRepository;
import com.example.demo.service.AuditLogService;

@ExtendWith(MockitoExtension.class)
class BloodStockServiceImplTest {

    @Mock
    private BloodStockRepository bloodStockRepository;

    @Mock
    private AuditLogService auditLogService;

    @InjectMocks
    private BloodStockServiceImpl bloodStockService;

    // =========================================================
    // TEST DATA
    // =========================================================

    private BloodStock createBloodStock() {

        BloodStock stock = new BloodStock();

        stock.setBloodGroup(BloodGroup.O_POSITIVE);
        stock.setUnits(100);
        stock.setLastUpdated(LocalDateTime.now());

        return stock;
    }

    private BloodStockRequest createRequest() {

        BloodStockRequest request = new BloodStockRequest();

        request.setBloodGroup(BloodGroup.O_POSITIVE);
        request.setUnits(50);

        return request;
    }

    // =========================================================
    // UPDATE BLOOD STOCK
    // =========================================================

    @Test
    void updateBloodStock_shouldUpdateStock_whenStockExists() {

        // Arrange

        Long id = 1L;

        BloodStock stock = createBloodStock();

        BloodStockRequest request = createRequest();

        request.setUnits(150);

        when(bloodStockRepository.findById(id))
                .thenReturn(Optional.of(stock));

        // Act

        bloodStockService.updateBloodStock(id, request);

        // Assert

        assertEquals(
                BloodGroup.O_POSITIVE,
                stock.getBloodGroup());

        assertEquals(
                150,
                stock.getUnits());

        assertNotNull(stock.getLastUpdated());

        verify(bloodStockRepository, times(1))
                .findById(id);

        verify(bloodStockRepository, times(1))
                .save(stock);

        verify(auditLogService, times(1))
                .saveLog(
                        "Admin",
                        "UPDATE",
                        "Blood Stock",
                        "Updated Blood Stock ID : " + id);
    }

    @Test
    void updateBloodStock_shouldThrowException_whenStockDoesNotExist() {

        // Arrange

        Long id = 999L;

        BloodStockRequest request = createRequest();

        when(bloodStockRepository.findById(id))
                .thenReturn(Optional.empty());

        // Act + Assert

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> bloodStockService.updateBloodStock(id, request));

        assertEquals(
                "Blood stock not found.",
                exception.getMessage());

        verify(bloodStockRepository, never())
                .save(any(BloodStock.class));

        verify(auditLogService, never())
                .saveLog(
                        any(),
                        any(),
                        any(),
                        any());
    }

    // =========================================================
    // GET ALL BLOOD STOCK
    // =========================================================

    @Test
    void getAllBloodStock_shouldReturnAllStock() {

        // Arrange

        BloodStock stock = createBloodStock();

        when(bloodStockRepository.findAll())
                .thenReturn(List.of(stock));

        // Act

        List<BloodStockResponse> result = bloodStockService.getAllBloodStock();

        // Assert

        assertNotNull(result);
        assertEquals(1, result.size());

        verify(bloodStockRepository, times(1))
                .findAll();
    }

    @Test
    void getAllBloodStock_shouldReturnEmptyList_whenNoStockExists() {

        // Arrange

        when(bloodStockRepository.findAll())
                .thenReturn(Collections.emptyList());

        // Act

        List<BloodStockResponse> result = bloodStockService.getAllBloodStock();

        // Assert

        assertNotNull(result);
        assertTrue(result.isEmpty());

        verify(bloodStockRepository, times(1))
                .findAll();
    }

    // =========================================================
    // GET BLOOD STOCK BY GROUP
    // =========================================================

    @Test
    void getBloodStockByGroup_shouldReturnStock_whenGroupExists() {

        // Arrange

        BloodStock stock = createBloodStock();

        when(bloodStockRepository.findByBloodGroup(
                BloodGroup.O_POSITIVE))
                .thenReturn(Optional.of(stock));

        // Act

        BloodStockResponse result = bloodStockService.getBloodStockByGroup(
                BloodGroup.O_POSITIVE);

        // Assert

        assertNotNull(result);

        verify(bloodStockRepository, times(1))
                .findByBloodGroup(
                        BloodGroup.O_POSITIVE);
    }

    @Test
    void getBloodStockByGroup_shouldThrowException_whenGroupDoesNotExist() {

        // Arrange

        when(bloodStockRepository.findByBloodGroup(
                BloodGroup.O_POSITIVE))
                .thenReturn(Optional.empty());

        // Act + Assert

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> bloodStockService.getBloodStockByGroup(
                        BloodGroup.O_POSITIVE));

        assertEquals(
                "Blood group not found.",
                exception.getMessage());
    }

    // =========================================================
    // INCREASE UNITS
    // =========================================================

    @Test
    void increaseUnits_shouldIncreaseStockUnits() {

        // Arrange

        BloodStock stock = createBloodStock();

        stock.setUnits(100);

        when(bloodStockRepository.findByBloodGroup(
                BloodGroup.O_POSITIVE))
                .thenReturn(Optional.of(stock));

        // Act

        bloodStockService.increaseUnits(
                BloodGroup.O_POSITIVE,
                25);

        // Assert

        assertEquals(
                125,
                stock.getUnits());

        assertNotNull(stock.getLastUpdated());

        verify(bloodStockRepository, times(1))
                .save(stock);
    }

    @Test
    void increaseUnits_shouldTreatNullUnitsAsZero() {

        // Arrange

        BloodStock stock = createBloodStock();

        stock.setUnits(100);

        when(bloodStockRepository.findByBloodGroup(
                BloodGroup.O_POSITIVE))
                .thenReturn(Optional.of(stock));

        // Act

        bloodStockService.increaseUnits(
                BloodGroup.O_POSITIVE,
                null);

        // Assert

        assertEquals(
                100,
                stock.getUnits());

        verify(bloodStockRepository, times(1))
                .save(stock);
    }

    @Test
    void increaseUnits_shouldThrowException_whenBloodGroupDoesNotExist() {

        // Arrange

        when(bloodStockRepository.findByBloodGroup(
                BloodGroup.O_POSITIVE))
                .thenReturn(Optional.empty());

        // Act + Assert

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> bloodStockService.increaseUnits(
                        BloodGroup.O_POSITIVE,
                        10));

        assertEquals(
                "Blood group not found.",
                exception.getMessage());

        verify(bloodStockRepository, never())
                .save(any(BloodStock.class));
    }

    // =========================================================
    // DECREASE UNITS
    // =========================================================

    @Test
    void decreaseUnits_shouldDecreaseStockUnits() {

        // Arrange

        BloodStock stock = createBloodStock();

        stock.setUnits(100);

        when(bloodStockRepository.findByBloodGroup(
                BloodGroup.O_POSITIVE))
                .thenReturn(Optional.of(stock));

        // Act

        bloodStockService.decreaseUnits(
                BloodGroup.O_POSITIVE,
                30);

        // Assert

        assertEquals(
                70,
                stock.getUnits());

        assertNotNull(stock.getLastUpdated());

        verify(bloodStockRepository, times(1))
                .save(stock);
    }

    @Test
    void decreaseUnits_shouldThrowException_whenInsufficientUnits() {

        // Arrange

        BloodStock stock = createBloodStock();

        stock.setUnits(20);

        when(bloodStockRepository.findByBloodGroup(
                BloodGroup.O_POSITIVE))
                .thenReturn(Optional.of(stock));

        // Act + Assert

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> bloodStockService.decreaseUnits(
                        BloodGroup.O_POSITIVE,
                        50));

        assertEquals(
                "Insufficient blood units available.",
                exception.getMessage());

        assertEquals(
                20,
                stock.getUnits());

        verify(bloodStockRepository, never())
                .save(any(BloodStock.class));
    }

    @Test
    void decreaseUnits_shouldThrowException_whenBloodGroupDoesNotExist() {

        // Arrange

        when(bloodStockRepository.findByBloodGroup(
                BloodGroup.O_POSITIVE))
                .thenReturn(Optional.empty());

        // Act + Assert

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> bloodStockService.decreaseUnits(
                        BloodGroup.O_POSITIVE,
                        10));

        assertEquals(
                "Blood group not found.",
                exception.getMessage());

        verify(bloodStockRepository, never())
                .save(any(BloodStock.class));
    }

    // =========================================================
    // SAVE BLOOD STOCK
    // =========================================================

    @Test
    void saveBloodStock_shouldSaveStock_whenBloodGroupDoesNotExist() {

        // Arrange

        BloodStockRequest request = createRequest();

        when(bloodStockRepository.findByBloodGroup(
                BloodGroup.O_POSITIVE))
                .thenReturn(Optional.empty());

        // Act

        bloodStockService.saveBloodStock(request);

        // Assert

        verify(bloodStockRepository, times(1))
                .findByBloodGroup(
                        BloodGroup.O_POSITIVE);

        verify(bloodStockRepository, times(1))
                .save(any(BloodStock.class));

        verify(auditLogService, times(1))
                .saveLog(
                        "Admin",
                        "ADD",
                        "Blood Stock",
                        "Added 50 units of O_POSITIVE");
    }

    @Test
    void saveBloodStock_shouldThrowException_whenBloodGroupAlreadyExists() {

        // Arrange

        BloodStockRequest request = createRequest();

        BloodStock existingStock = createBloodStock();

        when(bloodStockRepository.findByBloodGroup(
                BloodGroup.O_POSITIVE))
                .thenReturn(Optional.of(existingStock));

        // Act + Assert

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> bloodStockService.saveBloodStock(request));

        assertEquals(
                "Blood group already exists.",
                exception.getMessage());

        verify(bloodStockRepository, never())
                .save(any(BloodStock.class));

        verify(auditLogService, never())
                .saveLog(
                        any(),
                        any(),
                        any(),
                        any());
    }

    // =========================================================
    // GET BLOOD STOCK BY ID
    // =========================================================

    @Test
    void getBloodStockById_shouldReturnRequest_whenStockExists() {

        // Arrange

        Long id = 1L;

        BloodStock stock = createBloodStock();

        when(bloodStockRepository.findById(id))
                .thenReturn(Optional.of(stock));

        // Act

        BloodStockRequest result = bloodStockService.getBloodStockById(id);

        // Assert

        assertNotNull(result);

        assertEquals(
                BloodGroup.O_POSITIVE,
                result.getBloodGroup());

        assertEquals(
                100,
                result.getUnits());

        verify(bloodStockRepository, times(1))
                .findById(id);
    }

    @Test
    void getBloodStockById_shouldThrowException_whenStockDoesNotExist() {

        // Arrange

        Long id = 999L;

        when(bloodStockRepository.findById(id))
                .thenReturn(Optional.empty());

        // Act + Assert

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> bloodStockService.getBloodStockById(id));

        assertEquals(
                "Blood Stock Not Found",
                exception.getMessage());
    }

    // =========================================================
    // GET BLOOD STOCK RESPONSE BY ID
    // =========================================================

    @Test
    void getBloodStockResponseById_shouldReturnResponse_whenStockExists() {

        // Arrange

        Long id = 1L;

        BloodStock stock = createBloodStock();

        when(bloodStockRepository.findById(id))
                .thenReturn(Optional.of(stock));

        // Act

        BloodStockResponse result = bloodStockService.getBloodStockResponseById(id);

        // Assert

        assertNotNull(result);

        verify(bloodStockRepository, times(1))
                .findById(id);
    }

    @Test
    void getBloodStockResponseById_shouldThrowException_whenStockDoesNotExist() {

        // Arrange

        Long id = 999L;

        when(bloodStockRepository.findById(id))
                .thenReturn(Optional.empty());

        // Act + Assert

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> bloodStockService.getBloodStockResponseById(id));

        assertEquals(
                "Blood Stock Not Found",
                exception.getMessage());
    }

    // =========================================================
    // DELETE BLOOD STOCK
    // =========================================================

    @Test
    void deleteBloodStock_shouldDeleteStock() {

        // Arrange

        Long id = 1L;

        // Act

        bloodStockService.deleteBloodStock(id);

        // Assert

        verify(bloodStockRepository, times(1))
                .deleteById(id);

        verify(auditLogService, times(1))
                .saveLog(
                        "Admin",
                        "DELETE",
                        "Blood Stock",
                        "Deleted Blood Stock ID : " + id);
    }

    // =========================================================
    // SEARCH BY BLOOD GROUP
    // =========================================================

    @Test
    void searchByBloodGroup_shouldReturnMatchingStock() {

        // Arrange

        BloodStock stock = createBloodStock();

        when(bloodStockRepository.findByBloodGroup(
                BloodGroup.O_POSITIVE))
                .thenReturn(Optional.of(stock));

        // Act

        List<BloodStockResponse> result = bloodStockService.searchByBloodGroup(
                BloodGroup.O_POSITIVE);

        // Assert

        assertNotNull(result);
        assertEquals(1, result.size());

        verify(bloodStockRepository, times(1))
                .findByBloodGroup(
                        BloodGroup.O_POSITIVE);
    }

    @Test
    void searchByBloodGroup_shouldReturnEmptyList_whenGroupDoesNotExist() {

        // Arrange

        when(bloodStockRepository.findByBloodGroup(
                BloodGroup.O_POSITIVE))
                .thenReturn(Optional.empty());

        // Act

        List<BloodStockResponse> result = bloodStockService.searchByBloodGroup(
                BloodGroup.O_POSITIVE);

        // Assert

        assertNotNull(result);
        assertTrue(result.isEmpty());

        verify(bloodStockRepository, times(1))
                .findByBloodGroup(
                        BloodGroup.O_POSITIVE);
    }

    // =========================================================
    // GET AVAILABLE BLOOD
    // =========================================================

    @Test
    void getAvailableBlood_shouldReturnBloodStock() {

        // Arrange

        BloodStock stock = createBloodStock();

        when(bloodStockRepository.findAll())
                .thenReturn(List.of(stock));

        // Act

        List<BloodStockResponse> result = bloodStockService.getAvailableBlood();

        // Assert

        assertNotNull(result);
        assertEquals(1, result.size());

        verify(bloodStockRepository, times(1))
                .findAll();
    }

    @Test
    void getAvailableBlood_shouldReturnEmptyList_whenNoStockExists() {

        // Arrange

        when(bloodStockRepository.findAll())
                .thenReturn(Collections.emptyList());

        // Act

        List<BloodStockResponse> result = bloodStockService.getAvailableBlood();

        // Assert

        assertNotNull(result);
        assertTrue(result.isEmpty());

        verify(bloodStockRepository, times(1))
                .findAll();
    }

    // =========================================================
    // PAGINATION - NO KEYWORD
    // =========================================================

    @Test
    void getBloodStocks_shouldReturnPaginatedStock_whenNoKeywordProvided() {

        // Arrange

        BloodStock stock = createBloodStock();

        Page<BloodStock> stockPage = new PageImpl<>(
                List.of(stock));

        when(bloodStockRepository.findAll(
                any(Pageable.class)))
                .thenReturn(stockPage);

        // Act

        Page<BloodStockResponse> result = bloodStockService.getBloodStocks(
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

        verify(bloodStockRepository, times(1))
                .findAll(any(Pageable.class));
    }

    // =========================================================
    // PAGINATION - VALID BLOOD GROUP KEYWORD
    // =========================================================

    @Test
    void getBloodStocks_shouldSearchByBloodGroup_whenValidKeywordProvided() {

        // Arrange

        BloodStock stock = createBloodStock();

        when(bloodStockRepository.findByBloodGroup(
                BloodGroup.O_POSITIVE))
                .thenReturn(Optional.of(stock));

        // Act

        Page<BloodStockResponse> result = bloodStockService.getBloodStocks(
                0,
                10,
                "O_POSITIVE",
                "id",
                "asc");

        // Assert

        assertNotNull(result);

        assertEquals(
                1,
                result.getTotalElements());

        verify(bloodStockRepository, times(1))
                .findByBloodGroup(
                        BloodGroup.O_POSITIVE);

        verify(
                bloodStockRepository,
                never()).findAll(any(Pageable.class));
    }

    // =========================================================
    // PAGINATION - INVALID KEYWORD
    // =========================================================

    @Test
    void getBloodStocks_shouldReturnAllStock_whenInvalidKeywordProvided() {

        // Arrange

        BloodStock stock = createBloodStock();

        Page<BloodStock> stockPage = new PageImpl<>(
                List.of(stock));

        when(bloodStockRepository.findAll(
                any(Pageable.class)))
                .thenReturn(stockPage);

        // Act

        Page<BloodStockResponse> result = bloodStockService.getBloodStocks(
                0,
                10,
                "INVALID_GROUP",
                "id",
                "asc");

        // Assert

        assertNotNull(result);

        assertEquals(
                1,
                result.getTotalElements());

        verify(bloodStockRepository, times(1))
                .findAll(any(Pageable.class));
    }

    // =========================================================
    // PAGINATION - DESCENDING SORT
    // =========================================================

    @Test
    void getBloodStocks_shouldSupportDescendingSort() {

        // Arrange

        BloodStock stock = createBloodStock();

        Page<BloodStock> stockPage = new PageImpl<>(
                List.of(stock));

        when(bloodStockRepository.findAll(
                any(Pageable.class)))
                .thenReturn(stockPage);

        // Act

        Page<BloodStockResponse> result = bloodStockService.getBloodStocks(
                0,
                10,
                null,
                "units",
                "desc");

        // Assert

        assertNotNull(result);

        verify(bloodStockRepository, times(1))
                .findAll(any(Pageable.class));
    }

    // =========================================================
    // PAGINATION - INVALID PAGE AND SIZE
    // =========================================================

    @Test
    void getBloodStocks_shouldUseDefaultValues_whenPageAndSizeAreInvalid() {

        // Arrange

        BloodStock stock = createBloodStock();

        Page<BloodStock> stockPage = new PageImpl<>(
                List.of(stock));

        when(bloodStockRepository.findAll(
                any(Pageable.class)))
                .thenReturn(stockPage);

        // Act

        Page<BloodStockResponse> result = bloodStockService.getBloodStocks(
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

        verify(bloodStockRepository, times(1))
                .findAll(any(Pageable.class));
    }
}
