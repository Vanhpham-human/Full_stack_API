package com.annam.shop.repo;

import com.annam.shop.domain.ShopOrderItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ShopOrderItemRepository extends JpaRepository<ShopOrderItem, Long> {

    boolean existsByProduct_Id(Long productId);
}
