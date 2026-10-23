package com.annam.shop.web;

import com.annam.shop.generated.api.CategoriesApi;
import com.annam.shop.generated.model.Category;
import com.annam.shop.generated.model.CategoryRequest;
import com.annam.shop.service.CategoryService;
import com.annam.shop.service.command.CategoryCommand;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequiredArgsConstructor
public class CategoryController implements CategoriesApi {

    private final CategoryService categories;
    private final ApiMapper mapper;

    @Override
    public ResponseEntity<List<Category>> listCategories() {
        return ResponseEntity.ok(categories.list().stream().map(mapper::toCategory).toList());
    }

    @Override
    public ResponseEntity<Category> createCategory(CategoryRequest request) {
        var created = categories.create(new CategoryCommand(request.name(), request.description()));
        return ResponseEntity.status(HttpStatus.CREATED).body(mapper.toCategory(created));
    }

    @Override
    public ResponseEntity<Category> getCategory(Long id) {
        return ResponseEntity.ok(mapper.toCategory(categories.get(id)));
    }

    @Override
    public ResponseEntity<Category> updateCategory(Long id, CategoryRequest request) {
        var updated = categories.update(id, new CategoryCommand(request.name(), request.description()));
        return ResponseEntity.ok(mapper.toCategory(updated));
    }

    @Override
    public ResponseEntity<Void> deleteCategory(Long id) {
        categories.delete(id);
        return ResponseEntity.noContent().build();
    }
}
