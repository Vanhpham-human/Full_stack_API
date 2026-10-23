package com.annam.shop.service;

import com.annam.shop.domain.Category;
import com.annam.shop.exception.ApiException;
import com.annam.shop.exception.ErrorCode;
import com.annam.shop.exception.InputRules;
import com.annam.shop.repo.CategoryRepository;
import com.annam.shop.repo.ProductRepository;
import com.annam.shop.service.command.CategoryCommand;
import com.annam.shop.service.view.Views.CategoryView;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CategoryService {

    private final CategoryRepository categories;
    private final ProductRepository products;

    @Transactional(readOnly = true)
    public List<CategoryView> list() {
        return categories.findAllByOrderByNameAsc().stream().map(CategoryService::toView).toList();
    }

    @Transactional(readOnly = true)
    public CategoryView get(Long id) {
        return toView(require(id));
    }

    @Transactional
    public CategoryView create(CategoryCommand command) {
        String name = normalizeName(command.name());
        if (categories.existsByNameIgnoreCase(name)) {
            throw new ApiException(ErrorCode.ERR_CATEGORY_EXISTS);
        }
        Category category = new Category();
        category.setName(name);
        category.setDescription(InputRules.optionalText(command.description(), "description", 500));
        return toView(categories.save(category));
    }

    @Transactional
    public CategoryView update(Long id, CategoryCommand command) {
        Category category = require(id);
        String name = normalizeName(command.name());
        if (categories.existsByNameIgnoreCaseAndIdNot(name, id)) {
            throw new ApiException(ErrorCode.ERR_CATEGORY_EXISTS);
        }
        category.setName(name);
        category.setDescription(InputRules.optionalText(command.description(), "description", 500));
        return toView(category);
    }

    @Transactional
    public void delete(Long id) {
        Category category = require(id);
        if (products.existsByCategoryId(id)) {
            throw new ApiException(ErrorCode.ERR_CATEGORY_IN_USE);
        }
        categories.delete(category);
    }

    public Category require(Long id) {
        return categories.findById(id)
                .orElseThrow(() -> new ApiException(ErrorCode.ERR_NOT_FOUND, "Không tìm thấy danh mục"));
    }

    private static String normalizeName(String name) {
        return InputRules.requiredText(name, "name", 2, 120);
    }

    public static CategoryView toView(Category category) {
        return new CategoryView(category.getId(), category.getName(), category.getDescription());
    }
}
