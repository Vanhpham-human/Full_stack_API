package com.annam.shop.web;

import java.util.List;

public record ApiErrorBody(String code, String message, List<FieldErrorBody> details) {

    public record FieldErrorBody(String field, String issue) {
    }
}
