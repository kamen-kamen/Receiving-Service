package com.waregang.receiving_service.common.exception_handling;

import com.waregang.receiving_service.common.exception_handling.error_code.ErrorCode;
import com.waregang.receiving_service.common.idempotency.IdempotencyKeyConflictException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Component;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.net.URI;
import java.time.Instant;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j

@Component
@RequiredArgsConstructor
public class ProblemDetailSimpleFactory {

    private final MessageSource messageSource;

    private ProblemDetail baseProblemDetail(
            HttpStatus status,
            String title,
            String detail
    ) {
        ProblemDetail pd = ProblemDetail
                .forStatusAndDetail(status, detail);

        pd.setTitle(title);
        pd.setType(URI.create("about:blank"));
        pd.setProperty("timestamp", Instant.now());

        return pd;
    }

    public ProblemDetail create(IdempotencyKeyConflictException ex) {
        return baseProblemDetail(
                HttpStatus.CONFLICT,
                "Idempotency key conflict",
                ex.getMessage()
        );
    }

    public ProblemDetail create(AppException ex) {
        ErrorCode errorCode = ex.getErrorCode();
        String code = errorCode.getCode();
        String message = messageSource.getMessage(code, null, code, LocaleContextHolder.getLocale());

        ProblemDetail pd = baseProblemDetail(
                errorCode.getHttpStatus(),
                message,
                message
        );

        pd.setProperty("error_code", code);

        if (!ex.getErrorContext().isEmpty() && pd.getProperties() != null)
            pd.getProperties().putAll(ex.getErrorContext());

        return pd;
    }

    public ProblemDetail create(AccessDeniedException accessDeniedException) {
        return baseProblemDetail(
                HttpStatus.FORBIDDEN,
                "Access denied",
                accessDeniedException.getMessage()
        );
    }

    public ProblemDetail create(AuthenticationException authenticationException) {
        return baseProblemDetail(
                HttpStatus.UNAUTHORIZED,
                "Authentication failed",
                authenticationException.getMessage()
        );

    }

    public ProblemDetail create(ObjectOptimisticLockingFailureException optimisticLockException) {
        return baseProblemDetail(
                HttpStatus.CONFLICT,
                "Smth wrong: try again :(",
                optimisticLockException.getMessage()
        );

    }

    public ProblemDetail create(Exception e) {
        return switch (e) {
            case MethodArgumentNotValidException ex -> buildValidationPD(extractErrors(ex));
            case ConstraintViolationException ex -> buildValidationPD(extractErrors(ex));

            default -> baseProblemDetail(HttpStatus.INTERNAL_SERVER_ERROR, "Server Error", ":(");
        };
    }

    private ProblemDetail buildValidationPD(Map<String, String> errors) {
        ProblemDetail pd = baseProblemDetail(HttpStatus.BAD_REQUEST, "Bad request", "Validation Error");
        pd.setProperty("invalid_params", errors);
        return pd;
    }

    private Map<String, String> extractErrors(MethodArgumentNotValidException ex) {
        return ex.getBindingResult().getFieldErrors().stream()
                .collect(Collectors.toMap(FieldError::getField,
                        e -> e.getDefaultMessage() != null ? e.getDefaultMessage() : "Invalid value", this::mergeMessages));
    }

    private Map<String, String> extractErrors(ConstraintViolationException ex) {
        return ex.getConstraintViolations().stream()
                .collect(Collectors.toMap(
                        v -> v.getPropertyPath().toString(),
                        ConstraintViolation::getMessage,
                        this::mergeMessages
                ));
    }

    private String mergeMessages(String existing, String replacement) {
        return existing + " | " + replacement;
    }

}