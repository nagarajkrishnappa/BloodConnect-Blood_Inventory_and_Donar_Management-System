package com.example.demo.integration;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.example.demo.entity.BloodStock;
import com.example.demo.enums.BloodGroup;
import com.example.demo.repository.BloodStockRepository;

@SpringBootTest
@Testcontainers
class BloodStockRepositoryTestcontainersTest {

    @Container
    static MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.0")
            .withDatabaseName("bloodbank_test")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void configureDatabase(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", mysql::getJdbcUrl);
        registry.add("spring.datasource.username", mysql::getUsername);
        registry.add("spring.datasource.password", mysql::getPassword);
        registry.add("spring.datasource.driver-class-name", mysql::getDriverClassName);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
    }

    @Autowired
    private BloodStockRepository bloodStockRepository;

    @BeforeEach
    void setUp() {
        bloodStockRepository.deleteAll();
    }

    @Test
    void saveBloodStock_shouldSaveSuccessfully() {
        BloodStock stock = createBloodStock(BloodGroup.O_POSITIVE, 15);
        BloodStock savedStock = bloodStockRepository.save(stock);

        assertThat(savedStock.getId()).isNotNull();
        assertThat(savedStock.getBloodGroup()).isEqualTo(BloodGroup.O_POSITIVE);
        assertThat(savedStock.getUnitsAvailable()).isEqualTo(15);
    }

    @Test
    void findByBloodGroup_shouldReturnCorrectStock() {
        BloodStock stock = createBloodStock(BloodGroup.A_POSITIVE, 10);
        bloodStockRepository.save(stock);

        Optional<BloodStock> found = bloodStockRepository.findByBloodGroup(BloodGroup.A_POSITIVE);
        assertThat(found).isPresent();
        assertThat(found.get().getUnitsAvailable()).isEqualTo(10);
    }

    @Test
    void findAllByOrderByBloodGroup_shouldReturnSortedList() {
        BloodStock stock1 = createBloodStock(BloodGroup.O_POSITIVE, 20);
        BloodStock stock2 = createBloodStock(BloodGroup.A_POSITIVE, 5);
        bloodStockRepository.save(stock1);
        bloodStockRepository.save(stock2);

        List<BloodStock> sortedStock = bloodStockRepository.findAllByOrderByBloodGroup();
        assertThat(sortedStock).hasSize(2);
        assertThat(sortedStock.get(0).getBloodGroup()).isEqualTo(BloodGroup.A_POSITIVE);
    }

    @Test
    void findAllPageable_shouldReturnPagedResults() {
        BloodStock stock = createBloodStock(BloodGroup.B_NEGATIVE, 8);
        bloodStockRepository.save(stock);

        Page<BloodStock> page = bloodStockRepository.findAll(PageRequest.of(0, 5));
        assertThat(page.getContent()).hasSize(1);
    }

    private BloodStock createBloodStock(BloodGroup group, int units) {
        BloodStock stock = new BloodStock();
        stock.setBloodGroup(group);
        stock.setUnitsAvailable(units);
        stock.setLastUpdated(LocalDateTime.now());
        return stock;
    }
}
