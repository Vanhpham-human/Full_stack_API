package com.annam.shop.web;

import com.annam.shop.generated.api.ProductsApi;
import com.annam.shop.generated.model.Product;
import com.annam.shop.generated.model.ProductPage;
import com.annam.shop.generated.model.ProductRequest;
import com.annam.shop.generated.model.ProductStockUpdate;
import com.annam.shop.service.ProductService;
import com.annam.shop.service.command.ProductCommand;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequiredArgsConstructor
public class ProductController implements ProductsApi {

    private final ProductService products;
    private final ApiMapper mapper;

    @Override
    public ResponseEntity<ProductPage> listProducts(
            String q,
            Long categoryId,
            Long minPrice,
            Long maxPrice,
            Integer page,
            Integer size,
            String sort
    ) {
        return ResponseEntity.ok(mapper.toProductPage(products.search(q, categoryId, minPrice, maxPrice, page, size, sort)));
    }

    @Override
    public ResponseEntity<Product> createProduct(ProductRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(mapper.toProduct(products.create(command(request))));
    }

    @Override
    public ResponseEntity<Product> getProduct(Long id) {
        return ResponseEntity.ok(mapper.toProduct(products.get(id)));
    }

    @Override
    public ResponseEntity<Product> updateProduct(Long id, ProductRequest request) {
        return ResponseEntity.ok(mapper.toProduct(products.update(id, command(request))));
    }

    @Override
    public ResponseEntity<Product> updateProductStock(Long id, ProductStockUpdate request) {
        return ResponseEntity.ok(mapper.toProduct(products.updateStock(id, request.stockQuantity())));
    }

    @Override
    public ResponseEntity<Void> deleteProduct(Long id) {
        products.delete(id);
        return ResponseEntity.noContent().build();
    }

    private static ProductCommand command(ProductRequest request) {
        return new ProductCommand(
                request.name(),
                request.description(),
                request.price(),
                request.imageUrl(),
                request.stockQuantity(),
                request.categoryId());
    }
}
