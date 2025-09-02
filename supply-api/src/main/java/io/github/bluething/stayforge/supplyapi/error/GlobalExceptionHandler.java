package io.github.bluething.stayforge.supplyapi.error;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.List;
import java.util.Map;

@RestControllerAdvice
@Slf4j
class GlobalExceptionHandler {
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ProblemDetail> handleBusinessException(
            BusinessException ex, HttpServletRequest request) {

        ProblemDetail problem = ProblemDetail.builder()
                .type(ex.getErrorCode().getTypeUri())
                .title(ex.getErrorCode().getTitle())
                .status(ex.getHttpStatus().value())
                .detail(ex.getMessage())
                .instance(request.getRequestURI())
                .build();

        return ResponseEntity.status(ex.getHttpStatus()).body(problem);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ProblemDetail> handleGenericException(
            Exception ex, HttpServletRequest request) {

        log.error("Unexpected error", ex);

        ProblemDetail problem = ProblemDetail.builder()
                .type(ErrorCode.INTERNAL_SERVER_ERROR.getTypeUri())
                .title(ErrorCode.INTERNAL_SERVER_ERROR.getTitle())
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .detail("An unexpected error occurred")
                .instance(request.getRequestURI())
                .build();

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(problem);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ProblemDetail> handleValidationException(
            MethodArgumentNotValidException ex, HttpServletRequest request) {

        List<ValidationError> errors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> new ValidationError(
                        error.getField(),
                        error.getRejectedValue(),
                        error.getDefaultMessage()
                ))
                .toList();

        Map<String, Object> extensions = Map.of("validation_errors", errors);

        ProblemDetail problem = ProblemDetail.builder()
                .type(ErrorCode.VALIDATION_ERROR.getTypeUri())
                .title(ErrorCode.VALIDATION_ERROR.getTitle())
                .status(HttpStatus.BAD_REQUEST.value())
                .detail("Request validation failed")
                .instance(request.getRequestURI())
                .extensions(extensions)
                .build();

        return ResponseEntity.badRequest().body(problem);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ProblemDetail> handleConstraintViolationException(
            ConstraintViolationException ex, HttpServletRequest request) {

        List<ValidationError> errors = ex.getConstraintViolations()
                .stream()
                .map(violation -> new ValidationError(
                        violation.getPropertyPath().toString(),
                        violation.getInvalidValue(),
                        violation.getMessage()
                ))
                .toList();

        Map<String, Object> extensions = Map.of("validation_errors", errors);

        ProblemDetail problem = ProblemDetail.builder()
                .type(ErrorCode.VALIDATION_ERROR.getTypeUri())
                .title(ErrorCode.VALIDATION_ERROR.getTitle())
                .status(HttpStatus.BAD_REQUEST.value())
                .detail("Path parameter or request parameter validation failed")
                .instance(request.getRequestURI())
                .extensions(extensions)
                .build();

        return ResponseEntity.badRequest().body(problem);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ProblemDetail> handleHttpMessageNotReadableException(
            HttpMessageNotReadableException ex, HttpServletRequest request) {

        ProblemDetail problem = ProblemDetail.builder()
                .type(ErrorCode.INVALID_REQUEST_FORMAT.getTypeUri())
                .title(ErrorCode.INVALID_REQUEST_FORMAT.getTitle())
                .status(HttpStatus.BAD_REQUEST.value())
                .detail("Request body is malformed or contains invalid JSON")
                .instance(request.getRequestURI())
                .build();

        return ResponseEntity.badRequest().body(problem);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ProblemDetail> handleDataIntegrityViolationException(
            DataIntegrityViolationException ex, HttpServletRequest request) {

        log.error("Data integrity violation", ex);

        String detail = "Data integrity constraint violation";
        ErrorCode errorCode = ErrorCode.INTERNAL_SERVER_ERROR;

        // Check for common constraint violations
        String message = ex.getMessage() != null ? ex.getMessage().toLowerCase() : "";
        if (message.contains("duplicate") || message.contains("unique")) {
            detail = "Duplicate value detected";
            errorCode = ErrorCode.DUPLICATE_SLUG;
        }

        ProblemDetail problem = ProblemDetail.builder()
                .type(errorCode.getTypeUri())
                .title(errorCode.getTitle())
                .status(HttpStatus.CONFLICT.value())
                .detail(detail)
                .instance(request.getRequestURI())
                .build();

        return ResponseEntity.status(HttpStatus.CONFLICT).body(problem);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ProblemDetail> handleMethodArgumentTypeMismatch(
            MethodArgumentTypeMismatchException ex, HttpServletRequest request) {

        Map<String, Object> extensions = getStringObjectMap(ex);

        ProblemDetail problem = ProblemDetail.builder()
                .type(ErrorCode.VALIDATION_ERROR.getTypeUri())
                .title(ErrorCode.VALIDATION_ERROR.getTitle())
                .status(HttpStatus.BAD_REQUEST.value())
                .detail("Invalid parameter type")
                .instance(request.getRequestURI())
                .extensions(extensions)
                .build();

        return ResponseEntity.badRequest().body(problem);
    }

    private static Map<String, Object> getStringObjectMap(MethodArgumentTypeMismatchException ex) {
        String parameterName = ex.getName();
        Object invalidValue = ex.getValue();
        String expectedType = ex.getRequiredType().getSimpleName();

        List<ValidationError> errors = List.of(
                new ValidationError(
                        parameterName,
                        invalidValue,
                        String.format("Cannot convert '%s' to %s", invalidValue, expectedType)
                )
        );

        return Map.of("validation_errors", errors);
    }
}
