package com.waregang.receiving_service.common.exception_handling;

import jakarta.validation.ConstraintViolationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.http.*;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@Slf4j
@RequiredArgsConstructor
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {
    private final ProblemDetailSimpleFactory problemDetailFactory;

    @ExceptionHandler(AppException.class)
    ProblemDetail handleBusinessException(AppException ex) {
        log.error("Handling business exception: {}. Error Code: {}. Context: {}", ex.getMessage(), ex.getErrorCode().getCode(), ex.getErrorContext(), ex);
        return problemDetailFactory.create(ex);
    }

    @Override
    protected @Nullable ResponseEntity<Object> handleMethodArgumentNotValid(
            @NonNull MethodArgumentNotValidException ex,
            @NonNull HttpHeaders headers,
            @NonNull HttpStatusCode status,
            @NonNull WebRequest request
    ) {
        log.warn("Handling method argument not valid exception: {}. Errors: {}", ex.getMessage(), ex.getBindingResult().getFieldErrors(), ex);
        ProblemDetail problemDetail = problemDetailFactory.create(ex);
        return createResponseEntity(problemDetail, headers, status, request);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    ProblemDetail handleConstraintViolation(ConstraintViolationException validationException) {
        log.warn("Handling constraint violation exception: {}. Violations: {}", validationException.getMessage(), validationException.getConstraintViolations(), validationException);
        return problemDetailFactory.create(validationException);
    }

    @ExceptionHandler(Exception.class)
    ProblemDetail handleGenericException(Exception genericException) {
        log.error("Handling generic exception: {}", genericException.getMessage(), genericException);
        return problemDetailFactory.create(genericException);
    }

    @ExceptionHandler(AccessDeniedException.class)
    ProblemDetail handleAccessDeniedException(AccessDeniedException accessDeniedException) {
        log.warn("Handling access denied exception: {}", accessDeniedException.getMessage(), accessDeniedException);
        return problemDetailFactory.create(accessDeniedException);
    }

    @ExceptionHandler(AuthenticationException.class)
    ProblemDetail handleAuthenticationException(AuthenticationException authenticationException) {
        log.warn("Handling authentication exception: {}", authenticationException.getMessage(), authenticationException);
        return problemDetailFactory.create(authenticationException);
    }
}