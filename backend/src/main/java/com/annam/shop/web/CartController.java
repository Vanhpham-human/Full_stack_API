package com.annam.shop.web;

import com.annam.shop.generated.api.CartsApi;
import com.annam.shop.generated.model.Cart;
import com.annam.shop.generated.model.CartItemRequest;
import com.annam.shop.generated.model.CartItemUpdate;
import com.annam.shop.security.CurrentUser;
import com.annam.shop.service.CartService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequiredArgsConstructor
public class CartController implements CartsApi {

    private final CartService carts;
    private final ApiMapper mapper;

    @Override
    public ResponseEntity<Cart> getMyCart() {
        return ResponseEntity.ok(mapper.toCart(carts.get(CurrentUser.require().id())));
    }

    @Override
    public ResponseEntity<Cart> addCartItem(CartItemRequest request) {
        var cart = carts.addItem(CurrentUser.require().id(), request.productId(), request.quantity());
        return ResponseEntity.status(HttpStatus.CREATED).body(mapper.toCart(cart));
    }

    @Override
    public ResponseEntity<Cart> updateCartItem(Long itemId, CartItemUpdate request) {
        var cart = carts.updateItem(CurrentUser.require().id(), itemId, request.quantity());
        return ResponseEntity.ok(mapper.toCart(cart));
    }

    @Override
    public ResponseEntity<Void> removeCartItem(Long itemId) {
        carts.removeItem(CurrentUser.require().id(), itemId);
        return ResponseEntity.noContent().build();
    }
}
