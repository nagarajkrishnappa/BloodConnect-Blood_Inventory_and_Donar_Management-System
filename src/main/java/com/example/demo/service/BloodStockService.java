package com.example.demo.service;

import java.util.List;

import org.springframework.data.domain.Page;

import com.example.demo.dto.request.BloodStockRequest;
import com.example.demo.dto.response.BloodStockResponse;
import com.example.demo.enums.BloodGroup;

public interface BloodStockService {

    void saveBloodStock(BloodStockRequest request);

    void updateBloodStock(Long id, BloodStockRequest request);

    void deleteBloodStock(Long id);

    List<BloodStockResponse> getAllBloodStock();

    Page<BloodStockResponse> getBloodStocks(int page, int size, String keyword, String sortBy, String direction);

    BloodStockRequest getBloodStockById(Long id);

    BloodStockResponse getBloodStockResponseById(Long id);

    BloodStockResponse getBloodStockByGroup(BloodGroup bloodGroup);

    List<BloodStockResponse> searchByBloodGroup(BloodGroup bloodGroup);

    List<BloodStockResponse> getAvailableBlood();

    void increaseUnits(BloodGroup bloodGroup, Integer units);

    void decreaseUnits(BloodGroup bloodGroup, Integer units);
}