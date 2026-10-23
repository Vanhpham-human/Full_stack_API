package com.annam.shop.service.command;

public record ProductCommand(
        String name,
        String description,
        long price,
        String imageUrl,
        int stockQuantity,
        long categoryId
) {
}
