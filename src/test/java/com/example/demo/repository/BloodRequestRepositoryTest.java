package com.example.demo.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

import com.example.demo.entity.BloodRequest;
import com.example.demo.entity.Role;
import com.example.demo.entity.User;
import com.example.demo.enums.BloodGroup;
import com.example.demo.enums.RequestStatus;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class BloodRequestRepositoryTest {

    @Autowired
    private BloodRequestRepository bloodRequestRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    private User testUser;

    @BeforeEach
    void setUp() {
        bloodRequestRepository.deleteAll();
        userRepository.deleteAll();

        Role role = roleRepository.findByRoleName("USER")
                .orElseGet(() -> {
                    Role r = new Role();
                    r.setRoleName("USER");
                    return roleRepository.save(r);
                });

        testUser = new User();
        testUser.setFullName("Request User");
        testUser.setEmail("requestuser@gmail.com");
        testUser.setPassword("Password@123");
        testUser.setPhone("9998887776");
        testUser.setEnabled(true);
        testUser.setRole(role);
        testUser = userRepository.save(testUser);
    }

    private BloodRequest createRequest(BloodGroup bg, int units, RequestStatus status, boolean deleted) {
        BloodRequest request = new BloodRequest();
        request.setUser(testUser);
        request.setBloodGroup(bg);
        request.setUnitsRequired(units);
        request.setReason("Emergency requirement");
        request.setStatus(status);
        request.setRequestDate(LocalDateTime.now());
        request.setDeleted(deleted);
        return request;
    }

    @Test
    void saveBloodRequest_shouldSaveSuccessfully() {
        BloodRequest request = createRequest(BloodGroup.A_POSITIVE, 2, RequestStatus.PENDING, false);

        BloodRequest saved = bloodRequestRepository.save(request);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getBloodGroup()).isEqualTo(BloodGroup.A_POSITIVE);
        assertThat(saved.getStatus()).isEqualTo(RequestStatus.PENDING);
    }

    @Test
    void findByUserAndDeletedFalse_shouldReturnUserRequests() {
        BloodRequest req1 = createRequest(BloodGroup.A_POSITIVE, 2, RequestStatus.PENDING, false);
        BloodRequest req2 = createRequest(BloodGroup.B_POSITIVE, 1, RequestStatus.APPROVED, true);
        bloodRequestRepository.saveAll(List.of(req1, req2));

        List<BloodRequest> list = bloodRequestRepository.findByUserAndDeletedFalse(testUser);

        assertThat(list).hasSize(1);
        assertThat(list.get(0).getBloodGroup()).isEqualTo(BloodGroup.A_POSITIVE);
    }

    @Test
    void findByStatusAndDeletedFalse_shouldReturnMatchingRequests() {
        BloodRequest req1 = createRequest(BloodGroup.O_POSITIVE, 3, RequestStatus.PENDING, false);
        BloodRequest req2 = createRequest(BloodGroup.AB_POSITIVE, 1, RequestStatus.APPROVED, false);
        bloodRequestRepository.saveAll(List.of(req1, req2));

        List<BloodRequest> pendingList = bloodRequestRepository.findByStatusAndDeletedFalse(RequestStatus.PENDING);

        assertThat(pendingList).hasSize(1);
        assertThat(pendingList.get(0).getStatus()).isEqualTo(RequestStatus.PENDING);
    }

    @Test
    void findAllByDeletedFalse_shouldReturnActiveRequests() {
        BloodRequest req1 = createRequest(BloodGroup.O_NEGATIVE, 2, RequestStatus.PENDING, false);
        BloodRequest req2 = createRequest(BloodGroup.A_NEGATIVE, 1, RequestStatus.REJECTED, true);
        bloodRequestRepository.saveAll(List.of(req1, req2));

        List<BloodRequest> activeList = bloodRequestRepository.findAllByDeletedFalse();

        assertThat(activeList).hasSize(1);
    }

    @Test
    void countByDeletedFalse_shouldReturnCorrectCount() {
        BloodRequest req1 = createRequest(BloodGroup.O_NEGATIVE, 2, RequestStatus.PENDING, false);
        BloodRequest req2 = createRequest(BloodGroup.A_NEGATIVE, 1, RequestStatus.REJECTED, true);
        bloodRequestRepository.saveAll(List.of(req1, req2));

        long count = bloodRequestRepository.countByDeletedFalse();

        assertThat(count).isEqualTo(1L);
    }

    @Test
    void countByStatusAndDeletedFalse_shouldReturnCorrectCount() {
        BloodRequest req1 = createRequest(BloodGroup.O_POSITIVE, 2, RequestStatus.PENDING, false);
        BloodRequest req2 = createRequest(BloodGroup.A_POSITIVE, 1, RequestStatus.PENDING, false);
        BloodRequest req3 = createRequest(BloodGroup.B_POSITIVE, 1, RequestStatus.APPROVED, false);
        bloodRequestRepository.saveAll(List.of(req1, req2, req3));

        long count = bloodRequestRepository.countByStatusAndDeletedFalse(RequestStatus.PENDING);

        assertThat(count).isEqualTo(2L);
    }

    @Test
    void findByUser_FullNameContainingIgnoreCaseAndDeletedFalse_shouldReturnMatchingPage() {
        BloodRequest req = createRequest(BloodGroup.O_POSITIVE, 2, RequestStatus.PENDING, false);
        bloodRequestRepository.save(req);

        Page<BloodRequest> page = bloodRequestRepository
                .findByUser_FullNameContainingIgnoreCaseAndDeletedFalse("Request", PageRequest.of(0, 10));

        assertThat(page.getContent()).hasSize(1);
        assertThat(page.getContent().get(0).getUser().getFullName()).isEqualTo("Request User");
    }
}
