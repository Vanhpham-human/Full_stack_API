package com.annam.shop.service;

import com.annam.shop.domain.Category;
import com.annam.shop.domain.Product;
import com.annam.shop.exception.ApiException;
import com.annam.shop.exception.ErrorCode;
import com.annam.shop.exception.FieldIssue;
import com.annam.shop.exception.InputRules;
import com.annam.shop.repo.CartItemRepository;
import com.annam.shop.repo.ProductRepository;
import com.annam.shop.repo.ProductSpecifications;
import com.annam.shop.repo.ShopOrderItemRepository;
import com.annam.shop.service.command.ProductCommand;
import com.annam.shop.service.view.Views.PageView;
import com.annam.shop.service.view.Views.ProductView;
import java.time.Instant;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductService {

    private static final Set<String> SORTS = Set.of("newest", "priceAsc", "priceDesc", "name");

    private final ProductRepository products;
    private final CategoryService categories;
    private final ShopOrderItemRepository orderItems;
    private final CartItemRepository cartItems;

    @Transactional(readOnly = true)
    public PageView<ProductView> search(
            String q,
            Long categoryId,
            Long minPrice,
            Long maxPrice,
            Integer page,
            Integer size,
            String sort
    ) {
        if (q != null && q.trim().length() > 100) {
            throw new ApiException(ErrorCode.ERR_VALIDATION, ErrorCode.ERR_VALIDATION.defaultMessage(),
                    List.of(new FieldIssue("q", "Từ khóa tối đa 100 ký tự")));
        }
        if (minPrice != null && minPrice < 0 || maxPrice != null && maxPrice < 0) {
            throw new ApiException(ErrorCode.ERR_VALIDATION, ErrorCode.ERR_VALIDATION.defaultMessage(),
                    List.of(new FieldIssue("price", "Khoảng giá không được âm")));
        }
        if (minPrice != null && maxPrice != null && minPrice > maxPrice) {
            throw new ApiException(ErrorCode.ERR_VALIDATION, ErrorCode.ERR_VALIDATION.defaultMessage(),
                    List.of(new FieldIssue("price", "Giá tối thiểu đang lớn hơn giá tối đa")));
        }
        String sortKey = sort == null || sort.isBlank() ? "newest" : sort.trim();
        if (!SORTS.contains(sortKey)) {
            throw new ApiException(ErrorCode.ERR_VALIDATION, ErrorCode.ERR_VALIDATION.defaultMessage(),
                    List.of(new FieldIssue("sort", "Chỉ nhận newest, priceAsc, priceDesc, name")));
        }
        PageQuery query = PageQuery.of(page, size, 12);
        Page<Product> result = products.findAll(
                ProductSpecifications.filter(q, categoryId, minPrice, maxPrice),
                PageRequest.of(query.page(), query.size(), toSort(sortKey)));
        return new PageView<>(
                result.getContent().stream().map(ProductService::toView).toList(),
                result.getNumber(),
                result.getSize(),
                result.getTotalElements(),
                result.getTotalPages());
    }

    @Transactional(readOnly = true)
    public ProductView get(Long id) {
        return toView(require(id));
    }

    @Transactional
    public ProductView create(ProductCommand command) {
        Product product = new Product();
        apply(product, command);
        product.setCreatedAt(Instant.now());
        return toView(products.save(product));
    }

    @Transactional
    public ProductView update(Long id, ProductCommand command) {
        Product product = require(id);
        apply(product, command);
        return toView(product);
    }

    @Transactional
    public ProductView updateStock(Long id, int stockQuantity) {
        InputRules.nonNegative(stockQuantity, "stockQuantity");
        Product product = require(id);
        product.setStockQuantity(stockQuantity);
        return toView(product);
    }

    @Transactional
    public void delete(Long id) {
        Product product = require(id);
        if (orderItems.existsByProduct_Id(id)) {
            throw new ApiException(ErrorCode.ERR_PRODUCT_IN_USE);
        }
        cartItems.deleteByProductId(id);
        products.delete(product);
    }

    private void apply(Product product, ProductCommand command) {
        InputRules.positiveMoney(command.price(), "price");
        InputRules.nonNegative(command.stockQuantity(), "stockQuantity");
        Category category = categories.require(command.categoryId());
        product.setName(InputRules.requiredText(command.name(), "name", 2, 200));
        product.setDescription(InputRules.requiredText(command.description(), "description", 2, 4000));
        product.setPrice(command.price());
        product.setStockQuantity(command.stockQuantity());
        product.setCategory(category);
        String image = InputRules.optionalText(command.imageUrl(), "imageUrl", 500);
        if (image == null) {
            String seed = product.getName().toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]+", "-");
            image = "https://picsum.photos/seed/annam-" + seed + "/800/640";
        }
        product.setImageUrl(image);
    }

    private Product require(Long id) {
        return products.findDetailedById(id)
                .orElseThrow(() -> new ApiException(ErrorCode.ERR_NOT_FOUND, "Không tìm thấy sản phẩm"));
    }

    private static Sort toSort(String sort) {
        return switch (sort) {
            case "priceAsc" -> Sort.by(Sort.Order.asc("price"), Sort.Order.asc("id"));
            case "priceDesc" -> Sort.by(Sort.Order.desc("price"), Sort.Order.desc("id"));
            case "name" -> Sort.by(Sort.Order.asc("name"), Sort.Order.asc("id"));
            default -> Sort.by(Sort.Order.desc("createdAt"), Sort.Order.desc("id"));
        };
    }

    public static ProductView toView(Product product) {
        return new ProductView(
                product.getId(),
                product.getName(),
                product.getDescription(),
                product.getPrice(),
                product.getImageUrl(),
                product.getStockQuantity(),
                product.getCategory().getId(),
                product.getCategory().getName(),
                product.getCreatedAt());
    }
}
