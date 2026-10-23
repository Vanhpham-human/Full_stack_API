package com.annam.shop.web;

import com.annam.shop.generated.api.SessionsApi;
import com.annam.shop.generated.model.Session;
import com.annam.shop.generated.model.SessionCreateRequest;
import com.annam.shop.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequiredArgsConstructor
public class SessionController implements SessionsApi {

    private final AuthService auth;
    private final ApiMapper mapper;

    @Override
    public ResponseEntity<Session> createSession(SessionCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(mapper.toSession(auth.login(request.email(), request.password())));
    }

    @Override
    public ResponseEntity<Void> deleteCurrentSession() {
        return ResponseEntity.noContent().build();
    }
}
