package com.annam.shop.repo;

import com.annam.shop.domain.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("delete from CartItem i where i.product.id = :productId")
    void deleteByProductId(@Param("productId") Long productId);
}
