package com.autoparts.shop.controller;

import com.autoparts.shop.common.ApiResult;
import com.autoparts.shop.entity.Category;
import com.autoparts.shop.entity.Supplier;
import com.autoparts.shop.mapper.CategoryMapper;
import com.autoparts.shop.mapper.SupplierMapper;
import com.autoparts.shop.security.RequireRole;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class SimpleDataController {
    private final CategoryMapper categoryMapper;
    private final SupplierMapper supplierMapper;

    public SimpleDataController(CategoryMapper categoryMapper, SupplierMapper supplierMapper) {
        this.categoryMapper = categoryMapper;
        this.supplierMapper = supplierMapper;
    }

    @GetMapping("/categories")
    public ApiResult<List<Category>> categories() {
        return ApiResult.ok(categoryMapper.selectList(null));
    }

    @PostMapping("/categories")
    @RequireRole({"ADMIN", "MANAGER"})
    public ApiResult<Category> saveCategory(@RequestBody Category category) {
        if (category.getId() == null) categoryMapper.insert(category);
        else categoryMapper.updateById(category);
        return ApiResult.ok(category);
    }

    @GetMapping("/suppliers")
    public ApiResult<List<Supplier>> suppliers() {
        return ApiResult.ok(supplierMapper.selectList(null));
    }

    @PostMapping("/suppliers")
    @RequireRole({"ADMIN", "MANAGER"})
    public ApiResult<Supplier> saveSupplier(@RequestBody Supplier supplier) {
        if (supplier.getId() == null) supplierMapper.insert(supplier);
        else supplierMapper.updateById(supplier);
        return ApiResult.ok(supplier);
    }
}
