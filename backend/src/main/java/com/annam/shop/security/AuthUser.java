package com.annam.shop.security;

import com.annam.shop.domain.Role;

public record AuthUser(Long id, String email, Role role) {
}
