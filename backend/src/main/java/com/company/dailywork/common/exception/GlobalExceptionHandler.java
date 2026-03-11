package com.company.dailywork.common.exception;

import com.company.dailywork.common.model.ApiResponse;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ApiResponse<Void> handleValidationException(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(err -> err.getField() + " " + err.getDefaultMessage())
                .orElse("Invalid request");
        return ApiResponse.fail(400, message);
    }

    @ExceptionHandler(BindException.class)
    public ApiResponse<Void> handleBindException(BindException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(err -> err.getField() + " " + err.getDefaultMessage())
                .orElse("Invalid request");
        return ApiResponse.fail(400, message);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ApiResponse<Void> handleBodyParseException() {
        return ApiResponse.fail(400, "Request body format is invalid");
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ApiResponse<Void> handleIllegalArgumentException(IllegalArgumentException ex) {
        return ApiResponse.fail(400, ex.getMessage());
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ApiResponse<Void> handleAccessDeniedException() {
        return ApiResponse.fail(403, "Forbidden");
    }

    @ExceptionHandler(Exception.class)
    public ApiResponse<Void> handleGenericException(Exception ex) {
        return ApiResponse.fail(500, ex.getMessage());
    }
}
