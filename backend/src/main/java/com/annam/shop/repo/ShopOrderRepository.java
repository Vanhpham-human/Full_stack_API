package com.annam.shop.repo;

import com.annam.shop.domain.OrderStatus;
import com.annam.shop.domain.ShopOrder;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ShopOrderRepository extends JpaRepository<ShopOrder, Long> {

    @Query("""
            select distinct o from ShopOrder o
            left join fetch o.items i
            left join fetch i.product
            left join fetch o.customer
            where o.id = :id
            """)
    Optional<ShopOrder> findDetailedById(@Param("id") Long id);

    Page<ShopOrder> findByCustomer_Id(Long customerId, Pageable pageable);

    @Query("select o from ShopOrder o where (:status is null or o.status = :status)")
    Page<ShopOrder> search(@Param("status") OrderStatus status, Pageable pageable);
}
