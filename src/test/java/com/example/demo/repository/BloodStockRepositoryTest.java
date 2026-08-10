package com.example.demo.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

import com.example.demo.entity.BloodStock;
import com.example.demo.enums.BloodGroup;

@DataJpaTest
@ActiveProfiles("test")
class BloodStockRepositoryTest {

    @Autowired
    private BloodStockRepository bloodStockRepository;

    @BeforeEach
    void setUp() {
        bloodStockRepository.deleteAll();
    }

    @Test
    void saveBloodStock_shouldSaveSuccessfully() {
        BloodStock stock = new BloodStock();
        stock.setBloodGroup(BloodGroup.A_POSITIVE);
        stock.setUnitsAvailable(10);
        stock.setLastUpdated(LocalDateTime.now());

        BloodStock saved = bloodStockRepository.save(stock);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getBloodGroup()).isEqualTo(BloodGroup.A_POSITIVE);
        assertThat(saved.getUnitsAvailable()).isEqualTo(10);
    }

    @Test
    void findByBloodGroup_shouldReturnStock_whenExists() {
        BloodStock stock = new BloodStock();
        stock.setBloodGroup(BloodGroup.O_NEGATIVE);
        stock.setUnitsAvailable(5);
        stock.setLastUpdated(LocalDateTime.now());
        bloodStockRepository.save(stock);

        Optional<BloodStock> result = bloodStockRepository.findByBloodGroup(BloodGroup.O_NEGATIVE);

        assertThat(result).isPresent();
        assertThat(result.get().getUnitsAvailable()).isEqualTo(5);
    }

    @Test
    void findByBloodGroup_shouldReturnEmpty_whenDoesNotExist() {
        Optional<BloodStock> result = bloodStockRepository.findByBloodGroup(BloodGroup.B_POSITIVE);

        assertThat(result).isEmpty();
    }

    @Test
    void findAllByOrderByBloodGroup_shouldReturnOrderedStock() {
        BloodStock stock1 = new BloodStock();
        stock1.setBloodGroup(BloodGroup.O_POSITIVE);
        stock1.setUnitsAvailable(15);
        stock1.setLastUpdated(LocalDateTime.now());

        BloodStock stock2 = new BloodStock();
        stock2.setBloodGroup(BloodGroup.A_NEGATIVE);
        stock2.setUnitsAvailable(8);
        stock2.setLastUpdated(LocalDateTime.now());

        bloodStockRepository.saveAll(List.of(stock1, stock2));

        List<BloodStock> list = bloodStockRepository.findAllByOrderByBloodGroup();

        assertThat(list).hasSize(2);
    }

    @Test
    void findAll_withPagination_shouldReturnPageOfStock() {
        BloodStock stock = new BloodStock();
        stock.setBloodGroup(BloodGroup.AB_POSITIVE);
        stock.setUnitsAvailable(20);
        stock.setLastUpdated(LocalDateTime.now());
        bloodStockRepository.save(stock);

        Page<BloodStock> page = bloodStockRepository.findAll(PageRequest.of(0, 5));

        assertThat(page.getContent()).hasSize(1);
        assertThat(page.getTotalElements()).isEqualTo(1);
    }
}
