package com.annam.shop.repo;

import com.annam.shop.domain.Cart;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CartRepository extends JpaRepository<Cart, Long> {

    @Query("""
            select distinct c from Cart c
            left join fetch c.items i
            left join fetch i.product p
            left join fetch p.category
            where c.customer.id = :customerId
            """)
    Optional<Cart> findDetailedByCustomerId(@Param("customerId") Long customerId);

    Optional<Cart> findByCustomerId(Long customerId);
}
