package com.annam.shop.exception;

import org.springframework.http.HttpStatus;

public enum ErrorCode {
    ERR_VALIDATION(HttpStatus.BAD_REQUEST, "Dữ liệu không hợp lệ"),
    ERR_NOT_FOUND(HttpStatus.NOT_FOUND, "Không tìm thấy dữ liệu"),
    ERR_EMAIL_EXISTS(HttpStatus.UNPROCESSABLE_ENTITY, "Email đã được sử dụng"),
    ERR_PHONE_EXISTS(HttpStatus.UNPROCESSABLE_ENTITY, "Số điện thoại đã được sử dụng"),
    ERR_INVALID_CREDENTIALS(HttpStatus.UNAUTHORIZED, "Email hoặc mật khẩu không đúng"),
    ERR_UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "Bạn cần đăng nhập"),
    ERR_FORBIDDEN(HttpStatus.FORBIDDEN, "Bạn không có quyền thực hiện thao tác này"),
    ERR_STOCK_EMPTY(HttpStatus.UNPROCESSABLE_ENTITY, "Sản phẩm đã hết hàng"),
    ERR_CART_EMPTY(HttpStatus.UNPROCESSABLE_ENTITY, "Giỏ hàng đang trống"),
    ERR_INVALID_STATUS(HttpStatus.UNPROCESSABLE_ENTITY, "Không thể chuyển trạng thái đơn hàng"),
    ERR_CATEGORY_EXISTS(HttpStatus.UNPROCESSABLE_ENTITY, "Tên danh mục đã tồn tại"),
    ERR_CATEGORY_IN_USE(HttpStatus.UNPROCESSABLE_ENTITY, "Danh mục đang có sản phẩm, không thể xóa"),
    ERR_PRODUCT_IN_USE(HttpStatus.UNPROCESSABLE_ENTITY, "Sản phẩm đã nằm trong đơn hàng, không thể xóa"),
    ERR_INTERNAL(HttpStatus.INTERNAL_SERVER_ERROR, "Lỗi hệ thống");

    private final HttpStatus status;
    private final String defaultMessage;

    ErrorCode(HttpStatus status, String defaultMessage) {
        this.status = status;
        this.defaultMessage = defaultMessage;
    }

    public HttpStatus status() {
        return status;
    }

    public String defaultMessage() {
        return defaultMessage;
    }
}
