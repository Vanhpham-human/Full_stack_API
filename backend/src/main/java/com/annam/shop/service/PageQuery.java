package com.annam.shop.service;

import com.annam.shop.exception.ApiException;
import com.annam.shop.exception.ErrorCode;
import com.annam.shop.exception.FieldIssue;
import java.util.List;

public record PageQuery(int page, int size) {

    public static PageQuery of(Integer page, Integer size, int defaultSize) {
        int resolvedPage = page == null ? 0 : page;
        int resolvedSize = size == null ? defaultSize : size;
        if (resolvedPage < 0 || resolvedSize < 1 || resolvedSize > 60) {
            throw new ApiException(
                    ErrorCode.ERR_VALIDATION,
                    ErrorCode.ERR_VALIDATION.defaultMessage(),
                    List.of(new FieldIssue("page", "page >= 0 và size từ 1 đến 60")));
        }
        return new PageQuery(resolvedPage, resolvedSize);
    }
}
