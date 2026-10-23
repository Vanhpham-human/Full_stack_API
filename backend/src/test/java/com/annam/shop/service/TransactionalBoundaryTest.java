package com.annam.shop.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.annam.shop.domain.OrderStatus;
import com.annam.shop.security.AuthUser;
import com.annam.shop.service.command.RegisterCommand;
import java.lang.reflect.Method;
import org.junit.jupiter.api.Test;
import org.springframework.transaction.annotation.Transactional;

class TransactionalBoundaryTest {

    @Test
    void readServicesAreReadOnlyByDefault() {
        assertReadOnly(ProductService.class);
        assertReadOnly(CategoryService.class);
        assertReadOnly(CustomerService.class);
        assertReadOnly(CartService.class);
        assertReadOnly(OrderService.class);
    }

    @Test
    void readMethodsKeepReadOnlyTransactions() throws Exception {
        assertMethodReadOnly(ProductService.class, "search", String.class, Long.class, Long.class, Long.class, Integer.class, Integer.class, String.class);
        assertMethodReadOnly(CustomerService.class, "authenticate", String.class, String.class);
        assertMethodReadOnly(CartService.class, "get", Long.class);
        assertMethodReadOnly(OrderService.class, "get", Long.class, AuthUser.class);
    }

    @Test
    void checkoutAndWritesAreNotReadOnly() throws Exception {
        Transactional checkout = OrderService.class
                .getMethod("checkout", Long.class, String.class, String.class)
                .getAnnotation(Transactional.class);
        assertThat(checkout).isNotNull();
        assertThat(checkout.readOnly()).isFalse();

        Transactional status = OrderService.class
                .getMethod("updateStatus", Long.class, OrderStatus.class, AuthUser.class)
                .getAnnotation(Transactional.class);
        assertThat(status.readOnly()).isFalse();

        Transactional addItem = CartService.class
                .getMethod("addItem", Long.class, Long.class, int.class)
                .getAnnotation(Transactional.class);
        assertThat(addItem.readOnly()).isFalse();

        Transactional register = CustomerService.class
                .getMethod("register", RegisterCommand.class)
                .getAnnotation(Transactional.class);
        assertThat(register.readOnly()).isFalse();
    }

    private static void assertReadOnly(Class<?> type) {
        Transactional transactional = type.getAnnotation(Transactional.class);
        assertThat(transactional).as(type.getSimpleName()).isNotNull();
        assertThat(transactional.readOnly()).as(type.getSimpleName()).isTrue();
    }

    private static void assertMethodReadOnly(Class<?> type, String name, Class<?>... params) throws Exception {
        Method method = type.getMethod(name, params);
        Transactional transactional = method.getAnnotation(Transactional.class);
        assertThat(transactional).as(name).isNotNull();
        assertThat(transactional.readOnly()).as(name).isTrue();
    }
}
