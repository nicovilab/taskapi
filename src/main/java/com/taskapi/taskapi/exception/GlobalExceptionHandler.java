package com.taskapi.taskapi.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.apache.coyote.Response;
import org.flywaydb.core.api.callback.Error;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class) //404
    public ResponseEntity<ErrorResponse> handleResourceNotFound(ResourceNotFoundException ex, HttpServletRequest request){
        return buildResponse(HttpStatus.NOT_FOUND, ex.getMessage(), request, null);
    }

    @ExceptionHandler(DuplicateResourceException.class) //409
    public ResponseEntity<ErrorResponse> handleDuplicateResource (DuplicateResourceException ex, HttpServletRequest request){
        return buildResponse(HttpStatus.CONFLICT, ex.getMessage(), request, null);
    }

    @ExceptionHandler(BadCredentialsException.class) //401
    public ResponseEntity<ErrorResponse> handleBadCredentials(BadCredentialsException ex, HttpServletRequest request){
        return buildResponse(HttpStatus.UNAUTHORIZED, "Incorrect credentials", request, null);
    }

    @ExceptionHandler(UsernameNotFoundException.class) //401
    public ResponseEntity<ErrorResponse> handleUsernameNotFound(UsernameNotFoundException ex, HttpServletRequest request){
        return buildResponse(HttpStatus.UNAUTHORIZED, "Incorrect credentials", request, null);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class) //400
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex, HttpServletRequest request){
        Map<String, String> fieldErrors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(e -> fieldErrors.put(e.getField(), e.getDefaultMessage()));

        return buildResponse(HttpStatus.BAD_REQUEST, "Validation error", request, fieldErrors);
    }

    @ExceptionHandler(Exception.class) //500
    public ResponseEntity<ErrorResponse> handleGeneral(Exception ex, HttpServletRequest request){
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error", request, null);
    }


    private ResponseEntity<ErrorResponse> buildResponse(HttpStatus status, String message, HttpServletRequest request, Map<String, String> fieldErrors){
        ErrorResponse response = ErrorResponse.builder()
                .status(status.value())
                .error(status.getReasonPhrase())
                .message(message)
                .fieldErrors(fieldErrors)
                .timestamp(LocalDateTime.now())
                .path(request.getRequestURI())
                .build();

        return ResponseEntity.status(status).body(response);
    }
}
