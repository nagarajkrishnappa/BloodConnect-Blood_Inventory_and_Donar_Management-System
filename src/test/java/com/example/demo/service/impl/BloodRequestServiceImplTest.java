
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

import com.example.demo.dto.request.BloodRequestRequest;
import com.example.demo.dto.response.BloodRequestResponse;
import com.example.demo.entity.BloodRequest;
import com.example.demo.entity.BloodStock;
import com.example.demo.entity.User;
import com.example.demo.enums.BloodGroup;
import com.example.demo.enums.RequestStatus;
import com.example.demo.repository.BloodRequestRepository;
import com.example.demo.repository.BloodStockRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.AuditLogService;

@ExtendWith(MockitoExtension.class)
class BloodRequestServiceImplTest {

    @Mock
    private BloodRequestRepository bloodRequestRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private BloodStockRepository bloodStockRepository;

    @Mock
    private AuditLogService auditLogService;

    @InjectMocks
    private BloodRequestServiceImpl bloodRequestService;

    // =========================================================
    // TEST DATA
    // =========================================================

    private User createUser() {

        User user = new User();

        user.setId(1L);
        user.setEmail("user@gmail.com");
        user.setFullName("Test User");

        return user;
    }

    private BloodRequest createBloodRequest() {

        BloodRequest request = new BloodRequest();

        request.setId(1L);
        request.setUser(createUser());
        request.setBloodGroup(BloodGroup.O_POSITIVE);
        request.setUnitsRequired(5);
        request.setStatus(RequestStatus.PENDING);
        request.setRequestDate(LocalDateTime.now());

        return request;
    }

    private BloodStock createBloodStock() {

        BloodStock stock = new BloodStock();

        stock.setId(1L);
        stock.setBloodGroup(BloodGroup.O_POSITIVE);
        stock.setUnitsAvailable(20);
        stock.setLastUpdated(LocalDateTime.now());

        return stock;
    }

    private BloodRequestRequest createRequest() {

        BloodRequestRequest request = new BloodRequestRequest();

        request.setEmail("user@gmail.com");
        request.setBloodGroup(BloodGroup.O_POSITIVE);
        request.setUnitsRequired(5);

        return request;
    }

    // =========================================================
    // CREATE REQUEST
    // =========================================================

    @Test
    void createRequest_shouldCreateRequest_whenUserExists() {

        // Arrange

        String email = "user@gmail.com";

        User user = createUser();

        BloodRequestRequest request = createRequest();

        when(userRepository.findByEmail(email))
                .thenReturn(Optional.of(user));

        // Act

        bloodRequestService.createRequest(
                email,
                request);

        // Assert

        verify(userRepository, times(1))
                .findByEmail(email);

        verify(bloodRequestRepository, times(1))
                .save(any(BloodRequest.class));
    }

    @Test
    void createRequest_shouldThrowException_whenUserDoesNotExist() {

        // Arrange

        String email = "unknown@gmail.com";

        BloodRequestRequest request = createRequest();

        when(userRepository.findByEmail(email))
                .thenReturn(Optional.empty());

        // Act + Assert

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> bloodRequestService.createRequest(
                        email,
                        request));

        assertEquals(
                "User not found",
                exception.getMessage());

        verify(bloodRequestRepository, never())
                .save(any(BloodRequest.class));
    }

    // =========================================================
    // SAVE REQUEST
    // =========================================================

    @Test
    void saveRequest_shouldSaveRequest_whenEmailUserExists() {

        // Arrange

        BloodRequestRequest request = createRequest();

        User user = createUser();

        when(userRepository.findByEmail("user@gmail.com"))
                .thenReturn(Optional.of(user));

        // Act

        bloodRequestService.saveRequest(request);

        // Assert

        verify(userRepository, times(1))
                .findByEmail("user@gmail.com");

        verify(bloodRequestRepository, times(1))
                .save(any(BloodRequest.class));
    }

    @Test
    void saveRequest_shouldUseFirstUser_whenEmailUserDoesNotExist() {

        // Arrange

        BloodRequestRequest request = createRequest();

        User user = createUser();

        when(userRepository.findByEmail("user@gmail.com"))
                .thenReturn(Optional.empty());

        when(userRepository.findAll())
                .thenReturn(List.of(user));

        // Act

        bloodRequestService.saveRequest(request);

        // Assert

        verify(userRepository, times(1))
                .findByEmail("user@gmail.com");

        verify(userRepository, times(1))
                .findAll();

        verify(bloodRequestRepository, times(1))
                .save(any(BloodRequest.class));
    }

    @Test
    void saveRequest_shouldThrowException_whenNoUsersExist() {

        // Arrange

        BloodRequestRequest request = createRequest();

        when(userRepository.findByEmail("user@gmail.com"))
                .thenReturn(Optional.empty());

        when(userRepository.findAll())
                .thenReturn(Collections.emptyList());

        // Act + Assert

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> bloodRequestService.saveRequest(request));

        assertEquals(
                "No users exist in the system to submit a blood request.",
                exception.getMessage());

        verify(bloodRequestRepository, never())
                .save(any(BloodRequest.class));
    }

    // =========================================================
    // GET MY REQUESTS
    // =========================================================

    @Test
    void getMyRequests_shouldReturnUserRequests() {

        // Arrange

        String email = "user@gmail.com";

        User user = createUser();

        BloodRequest request = createBloodRequest();

        when(userRepository.findByEmail(email))
                .thenReturn(Optional.of(user));

        when(bloodRequestRepository
                .findByUserAndDeletedFalse(user))
                .thenReturn(List.of(request));

        // Act

        List<BloodRequestResponse> result = bloodRequestService.getMyRequests(email);

        // Assert

        assertNotNull(result);
        assertEquals(1, result.size());

        verify(userRepository, times(1))
                .findByEmail(email);

        verify(
                bloodRequestRepository,
                times(1)).findByUserAndDeletedFalse(user);
    }

    @Test
    void getMyRequests_shouldReturnEmptyList_whenNoRequestsExist() {

        // Arrange

        String email = "user@gmail.com";

        User user = createUser();

        when(userRepository.findByEmail(email))
                .thenReturn(Optional.of(user));

        when(bloodRequestRepository
                .findByUserAndDeletedFalse(user))
                .thenReturn(Collections.emptyList());

        // Act

        List<BloodRequestResponse> result = bloodRequestService.getMyRequests(email);

        // Assert

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void getMyRequests_shouldThrowException_whenUserDoesNotExist() {

        // Arrange

        when(userRepository.findByEmail(anyString()))
                .thenReturn(Optional.empty());

        // Act + Assert

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> bloodRequestService.getMyRequests(
                        "unknown@gmail.com"));

        assertEquals(
                "User not found",
                exception.getMessage());
    }

    // =========================================================
    // GET REQUESTS BY STATUS
    // =========================================================

    @Test
    void getRequestsByStatus_shouldReturnMatchingRequests() {

        // Arrange

        BloodRequest request = createBloodRequest();

        when(bloodRequestRepository
                .findByStatusAndDeletedFalse(
                        RequestStatus.PENDING))
                .thenReturn(List.of(request));

        // Act

        List<BloodRequestResponse> result = bloodRequestService.getRequestsByStatus(
                RequestStatus.PENDING);

        // Assert

        assertNotNull(result);
        assertEquals(1, result.size());

        verify(
                bloodRequestRepository,
                times(1)).findByStatusAndDeletedFalse(
                        RequestStatus.PENDING);
    }

    @Test
    void getRequestsByStatus_shouldReturnEmptyList_whenNoMatchingRequests() {

        // Arrange

        when(bloodRequestRepository
                .findByStatusAndDeletedFalse(
                        RequestStatus.PENDING))
                .thenReturn(Collections.emptyList());

        // Act

        List<BloodRequestResponse> result = bloodRequestService.getRequestsByStatus(
                RequestStatus.PENDING);

        // Assert

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    // =========================================================
    // GET ALL REQUESTS
    // =========================================================

    @Test
    void getAllRequests_shouldReturnAllActiveRequests() {

        // Arrange

        BloodRequest request = createBloodRequest();

        when(bloodRequestRepository
                .findAllByDeletedFalse())
                .thenReturn(List.of(request));

        // Act

        List<BloodRequestResponse> result = bloodRequestService.getAllRequests();

        // Assert

        assertNotNull(result);
        assertEquals(1, result.size());

        verify(
                bloodRequestRepository,
                times(1)).findAllByDeletedFalse();
    }

    @Test
    void getAllRequests_shouldReturnEmptyList_whenNoRequestsExist() {

        // Arrange

        when(bloodRequestRepository
                .findAllByDeletedFalse())
                .thenReturn(Collections.emptyList());

        // Act

        List<BloodRequestResponse> result = bloodRequestService.getAllRequests();

        // Assert

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    // =========================================================
    // PAGINATION
    // =========================================================

    @Test
    void getBloodRequests_shouldReturnPaginatedRequests_withoutKeyword() {

        // Arrange

        BloodRequest request = createBloodRequest();

        Page<BloodRequest> page = new PageImpl<>(
                List.of(request));

        when(bloodRequestRepository
                .findAllByDeletedFalse(
                        any(Pageable.class)))
                .thenReturn(page);

        // Act

        Page<BloodRequestResponse> result = bloodRequestService.getBloodRequests(
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
                bloodRequestRepository,
                times(1)).findAllByDeletedFalse(
                        any(Pageable.class));
    }

    @Test
    void getBloodRequests_shouldSearchByUserName_whenKeywordProvided() {

        // Arrange

        BloodRequest request = createBloodRequest();

        Page<BloodRequest> page = new PageImpl<>(
                List.of(request));

        when(bloodRequestRepository
                .findByUser_FullNameContainingIgnoreCaseAndDeletedFalse(
                        eq("Test User"),
                        any(Pageable.class)))
                .thenReturn(page);

        // Act

        Page<BloodRequestResponse> result = bloodRequestService.getBloodRequests(
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
                bloodRequestRepository,
                times(1)).findByUser_FullNameContainingIgnoreCaseAndDeletedFalse(
                        eq("Test User"),
                        any(Pageable.class));
    }

    @Test
    void getBloodRequests_shouldUseDefaultPageAndSize_whenInvalidValuesProvided() {

        // Arrange

        BloodRequest request = createBloodRequest();

        Page<BloodRequest> page = new PageImpl<>(
                List.of(request));

        when(bloodRequestRepository
                .findAllByDeletedFalse(
                        any(Pageable.class)))
                .thenReturn(page);

        // Act

        Page<BloodRequestResponse> result = bloodRequestService.getBloodRequests(
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
                bloodRequestRepository,
                times(1)).findAllByDeletedFalse(
                        any(Pageable.class));
    }

    // =========================================================
    // GET REQUEST BY ID
    // =========================================================

    @Test
    void getRequestById_shouldReturnRequest_whenRequestExists() {

        // Arrange

        Long id = 1L;

        BloodRequest request = createBloodRequest();

        when(bloodRequestRepository.findById(id))
                .thenReturn(Optional.of(request));

        // Act

        BloodRequestResponse result = bloodRequestService.getRequestById(id);

        // Assert

        assertNotNull(result);

        verify(
                bloodRequestRepository,
                times(1)).findById(id);
    }

    @Test
    void getRequestById_shouldThrowException_whenRequestDoesNotExist() {

        // Arrange

        Long id = 999L;

        when(bloodRequestRepository.findById(id))
                .thenReturn(Optional.empty());

        // Act + Assert

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> bloodRequestService.getRequestById(id));

        assertEquals(
                "Request not found",
                exception.getMessage());
    }

    // =========================================================
    // APPROVE REQUEST - SUCCESS
    // =========================================================

    @Test
    void approveRequest_shouldApproveRequestAndDeductStock() {

        // Arrange

        Long id = 1L;

        BloodRequest request = createBloodRequest();

        request.setStatus(RequestStatus.PENDING);
        request.setUnitsRequired(5);

        BloodStock stock = createBloodStock();

        stock.setUnitsAvailable(20);

        when(bloodRequestRepository.findById(id))
                .thenReturn(Optional.of(request));

        when(bloodStockRepository.findByBloodGroup(
                request.getBloodGroup()))
                .thenReturn(Optional.of(stock));

        // Act

        bloodRequestService.approveRequest(id);

        // Assert

        assertEquals(
                RequestStatus.APPROVED,
                request.getStatus());

        assertEquals(
                15,
                stock.getUnitsAvailable());

        verify(
                bloodStockRepository,
                times(1)).save(stock);

        verify(
                bloodRequestRepository,
                times(1)).save(request);

        verify(
                auditLogService,
                times(1)).saveLog(
                        "Admin",
                        "APPROVE",
                        "Blood Request",
                        "Approved Request ID : " + id);
    }

    // =========================================================
    // APPROVE - REQUEST NOT FOUND
    // =========================================================

    @Test
    void approveRequest_shouldThrowException_whenRequestDoesNotExist() {

        // Arrange

        Long id = 999L;

        when(bloodRequestRepository.findById(id))
                .thenReturn(Optional.empty());

        // Act + Assert

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> bloodRequestService.approveRequest(id));

        assertEquals(
                "Request not found",
                exception.getMessage());

        verify(
                bloodStockRepository,
                never()).save(any(BloodStock.class));

        verify(
                bloodRequestRepository,
                never()).save(any(BloodRequest.class));
    }

    // =========================================================
    // APPROVE - ALREADY APPROVED
    // =========================================================

    @Test
    void approveRequest_shouldThrowException_whenAlreadyApproved() {

        // Arrange

        Long id = 1L;

        BloodRequest request = createBloodRequest();

        request.setStatus(
                RequestStatus.APPROVED);

        when(bloodRequestRepository.findById(id))
                .thenReturn(Optional.of(request));

        // Act + Assert

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> bloodRequestService.approveRequest(id));

        assertEquals(
                "Request is already approved.",
                exception.getMessage());

        verify(
                bloodStockRepository,
                never()).findByBloodGroup(any());

        verify(
                bloodRequestRepository,
                never()).save(any(BloodRequest.class));
    }

    // =========================================================
    // APPROVE - INSUFFICIENT STOCK
    // =========================================================

    @Test
    void approveRequest_shouldThrowException_whenStockIsInsufficient() {

        // Arrange

        Long id = 1L;

        BloodRequest request = createBloodRequest();

        request.setUnitsRequired(50);

        BloodStock stock = createBloodStock();

        stock.setUnitsAvailable(20);

        when(bloodRequestRepository.findById(id))
                .thenReturn(Optional.of(request));

        when(bloodStockRepository.findByBloodGroup(
                request.getBloodGroup()))
                .thenReturn(Optional.of(stock));

        // Act + Assert

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> bloodRequestService.approveRequest(id));

        assertTrue(
                exception.getMessage()
                        .contains(
                                "Insufficient blood stock"));

        assertEquals(
                RequestStatus.PENDING,
                request.getStatus());

        assertEquals(
                20,
                stock.getUnitsAvailable());

        verify(
                bloodStockRepository,
                never()).save(any(BloodStock.class));

        verify(
                bloodRequestRepository,
                never()).save(any(BloodRequest.class));
    }

    // =========================================================
    // APPROVE - STOCK DOES NOT EXIST
    // =========================================================

    @Test
    void approveRequest_shouldCreateStock_whenStockDoesNotExist() {

        // Arrange

        Long id = 1L;

        BloodRequest request = createBloodRequest();

        request.setUnitsRequired(0);

        when(bloodRequestRepository.findById(id))
                .thenReturn(Optional.of(request));

        when(bloodStockRepository.findByBloodGroup(
                request.getBloodGroup()))
                .thenReturn(Optional.empty());

        BloodStock newStock = createBloodStock();

        newStock.setUnitsAvailable(0);

        when(bloodStockRepository.save(
                any(BloodStock.class)))
                .thenReturn(newStock);

        // Act

        bloodRequestService.approveRequest(id);

        // Assert

        assertEquals(
                RequestStatus.APPROVED,
                request.getStatus());

        verify(
                bloodStockRepository,
                times(2)).save(any(BloodStock.class));

        verify(
                bloodRequestRepository,
                times(1)).save(request);

        verify(
                auditLogService,
                times(1)).saveLog(
                        "Admin",
                        "APPROVE",
                        "Blood Request",
                        "Approved Request ID : " + id);
    }

    // =========================================================
    // REJECT REQUEST - SUCCESS
    // =========================================================

    @Test
    void rejectRequest_shouldRejectRequest() {

        // Arrange

        Long id = 1L;

        BloodRequest request = createBloodRequest();

        request.setStatus(
                RequestStatus.PENDING);

        when(bloodRequestRepository.findById(id))
                .thenReturn(Optional.of(request));

        // Act

        bloodRequestService.rejectRequest(id);

        // Assert

        assertEquals(
                RequestStatus.REJECTED,
                request.getStatus());

        verify(
                bloodRequestRepository,
                times(1)).save(request);

        verify(
                auditLogService,
                times(1)).saveLog(
                        "Admin",
                        "REJECT",
                        "Blood Request",
                        "Rejected Request ID : " + id);
    }

    // =========================================================
    // REJECT - REQUEST NOT FOUND
    // =========================================================

    @Test
    void rejectRequest_shouldThrowException_whenRequestDoesNotExist() {

        // Arrange

        Long id = 999L;

        when(bloodRequestRepository.findById(id))
                .thenReturn(Optional.empty());

        // Act + Assert

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> bloodRequestService.rejectRequest(id));

        assertEquals(
                "Request not found",
                exception.getMessage());

        verify(
                bloodRequestRepository,
                never()).save(any(BloodRequest.class));
    }

    // =========================================================
    // REJECT - ALREADY REJECTED
    // =========================================================

    @Test
    void rejectRequest_shouldThrowException_whenAlreadyRejected() {

        // Arrange

        Long id = 1L;

        BloodRequest request = createBloodRequest();

        request.setStatus(
                RequestStatus.REJECTED);

        when(bloodRequestRepository.findById(id))
                .thenReturn(Optional.of(request));

        // Act + Assert

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> bloodRequestService.rejectRequest(id));

        assertEquals(
                "Request is already rejected.",
                exception.getMessage());

        verify(
                bloodRequestRepository,
                never()).save(any(BloodRequest.class));

        verify(
                auditLogService,
                never()).saveLog(
                        any(),
                        any(),
                        any(),
                        any());
    }
}
