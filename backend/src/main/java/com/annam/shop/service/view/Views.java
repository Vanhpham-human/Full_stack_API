package com.annam.shop.service.view;

import com.annam.shop.domain.OrderStatus;
import com.annam.shop.domain.Role;
import java.time.Instant;
import java.util.List;

public final class Views {

    private Views() {
    }

    public record CustomerView(
            Long id,
            String fullName,
            String email,
            String phone,
            String address,
            Role role,
            Instant createdAt
    ) {
    }

    public record CategoryView(Long id, String name, String description) {
    }

    public record ProductView(
            Long id,
            String name,
            String description,
            long price,
            String imageUrl,
            int stockQuantity,
            Long categoryId,
            String categoryName,
            Instant createdAt
    ) {
    }

    public record CartItemView(
            Long id,
            Long productId,
            String productName,
            String imageUrl,
            long unitPrice,
            int quantity,
            long lineTotal,
            int stockQuantity
    ) {
    }

    public record CartView(Long id, Long customerId, List<CartItemView> items, long totalAmount) {
    }

    public record OrderItemView(
            Long id,
            Long productId,
            String productName,
            long unitPrice,
            int quantity,
            long lineTotal
    ) {
    }

    public record OrderView(
            Long id,
            String code,
            OrderStatus status,
            long totalAmount,
            String shippingAddress,
            String note,
            Long customerId,
            String customerName,
            Instant createdAt,
            Instant updatedAt,
            List<OrderItemView> items
    ) {
    }

    public record PageView<T>(List<T> content, int page, int size, long totalElements, int totalPages) {
    }

    public record SessionView(String accessToken, String tokenType, long expiresIn, CustomerView customer) {
    }
}
