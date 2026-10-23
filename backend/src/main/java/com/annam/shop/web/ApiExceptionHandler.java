package com.annam.shop.web;

import com.annam.shop.exception.ApiException;
import com.annam.shop.exception.ErrorCode;
import com.annam.shop.web.ApiErrorBody.FieldErrorBody;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@Slf4j
@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ApiErrorBody> handleApi(ApiException ex) {
        return body(ex.getErrorCode(), ex.getMessage(), ex.getDetails().stream()
                .map(issue -> new FieldErrorBody(issue.field(), issue.issue()))
                .toList());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorBody> handleInvalid(MethodArgumentNotValidException ex) {
        List<FieldErrorBody> details = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> new FieldErrorBody(error.getField(), vietnamese(error.getField(), error.getCode(), error.getDefaultMessage())))
                .toList();
        return body(ErrorCode.ERR_VALIDATION, ErrorCode.ERR_VALIDATION.defaultMessage(), details);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiErrorBody> handleConstraint(ConstraintViolationException ex) {
        List<FieldErrorBody> details = ex.getConstraintViolations().stream()
                .map(violation -> {
                    String path = violation.getPropertyPath().toString();
                    String field = path.contains(".") ? path.substring(path.lastIndexOf('.') + 1) : path;
                    return new FieldErrorBody(field, violation.getMessage());
                })
                .toList();
        return body(ErrorCode.ERR_VALIDATION, ErrorCode.ERR_VALIDATION.defaultMessage(), details);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiErrorBody> handleUnreadable(HttpMessageNotReadableException ex) {
        return body(ErrorCode.ERR_VALIDATION, "JSON không hợp lệ", List.of());
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiErrorBody> handleConflict(DataIntegrityViolationException ex) {
        String raw = String.valueOf(ex.getMostSpecificCause().getMessage()).toLowerCase();
        if (raw.contains("email")) {
            return body(ErrorCode.ERR_EMAIL_EXISTS, ErrorCode.ERR_EMAIL_EXISTS.defaultMessage(), List.of());
        }
        if (raw.contains("phone")) {
            return body(ErrorCode.ERR_PHONE_EXISTS, ErrorCode.ERR_PHONE_EXISTS.defaultMessage(), List.of());
        }
        return body(ErrorCode.ERR_VALIDATION, "Dữ liệu bị trùng hoặc không hợp lệ", List.of());
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiErrorBody> handleDenied(AccessDeniedException ex) {
        return body(ErrorCode.ERR_FORBIDDEN, ErrorCode.ERR_FORBIDDEN.defaultMessage(), List.of());
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ApiErrorBody> handleMissing(NoResourceFoundException ex) {
        return body(ErrorCode.ERR_NOT_FOUND, "Không tìm thấy đường dẫn", List.of());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorBody> handleUnexpected(Exception ex, HttpServletRequest request) {
        log.error("Lỗi không xử lý được tại {}", request.getRequestURI(), ex);
        return body(ErrorCode.ERR_INTERNAL, ErrorCode.ERR_INTERNAL.defaultMessage(), List.of());
    }

    private static ResponseEntity<ApiErrorBody> body(ErrorCode code, String message, List<FieldErrorBody> details) {
        return ResponseEntity.status(code.status()).body(new ApiErrorBody(code.name(), message, details));
    }

    private static String vietnamese(String field, String code, String fallback) {
        if ("Pattern".equals(code) && "phone".equals(field)) {
            return "Số điện thoại không hợp lệ";
        }
        if ("Pattern".equals(code) && "password".equals(field)) {
            return "Mật khẩu tối thiểu 8 ký tự, gồm chữ và số";
        }
        if ("Email".equals(code)) {
            return "Email không hợp lệ";
        }
        if ("NotNull".equals(code) || "NotBlank".equals(code)) {
            return "Không được để trống";
        }
        if ("Size".equals(code) || "Length".equals(code)) {
            return "Độ dài không hợp lệ";
        }
        if ("Min".equals(code) || "Max".equals(code) || "DecimalMin".equals(code) || "DecimalMax".equals(code)) {
            return "Giá trị nằm ngoài khoảng cho phép";
        }
        return fallback == null ? "Không hợp lệ" : fallback;
    }
}
