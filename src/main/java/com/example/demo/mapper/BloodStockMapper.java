package com.example.demo.mapper;

import com.example.demo.dto.request.BloodStockRequest;
import com.example.demo.dto.response.BloodStockResponse;
import com.example.demo.entity.BloodStock;

public class BloodStockMapper {

    private BloodStockMapper() {
    }

    public static BloodStock toEntity(BloodStockRequest request) {

        BloodStock stock = new BloodStock();

        stock.setBloodGroup(request.getBloodGroup());
        stock.setUnits(request.getUnits() != null ? request.getUnits() : 0);

        return stock;
    }

    public static BloodStockResponse toResponse(BloodStock stock) {

        BloodStockResponse response = new BloodStockResponse();

        response.setId(stock.getId());
        response.setBloodGroup(stock.getBloodGroup());
        response.setUnits(stock.getUnits() != null ? stock.getUnits() : 0);
        response.setLastUpdated(stock.getLastUpdated());

        return response;
    }
}