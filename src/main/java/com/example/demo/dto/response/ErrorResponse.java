package com.example.demo.dto.response;

import java.time.LocalDateTime;

public class ErrorResponse {

    private boolean success;
    private String message;
    private int status;
    private String error;
    private LocalDateTime timestamp;

    public ErrorResponse() {
    }

    public ErrorResponse(
            boolean success,
            String message,
            int status,
            String error,
            LocalDateTime timestamp) {

        this.success = success;
        this.message = message;
        this.status = status;
        this.error = error;
        this.timestamp = timestamp;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
}