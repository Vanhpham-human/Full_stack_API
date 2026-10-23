package com.annam.shop.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.annam.shop.domain.Cart;
import com.annam.shop.domain.CartItem;
import com.annam.shop.domain.Customer;
import com.annam.shop.domain.OrderStatus;
import com.annam.shop.domain.Product;
import com.annam.shop.domain.Role;
import com.annam.shop.domain.ShopOrder;
import com.annam.shop.domain.ShopOrderItem;
import com.annam.shop.exception.ApiException;
import com.annam.shop.exception.ErrorCode;
import com.annam.shop.repo.CartRepository;
import com.annam.shop.repo.CustomerRepository;
import com.annam.shop.repo.ProductRepository;
import com.annam.shop.repo.ShopOrderRepository;
import com.annam.shop.security.AuthUser;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private ShopOrderRepository orders;
    @Mock
    private CustomerRepository customers;
    @Mock
    private CartRepository carts;
    @Mock
    private ProductRepository products;

    private OrderService service;
    private Customer customer;

    @BeforeEach
    void setUp() {
        service = new OrderService(orders, customers, carts, products);
        customer = new Customer();
        customer.setId(7L);
        customer.setFullName("Khách demo");
        customer.setAddress("12 Hàng Bông, Hoàn Kiếm, Hà Nội");
        customer.setRole(Role.CUSTOMER);
    }

    @Test
    void checkoutCreatesPendingOrderAndDeductsStock() {
        Product kettle = product(1L, "Ấm đun", 100_000, 5);
        Cart cart = cartWith(line(kettle, 2));
        when(customers.findById(7L)).thenReturn(Optional.of(customer));
        when(carts.findDetailedByCustomerId(7L)).thenReturn(Optional.of(cart));
        when(products.findByIdForUpdate(1L)).thenReturn(Optional.of(kettle));
        when(orders.save(any(ShopOrder.class))).thenAnswer(invocation -> {
            ShopOrder saved = invocation.getArgument(0);
            saved.setId(15L);
            saved.getItems().forEach(item -> item.setId(30L));
            return saved;
        });

        var order = service.checkout(7L, null, "Giao giờ hành chính");

        assertThat(order.status()).isEqualTo(OrderStatus.PENDING);
        assertThat(order.code()).startsWith("ORD-");
        assertThat(order.totalAmount()).isEqualTo(200_000);
        assertThat(order.items()).hasSize(1);
        assertThat(kettle.getStockQuantity()).isEqualTo(3);
        assertThat(cart.getItems()).isEmpty();
        verify(orders).save(any(ShopOrder.class));
    }

    @Test
    void checkoutFailsWhenAnyLineExceedsStockAndDoesNotChangeQuantities() {
        Product kettle = product(1L, "Ấm đun", 100_000, 5);
        Product candle = product(2L, "Nến thơm", 50_000, 0);
        Cart cart = cartWith(line(kettle, 2), line(candle, 1));
        when(customers.findById(7L)).thenReturn(Optional.of(customer));
        when(carts.findDetailedByCustomerId(7L)).thenReturn(Optional.of(cart));
        when(products.findByIdForUpdate(1L)).thenReturn(Optional.of(kettle));
        when(products.findByIdForUpdate(2L)).thenReturn(Optional.of(candle));

        assertThatThrownBy(() -> service.checkout(7L, customer.getAddress(), null))
                .isInstanceOf(ApiException.class)
                .extracting(ex -> ((ApiException) ex).getErrorCode())
                .isEqualTo(ErrorCode.ERR_STOCK_EMPTY);

        assertThat(kettle.getStockQuantity()).isEqualTo(5);
        assertThat(candle.getStockQuantity()).isEqualTo(0);
        assertThat(cart.getItems()).hasSize(2);
        verify(orders, never()).save(any());
    }

    @Test
    void checkoutRejectsEmptyCart() {
        Cart cart = cartWith();
        when(customers.findById(7L)).thenReturn(Optional.of(customer));
        when(carts.findDetailedByCustomerId(7L)).thenReturn(Optional.of(cart));

        assertThatThrownBy(() -> service.checkout(7L, customer.getAddress(), null))
                .isInstanceOf(ApiException.class)
                .extracting(ex -> ((ApiException) ex).getErrorCode())
                .isEqualTo(ErrorCode.ERR_CART_EMPTY);
        verify(orders, never()).save(any());
    }

    @Test
    void cancelRestoresStock() {
        Product kettle = product(1L, "Ấm đun", 100_000, 3);
        ShopOrder order = pendingOrder(kettle, 2);
        when(orders.findDetailedById(15L)).thenReturn(Optional.of(order));
        when(products.findByIdForUpdate(1L)).thenReturn(Optional.of(kettle));

        var updated = service.updateStatus(15L, OrderStatus.CANCELLED, new AuthUser(1L, "admin@annam.vn", Role.ADMIN));

        assertThat(updated.status()).isEqualTo(OrderStatus.CANCELLED);
        assertThat(kettle.getStockQuantity()).isEqualTo(5);
    }

    @Test
    void shippedOrderCannotBeCancelled() {
        Product kettle = product(1L, "Ấm đun", 100_000, 3);
        ShopOrder order = pendingOrder(kettle, 2);
        order.setStatus(OrderStatus.SHIPPED);
        when(orders.findDetailedById(15L)).thenReturn(Optional.of(order));

        assertThatThrownBy(() -> service.updateStatus(15L, OrderStatus.CANCELLED, new AuthUser(1L, "admin@annam.vn", Role.ADMIN)))
                .isInstanceOf(ApiException.class)
                .extracting(ex -> ((ApiException) ex).getErrorCode())
                .isEqualTo(ErrorCode.ERR_INVALID_STATUS);
        assertThat(kettle.getStockQuantity()).isEqualTo(3);
        verify(products, never()).findByIdForUpdate(any());
    }

    private Cart cartWith(CartItem... items) {
        Cart cart = new Cart();
        cart.setId(4L);
        cart.setCustomer(customer);
        cart.setItems(new ArrayList<>(List.of(items)));
        return cart;
    }

    private CartItem line(Product product, int quantity) {
        CartItem item = new CartItem();
        item.setId(product.getId());
        item.setProduct(product);
        item.setQuantity(quantity);
        return item;
    }

    private Product product(Long id, String name, long price, int stock) {
        Product product = new Product();
        product.setId(id);
        product.setName(name);
        product.setPrice(price);
        product.setStockQuantity(stock);
        return product;
    }

    private ShopOrder pendingOrder(Product product, int quantity) {
        ShopOrder order = new ShopOrder();
        order.setId(15L);
        order.setCode("ORD-20261023-TESTCODE");
        order.setCustomer(customer);
        order.setStatus(OrderStatus.PENDING);
        order.setShippingAddress(customer.getAddress());
        order.setTotalAmount(product.getPrice() * quantity);
        order.setCreatedAt(Instant.parse("2026-10-23T02:00:00Z"));
        order.setUpdatedAt(order.getCreatedAt());
        ShopOrderItem item = new ShopOrderItem();
        item.setId(9L);
        item.setOrder(order);
        item.setProduct(product);
        item.setProductName(product.getName());
        item.setUnitPrice(product.getPrice());
        item.setQuantity(quantity);
        order.setItems(new ArrayList<>(List.of(item)));
        return order;
    }
}
