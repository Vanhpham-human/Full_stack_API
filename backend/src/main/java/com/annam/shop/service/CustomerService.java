package com.annam.shop.service;

import com.annam.shop.domain.Cart;
import com.annam.shop.domain.Customer;
import com.annam.shop.domain.Role;
import com.annam.shop.exception.ApiException;
import com.annam.shop.exception.ErrorCode;
import com.annam.shop.exception.InputRules;
import com.annam.shop.repo.CartRepository;
import com.annam.shop.repo.CustomerRepository;
import com.annam.shop.service.command.RegisterCommand;
import com.annam.shop.service.command.UpdateProfileCommand;
import com.annam.shop.service.view.Views.CustomerView;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CustomerService {

    private final CustomerRepository customers;
    private final CartRepository carts;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public CustomerView register(RegisterCommand command) {
        String email = InputRules.email(command.email());
        String phone = InputRules.phone(command.phone());
        String password = InputRules.password(command.password());
        String fullName = InputRules.requiredText(command.fullName(), "fullName", 2, 120);
        String address = InputRules.optionalText(command.address(), "address", 500);
        if (customers.existsByEmailIgnoreCase(email)) {
            throw new ApiException(ErrorCode.ERR_EMAIL_EXISTS);
        }
        if (customers.existsByPhone(phone)) {
            throw new ApiException(ErrorCode.ERR_PHONE_EXISTS);
        }
        Customer customer = new Customer();
        customer.setFullName(fullName);
        customer.setEmail(email);
        customer.setPhone(phone);
        customer.setPasswordHash(passwordEncoder.encode(password));
        customer.setAddress(address);
        customer.setRole(Role.CUSTOMER);
        customer.setCreatedAt(Instant.now());
        Customer saved = customers.save(customer);
        Cart cart = new Cart();
        cart.setCustomer(saved);
        carts.save(cart);
        return toView(saved);
    }

    @Transactional(readOnly = true)
    public CustomerView authenticate(String email, String password) {
        String normalized = email == null ? "" : email.trim().toLowerCase();
        Customer customer = customers.findByEmailIgnoreCase(normalized)
                .orElseThrow(() -> new ApiException(ErrorCode.ERR_INVALID_CREDENTIALS));
        if (password == null || !passwordEncoder.matches(password, customer.getPasswordHash())) {
            throw new ApiException(ErrorCode.ERR_INVALID_CREDENTIALS);
        }
        return toView(customer);
    }

    @Transactional(readOnly = true)
    public Customer requireEntity(Long id) {
        return customers.findById(id).orElseThrow(() -> new ApiException(ErrorCode.ERR_NOT_FOUND, "Không tìm thấy khách hàng"));
    }

    @Transactional(readOnly = true)
    public CustomerView get(Long id) {
        return toView(requireEntity(id));
    }

    @Transactional
    public CustomerView update(Long id, UpdateProfileCommand command) {
        Customer customer = requireEntity(id);
        String fullName = InputRules.requiredText(command.fullName(), "fullName", 2, 120);
        String phone = InputRules.phone(command.phone());
        String address = InputRules.optionalText(command.address(), "address", 500);
        if (customers.existsByPhoneAndIdNot(phone, id)) {
            throw new ApiException(ErrorCode.ERR_PHONE_EXISTS);
        }
        customer.setFullName(fullName);
        customer.setPhone(phone);
        customer.setAddress(address);
        return toView(customer);
    }

    public static CustomerView toView(Customer customer) {
        return new CustomerView(
                customer.getId(),
                customer.getFullName(),
                customer.getEmail(),
                customer.getPhone(),
                customer.getAddress(),
                customer.getRole(),
                customer.getCreatedAt());
    }
}
