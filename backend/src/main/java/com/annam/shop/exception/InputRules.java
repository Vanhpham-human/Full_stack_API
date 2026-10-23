package com.annam.shop.exception;

import java.util.regex.Pattern;

public final class InputRules {

    private static final Pattern EMAIL = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");
    private static final Pattern PHONE = Pattern.compile("^(0|\\+84)(3|5|7|8|9)\\d{8}$");
    private static final Pattern PASSWORD = Pattern.compile("^(?=.*[A-Za-z])(?=.*\\d).{8,72}$");

    private InputRules() {
    }

    public static String email(String value) {
        String email = requiredText(value, "email", 5, 255).toLowerCase();
        if (!EMAIL.matcher(email).matches()) {
            throw invalid("email", "Email không hợp lệ");
        }
        return email;
    }

    public static String phone(String value) {
        String phone = requiredText(value, "phone", 10, 16);
        if (!PHONE.matcher(phone).matches()) {
            throw invalid("phone", "Số điện thoại không hợp lệ");
        }
        return phone;
    }

    public static String password(String value) {
        if (value == null || !PASSWORD.matcher(value).matches()) {
            throw invalid("password", "Mật khẩu tối thiểu 8 ký tự, gồm chữ và số");
        }
        return value;
    }

    public static String requiredText(String value, String field, int min, int max) {
        if (value == null || value.isBlank()) {
            throw invalid(field, "Không được để trống");
        }
        String trimmed = value.trim();
        if (trimmed.length() < min || trimmed.length() > max) {
            throw invalid(field, "Độ dài phải từ " + min + " đến " + max + " ký tự");
        }
        return trimmed;
    }

    public static String optionalText(String value, String field, int max) {
        if (value == null || value.isBlank()) {
            return null;
        }
        String trimmed = value.trim();
        if (trimmed.length() > max) {
            throw invalid(field, "Tối đa " + max + " ký tự");
        }
        return trimmed;
    }

    public static void nonNegative(int value, String field) {
        if (value < 0) {
            throw invalid(field, "Không được âm");
        }
    }

    public static void positiveMoney(long value, String field) {
        if (value < 1) {
            throw invalid(field, "Giá phải lớn hơn 0");
        }
    }

    public static void quantity(int value) {
        if (value < 1 || value > 99) {
            throw invalid("quantity", "Số lượng phải từ 1 đến 99");
        }
    }

    private static ApiException invalid(String field, String issue) {
        return new ApiException(ErrorCode.ERR_VALIDATION, ErrorCode.ERR_VALIDATION.defaultMessage(),
                java.util.List.of(new FieldIssue(field, issue)));
    }
}
