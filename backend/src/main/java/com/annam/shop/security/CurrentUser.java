package com.annam.shop.security;

import com.annam.shop.exception.ApiException;
import com.annam.shop.exception.ErrorCode;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public final class CurrentUser {

    private CurrentUser() {
    }

    public static AuthUser require() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof AuthUser user)) {
            throw new ApiException(ErrorCode.ERR_UNAUTHORIZED);
        }
        return user;
    }
}
