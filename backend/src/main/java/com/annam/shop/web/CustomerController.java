package com.annam.shop.web;

import com.annam.shop.generated.api.CustomersApi;
import com.annam.shop.generated.model.Customer;
import com.annam.shop.generated.model.CustomerCreateRequest;
import com.annam.shop.generated.model.CustomerUpdateRequest;
import com.annam.shop.generated.model.OrderPage;
import com.annam.shop.security.CurrentUser;
import com.annam.shop.service.CustomerService;
import com.annam.shop.service.OrderService;
import com.annam.shop.service.command.RegisterCommand;
import com.annam.shop.service.command.UpdateProfileCommand;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequiredArgsConstructor
public class CustomerController implements CustomersApi {

    private final CustomerService customers;
    private final OrderService orders;
    private final ApiMapper mapper;

    @Override
    public ResponseEntity<Customer> createCustomer(CustomerCreateRequest request) {
        var created = customers.register(new RegisterCommand(
                request.fullName(),
                request.email(),
                request.phone(),
                request.password(),
                request.address()));
        return ResponseEntity.status(HttpStatus.CREATED).body(mapper.toCustomer(created));
    }

    @Override
    public ResponseEntity<Customer> getCurrentCustomer() {
        return ResponseEntity.ok(mapper.toCustomer(customers.get(CurrentUser.require().id())));
    }

    @Override
    public ResponseEntity<Customer> updateCurrentCustomer(CustomerUpdateRequest request) {
        var updated = customers.update(CurrentUser.require().id(), new UpdateProfileCommand(
                request.fullName(),
                request.phone(),
                request.address()));
        return ResponseEntity.ok(mapper.toCustomer(updated));
    }

    @Override
    public ResponseEntity<OrderPage> listCustomerOrders(Integer page, Integer size) {
        return ResponseEntity.ok(mapper.toOrderPage(orders.listForCustomer(CurrentUser.require().id(), page, size)));
    }
}
