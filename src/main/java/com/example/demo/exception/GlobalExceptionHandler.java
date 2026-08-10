package com.example.demo.exception;

import java.time.LocalDateTime;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import com.example.demo.dto.response.ErrorResponse;

import jakarta.servlet.http.HttpServletRequest;

@ControllerAdvice
public class GlobalExceptionHandler {

    // ==========================================
    // Check whether request is REST API request
    // ==========================================

    private boolean isApiRequest(HttpServletRequest request) {

        String uri = request.getRequestURI();

        String acceptHeader = request.getHeader("Accept");

        return (uri != null && uri.startsWith("/api/"))
                || (acceptHeader != null
                        && acceptHeader.contains("application/json"));
    }

    // ==========================================
    // Build Standard API Error Response
    // ==========================================

    private ResponseEntity<ErrorResponse> buildJsonResponse(
            HttpStatus status,
            String message) {

        ErrorResponse errorResponse = new ErrorResponse(
                false,
                message,
                status.value(),
                status.getReasonPhrase(),
                LocalDateTime.now());

        return new ResponseEntity<>(
                errorResponse,
                status);
    }

    // ==========================================
    // Resource Not Found
    // ==========================================

    @ExceptionHandler(ResourceNotFoundException.class)
    public Object handleResourceNotFound(
            ResourceNotFoundException ex,
            HttpServletRequest request,
            Model model) {

        if (isApiRequest(request)) {

            return buildJsonResponse(
                    HttpStatus.NOT_FOUND,
                    ex.getMessage());
        }

        model.addAttribute("status", 404);
        model.addAttribute("errorMessage", ex.getMessage());

        return "error";
    }

    // ==========================================
    // Duplicate Resource
    // ==========================================

    @ExceptionHandler(DuplicateResourceException.class)
    public Object handleDuplicateResource(
            DuplicateResourceException ex,
            HttpServletRequest request,
            Model model) {

        if (isApiRequest(request)) {

            return buildJsonResponse(
                    HttpStatus.CONFLICT,
                    ex.getMessage());
        }

        model.addAttribute("status", 409);
        model.addAttribute("errorMessage", ex.getMessage());

        return "error";
    }

    // ==========================================
    // Invalid Request
    // ==========================================

    @ExceptionHandler(InvalidRequestException.class)
    public Object handleInvalidRequest(
            InvalidRequestException ex,
            HttpServletRequest request,
            Model model) {

        if (isApiRequest(request)) {

            return buildJsonResponse(
                    HttpStatus.BAD_REQUEST,
                    ex.getMessage());
        }

        model.addAttribute("status", 400);
        model.addAttribute("errorMessage", ex.getMessage());

        return "error";
    }

    // ==========================================
    // Http Message Not Readable (Malformed JSON / Invalid Enum)
    // ==========================================

    @ExceptionHandler(org.springframework.http.converter.HttpMessageNotReadableException.class)
    public Object handleHttpMessageNotReadable(
            org.springframework.http.converter.HttpMessageNotReadableException ex,
            HttpServletRequest request,
            Model model) {

        if (isApiRequest(request)) {
            return buildJsonResponse(
                    HttpStatus.BAD_REQUEST,
                    "Invalid request body format or parameter value.");
        }

        model.addAttribute("status", 400);
        model.addAttribute("errorMessage", "Invalid request body format or parameter value.");

        return "error";
    }

    // ==========================================
    // Runtime Exception
    // ==========================================

    @ExceptionHandler(RuntimeException.class)
    public Object handleRuntimeException(
            RuntimeException ex,
            HttpServletRequest request,
            Model model) {

        String message = (ex.getMessage() != null
                && !ex.getMessage().isBlank())
                        ? ex.getMessage()
                        : "An application runtime error occurred.";

        HttpStatus status = message.toLowerCase().contains("not found")
                ? HttpStatus.NOT_FOUND
                : HttpStatus.INTERNAL_SERVER_ERROR;

        if (isApiRequest(request)) {

            return buildJsonResponse(
                    status,
                    message);
        }

        model.addAttribute(
                "status",
                status.value());

        model.addAttribute(
                "errorMessage",
                message);

        return "error";
    }

    // ==========================================
    // Validation Exception
    // ==========================================

    @ExceptionHandler(org.springframework.web.bind.MethodArgumentNotValidException.class)
    public Object handleValidationException(
            org.springframework.web.bind.MethodArgumentNotValidException ex,
            HttpServletRequest request,
            Model model) {

        String message = ex.getBindingResult().getAllErrors().isEmpty()
                ? "Validation failed"
                : ex.getBindingResult().getAllErrors().get(0).getDefaultMessage();

        if (isApiRequest(request)) {

            return buildJsonResponse(
                    HttpStatus.BAD_REQUEST,
                    message);
        }

        model.addAttribute("status", 400);
        model.addAttribute("errorMessage", message);

        return "error";
    }

    // ==========================================
    // Method Not Supported
    // ==========================================

    @ExceptionHandler(org.springframework.web.HttpRequestMethodNotSupportedException.class)
    public Object handleMethodNotSupported(
            org.springframework.web.HttpRequestMethodNotSupportedException ex,
            HttpServletRequest request,
            Model model) {

        if (isApiRequest(request)) {

            return buildJsonResponse(
                    HttpStatus.METHOD_NOT_ALLOWED,
                    ex.getMessage());
        }

        model.addAttribute("status", 405);
        model.addAttribute("errorMessage", ex.getMessage());

        return "error";
    }

    // ==========================================
    // Media Type Not Supported
    // ==========================================

    @ExceptionHandler(org.springframework.web.HttpMediaTypeNotSupportedException.class)
    public Object handleMediaTypeNotSupported(
            org.springframework.web.HttpMediaTypeNotSupportedException ex,
            HttpServletRequest request,
            Model model) {

        if (isApiRequest(request)) {

            return buildJsonResponse(
                    HttpStatus.UNSUPPORTED_MEDIA_TYPE,
                    ex.getMessage());
        }

        model.addAttribute("status", 415);
        model.addAttribute("errorMessage", ex.getMessage());

        return "error";
    }

    // ==========================================
    // Method Argument Type Mismatch
    // ==========================================

    @ExceptionHandler(org.springframework.web.method.annotation.MethodArgumentTypeMismatchException.class)
    public Object handleTypeMismatch(
            org.springframework.web.method.annotation.MethodArgumentTypeMismatchException ex,
            HttpServletRequest request,
            Model model) {

        if (isApiRequest(request)) {

            return buildJsonResponse(
                    HttpStatus.BAD_REQUEST,
                    ex.getMessage());
        }

        model.addAttribute("status", 400);
        model.addAttribute("errorMessage", ex.getMessage());

        return "error";
    }

    // ==========================================
    // General Exception
    // ==========================================

    @ExceptionHandler(Exception.class)
    public Object handleGeneralException(
            Exception ex,
            HttpServletRequest request,
            Model model) {

        String message = "An unexpected system error occurred.";

        if (isApiRequest(request)) {

            return buildJsonResponse(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    message);
        }

        model.addAttribute(
                "status",
                500);

        model.addAttribute(
                "errorMessage",
                message);

        return "error";
    }
}