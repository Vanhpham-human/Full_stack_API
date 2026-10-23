package com.annam.shop.service;

import com.annam.shop.security.JwtService;
import com.annam.shop.service.view.Views.CustomerView;
import com.annam.shop.service.view.Views.SessionView;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final CustomerService customers;
    private final JwtService jwtService;

    public SessionView login(String email, String password) {
        CustomerView customer = customers.authenticate(email, password);
        return new SessionView(jwtService.create(customer), "Bearer", jwtService.expiresInSeconds(), customer);
    }
}
