package com.annam.shop.service;

import com.annam.shop.domain.Cart;
import com.annam.shop.domain.CartItem;
import com.annam.shop.domain.Customer;
import com.annam.shop.domain.Product;
import com.annam.shop.exception.ApiException;
import com.annam.shop.exception.ErrorCode;
import com.annam.shop.exception.InputRules;
import com.annam.shop.repo.CartRepository;
import com.annam.shop.repo.CustomerRepository;
import com.annam.shop.repo.ProductRepository;
import com.annam.shop.service.view.Views.CartItemView;
import com.annam.shop.service.view.Views.CartView;
import java.util.Comparator;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CartService {

    private final CartRepository carts;
    private final CustomerRepository customers;
    private final ProductRepository products;

    @Transactional(readOnly = true)
    public CartView get(Long customerId) {
        return carts.findDetailedByCustomerId(customerId)
                .map(CartService::toView)
                .orElseGet(() -> new CartView(null, customerId, List.of(), 0));
    }

    @Transactional
    public CartView addItem(Long customerId, Long productId, int quantity) {
        InputRules.quantity(quantity);
        Cart cart = loadOrCreate(customerId);
        Product product = products.findById(productId)
                .orElseThrow(() -> new ApiException(ErrorCode.ERR_NOT_FOUND, "Không tìm thấy sản phẩm"));
        CartItem item = cart.getItems().stream()
                .filter(existing -> existing.getProduct().getId().equals(productId))
                .findFirst()
                .orElse(null);
        int nextQuantity = item == null ? quantity : item.getQuantity() + quantity;
        InputRules.quantity(nextQuantity);
        if (item == null) {
            item = new CartItem();
            item.setCart(cart);
            item.setProduct(product);
            item.setQuantity(quantity);
            cart.getItems().add(item);
        } else {
            item.setQuantity(nextQuantity);
        }
        carts.save(cart);
        return toView(reload(customerId));
    }

    @Transactional
    public CartView updateItem(Long customerId, Long itemId, int quantity) {
        InputRules.quantity(quantity);
        Cart cart = requireCart(customerId);
        CartItem item = findItem(cart, itemId);
        item.setQuantity(quantity);
        return toView(reload(customerId));
    }

    @Transactional
    public void removeItem(Long customerId, Long itemId) {
        Cart cart = requireCart(customerId);
        CartItem item = findItem(cart, itemId);
        cart.getItems().remove(item);
        carts.save(cart);
    }

    private Cart loadOrCreate(Long customerId) {
        return carts.findDetailedByCustomerId(customerId).orElseGet(() -> {
            Customer customer = customers.findById(customerId)
                    .orElseThrow(() -> new ApiException(ErrorCode.ERR_NOT_FOUND, "Không tìm thấy khách hàng"));
            Cart cart = new Cart();
            cart.setCustomer(customer);
            return carts.save(cart);
        });
    }

    private Cart requireCart(Long customerId) {
        return carts.findDetailedByCustomerId(customerId)
                .orElseThrow(() -> new ApiException(ErrorCode.ERR_NOT_FOUND, "Không tìm thấy giỏ hàng"));
    }

    private Cart reload(Long customerId) {
        return carts.findDetailedByCustomerId(customerId).orElseThrow();
    }

    private static CartItem findItem(Cart cart, Long itemId) {
        return cart.getItems().stream()
                .filter(item -> item.getId().equals(itemId))
                .findFirst()
                .orElseThrow(() -> new ApiException(ErrorCode.ERR_NOT_FOUND, "Không tìm thấy dòng trong giỏ"));
    }

    static CartView toView(Cart cart) {
        var items = cart.getItems().stream()
                .sorted(Comparator.comparing(CartItem::getId, Comparator.nullsLast(Long::compareTo)))
                .map(item -> {
                    Product product = item.getProduct();
                    long line = product.getPrice() * item.getQuantity();
                    return new CartItemView(
                            item.getId(),
                            product.getId(),
                            product.getName(),
                            product.getImageUrl(),
                            product.getPrice(),
                            item.getQuantity(),
                            line,
                            product.getStockQuantity());
                })
                .toList();
        long total = items.stream().mapToLong(CartItemView::lineTotal).sum();
        return new CartView(cart.getId(), cart.getCustomer().getId(), items, total);
    }
}
