package com.annam.shop.web;

import com.annam.shop.generated.model.Cart;
import com.annam.shop.generated.model.CartItem;
import com.annam.shop.generated.model.Category;
import com.annam.shop.generated.model.Customer;
import com.annam.shop.generated.model.Order;
import com.annam.shop.generated.model.OrderItem;
import com.annam.shop.generated.model.OrderPage;
import com.annam.shop.generated.model.OrderStatus;
import com.annam.shop.generated.model.Product;
import com.annam.shop.generated.model.ProductPage;
import com.annam.shop.generated.model.Role;
import com.annam.shop.generated.model.Session;
import com.annam.shop.service.view.Views.CartView;
import com.annam.shop.service.view.Views.CategoryView;
import com.annam.shop.service.view.Views.CustomerView;
import com.annam.shop.service.view.Views.OrderView;
import com.annam.shop.service.view.Views.PageView;
import com.annam.shop.service.view.Views.ProductView;
import com.annam.shop.service.view.Views.SessionView;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import org.springframework.stereotype.Component;

@Component
public class ApiMapper {

    public Customer toCustomer(CustomerView view) {
        return new Customer(
                view.id(),
                view.fullName(),
                view.email(),
                view.phone(),
                view.address(),
                Role.valueOf(view.role().name()),
                time(view.createdAt()));
    }

    public Session toSession(SessionView view) {
        return new Session(view.accessToken(), view.tokenType(), view.expiresIn(), toCustomer(view.customer()));
    }

    public Category toCategory(CategoryView view) {
        return new Category(view.id(), view.name(), view.description());
    }

    public Product toProduct(ProductView view) {
        return new Product(
                view.id(),
                view.name(),
                view.description(),
                view.price(),
                view.imageUrl(),
                view.stockQuantity(),
                view.categoryId(),
                view.categoryName(),
                time(view.createdAt()));
    }

    public ProductPage toProductPage(PageView<ProductView> page) {
        return new ProductPage(
                page.content().stream().map(this::toProduct).toList(),
                page.page(),
                page.size(),
                page.totalElements(),
                page.totalPages());
    }

    public Cart toCart(CartView view) {
        return new Cart(
                view.id(),
                view.customerId(),
                view.items().stream().map(item -> new CartItem(
                        item.id(),
                        item.productId(),
                        item.productName(),
                        item.imageUrl(),
                        item.unitPrice(),
                        item.quantity(),
                        item.lineTotal(),
                        item.stockQuantity())).toList(),
                view.totalAmount());
    }

    public Order toOrder(OrderView view) {
        return new Order(
                view.id(),
                view.code(),
                OrderStatus.valueOf(view.status().name()),
                view.totalAmount(),
                view.shippingAddress(),
                view.note(),
                view.customerId(),
                view.customerName(),
                time(view.createdAt()),
                time(view.updatedAt()),
                view.items().stream().map(item -> new OrderItem(
                        item.id(),
                        item.productId(),
                        item.productName(),
                        item.unitPrice(),
                        item.quantity(),
                        item.lineTotal())).toList());
    }

    public OrderPage toOrderPage(PageView<OrderView> page) {
        return new OrderPage(
                page.content().stream().map(this::toOrder).toList(),
                page.page(),
                page.size(),
                page.totalElements(),
                page.totalPages());
    }

    private static OffsetDateTime time(Instant instant) {
        return instant == null ? null : instant.atOffset(ZoneOffset.UTC);
    }
}
