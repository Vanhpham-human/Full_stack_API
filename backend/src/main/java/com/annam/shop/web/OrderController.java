package com.annam.shop.web;

import com.annam.shop.domain.OrderStatus;
import com.annam.shop.generated.api.OrdersApi;
import com.annam.shop.generated.model.CheckoutRequest;
import com.annam.shop.generated.model.Order;
import com.annam.shop.generated.model.OrderPage;
import com.annam.shop.generated.model.OrderStatusUpdate;
import com.annam.shop.security.CurrentUser;
import com.annam.shop.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequiredArgsConstructor
public class OrderController implements OrdersApi {

    private final OrderService orders;
    private final ApiMapper mapper;

    @Override
    public ResponseEntity<Order> createOrder(CheckoutRequest request) {
        var order = orders.checkout(CurrentUser.require().id(), request.shippingAddress(), request.note());
        return ResponseEntity.status(HttpStatus.CREATED).body(mapper.toOrder(order));
    }

    @Override
    public ResponseEntity<OrderPage> listOrders(
            com.annam.shop.generated.model.OrderStatus status,
            Integer page,
            Integer size
    ) {
        OrderStatus domainStatus = status == null ? null : OrderStatus.valueOf(status.name());
        return ResponseEntity.ok(mapper.toOrderPage(orders.listAll(domainStatus, page, size)));
    }

    @Override
    public ResponseEntity<Order> getOrder(Long id) {
        return ResponseEntity.ok(mapper.toOrder(orders.get(id, CurrentUser.require())));
    }

    @Override
    public ResponseEntity<Order> updateOrderStatus(Long id, OrderStatusUpdate request) {
        var order = orders.updateStatus(id, OrderStatus.valueOf(request.status().name()), CurrentUser.require());
        return ResponseEntity.ok(mapper.toOrder(order));
    }
}
