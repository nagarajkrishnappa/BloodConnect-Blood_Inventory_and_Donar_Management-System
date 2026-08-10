package com.example.demo.service.impl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.demo.dto.request.BloodStockRequest;
import com.example.demo.dto.response.BloodStockResponse;
import com.example.demo.entity.BloodStock;
import com.example.demo.enums.BloodGroup;
import com.example.demo.mapper.BloodStockMapper;
import com.example.demo.repository.BloodStockRepository;
import com.example.demo.service.AuditLogService;
import com.example.demo.service.BloodStockService;

@Service
public class BloodStockServiceImpl implements BloodStockService {

    @Autowired
    private BloodStockRepository bloodStockRepository;

    @Autowired
    private AuditLogService auditLogService;

    @Override
    public void updateBloodStock(Long id, BloodStockRequest request) {

        BloodStock stock = bloodStockRepository.findById(id)
                .orElseThrow(() -> new com.example.demo.exception.ResourceNotFoundException("Blood stock not found."));

        stock.setBloodGroup(request.getBloodGroup());
        stock.setUnits(request.getUnits());
        stock.setLastUpdated(LocalDateTime.now());

        bloodStockRepository.save(stock);

        auditLogService.saveLog(
                "Admin",
                "UPDATE",
                "Blood Stock",
                "Updated Blood Stock ID : " + id);
    }

    @Override
    public List<BloodStockResponse> getAllBloodStock() {

        return bloodStockRepository.findAll()
                .stream()
                .map(BloodStockMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public BloodStockResponse getBloodStockByGroup(BloodGroup bloodGroup) {

        BloodStock stock = bloodStockRepository.findByBloodGroup(bloodGroup)
                .orElseThrow(() -> new com.example.demo.exception.ResourceNotFoundException("Blood group not found."));

        return BloodStockMapper.toResponse(stock);
    }

    @Override
    public void increaseUnits(BloodGroup bloodGroup, Integer units) {

        BloodStock stock = bloodStockRepository.findByBloodGroup(bloodGroup)
                .orElseThrow(() -> new com.example.demo.exception.ResourceNotFoundException("Blood group not found."));

        int current = stock.getUnits() != null ? stock.getUnits() : 0;
        int toAdd = units != null ? units : 0;

        stock.setUnits(current + toAdd);
        stock.setLastUpdated(LocalDateTime.now());

        bloodStockRepository.save(stock);
    }

    @Override
    public void decreaseUnits(BloodGroup bloodGroup, Integer units) {

        BloodStock stock = bloodStockRepository.findByBloodGroup(bloodGroup)
                .orElseThrow(() -> new com.example.demo.exception.ResourceNotFoundException("Blood group not found."));

        int current = stock.getUnits() != null ? stock.getUnits() : 0;
        int toSubtract = units != null ? units : 0;

        if (current < toSubtract) {
            throw new com.example.demo.exception.InvalidRequestException("Insufficient blood units available.");
        }

        stock.setUnits(current - toSubtract);
        stock.setLastUpdated(LocalDateTime.now());

        bloodStockRepository.save(stock);
    }

    @Override
    public void saveBloodStock(BloodStockRequest request) {

        if (request == null || request.getUnits() == null || request.getUnits() < 0) {
            throw new com.example.demo.exception.InvalidRequestException("Units cannot be negative or null.");
        }

        if (bloodStockRepository.findByBloodGroup(request.getBloodGroup()).isPresent()) {
            throw new com.example.demo.exception.DuplicateResourceException("Blood group already exists.");
        }

        BloodStock stock = BloodStockMapper.toEntity(request);
        stock.setLastUpdated(LocalDateTime.now());

        bloodStockRepository.save(stock);

        auditLogService.saveLog(
                "Admin",
                "ADD",
                "Blood Stock",
                "Added " + request.getUnits() + " units of " + request.getBloodGroup());
    }

    @Override
    public BloodStockRequest getBloodStockById(Long id) {

        BloodStock stock = bloodStockRepository.findById(id)
                .orElseThrow(() -> new com.example.demo.exception.ResourceNotFoundException("Blood Stock Not Found"));

        BloodStockRequest request = new BloodStockRequest();

        request.setBloodGroup(stock.getBloodGroup());
        request.setUnits(stock.getUnits());

        return request;
    }

    @Override
    public BloodStockResponse getBloodStockResponseById(Long id) {

        BloodStock stock = bloodStockRepository.findById(id)
                .orElseThrow(() -> new com.example.demo.exception.ResourceNotFoundException("Blood Stock Not Found"));

        return BloodStockMapper.toResponse(stock);
    }

    @Override
    public void deleteBloodStock(Long id) {

        bloodStockRepository.deleteById(id);

        auditLogService.saveLog(
                "Admin",
                "DELETE",
                "Blood Stock",
                "Deleted Blood Stock ID : " + id);
    }

    @Override
    public List<BloodStockResponse> searchByBloodGroup(BloodGroup bloodGroup) {

        return bloodStockRepository.findByBloodGroup(bloodGroup)
                .stream()
                .map(BloodStockMapper::toResponse)
                .collect(Collectors.toList());

    }

    @Override
    public List<BloodStockResponse> getAvailableBlood() {

        return bloodStockRepository.findAll()
                .stream()
                .map(BloodStockMapper::toResponse)
                .collect(Collectors.toList());

    }

    @Override
    public org.springframework.data.domain.Page<BloodStockResponse> getBloodStocks(int page, int size, String keyword, String sortBy, String direction) {
        String validSortBy = (sortBy == null || sortBy.isBlank()) ? "id" : sortBy;
        org.springframework.data.domain.Sort sort = "desc".equalsIgnoreCase(direction)
                ? org.springframework.data.domain.Sort.by(validSortBy).descending()
                : org.springframework.data.domain.Sort.by(validSortBy).ascending();

        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(page < 0 ? 0 : page, size <= 0 ? 10 : size, sort);

        org.springframework.data.domain.Page<BloodStock> stockPage;
        if (keyword != null && !keyword.isBlank()) {
            try {
                BloodGroup bg = BloodGroup.valueOf(keyword.trim().toUpperCase());
                java.util.Optional<BloodStock> opt = bloodStockRepository.findByBloodGroup(bg);
                java.util.List<BloodStock> list = opt.isPresent() ? java.util.List.of(opt.get()) : java.util.Collections.emptyList();
                stockPage = new org.springframework.data.domain.PageImpl<>(list, pageable, list.size());
            } catch (Exception e) {
                stockPage = bloodStockRepository.findAll(pageable);
            }
        } else {
            stockPage = bloodStockRepository.findAll(pageable);
        }

        return stockPage.map(BloodStockMapper::toResponse);
    }

}