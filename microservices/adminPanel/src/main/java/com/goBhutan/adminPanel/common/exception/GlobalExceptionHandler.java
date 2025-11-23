package com.goBhutan.adminPanel.common.exception;

import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.goBhutan.adminPanel.common.dto.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(InvalidFormatException.class)
    public ResponseEntity<ApiResponse<?>> handleInvalidFormat(InvalidFormatException ex) {

        String message = "Invalid value: '" + ex.getValue() + "'. ";

        if (ex.getTargetType() != null && ex.getTargetType().isEnum()) {
            Object[] constants = ex.getTargetType().getEnumConstants();
            message += "Valid values are: ";
            for (Object constant : constants) {
                message += constant.toString() + ", ";
            }
            message = message.substring(0, message.length() - 2); // remove last comma
        }

        return ResponseEntity.badRequest().body(ApiResponse.error(message));
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ApiResponse<?>> handleRuntime(RuntimeException ex) {
        return ResponseEntity.badRequest().body(ApiResponse.error(ex.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<?>> handleException(Exception ex) {
        return ResponseEntity.internalServerError()
                .body(ApiResponse.error("Internal server error: " + ex.getMessage()));
    }

}
