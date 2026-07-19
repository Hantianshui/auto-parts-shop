package com.autoparts.shop.controller;

import com.autoparts.shop.common.ApiResult;
import com.autoparts.shop.entity.Sale;
import com.autoparts.shop.mapper.SaleMapper;
import com.autoparts.shop.service.SaleService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/sales")
public class SaleController {
    private final SaleService saleService;
    private final SaleMapper saleMapper;

    public SaleController(SaleService saleService, SaleMapper saleMapper) {
        this.saleService = saleService;
        this.saleMapper = saleMapper;
    }

    @GetMapping
    public ApiResult<List<Sale>> list() {
        return ApiResult.ok(saleMapper.selectList(new LambdaQueryWrapper<Sale>().orderByDesc(Sale::getCreatedAt)));
    }

    @PostMapping
    public ApiResult<Sale> create(@RequestBody SaleRequest request) {
        return ApiResult.ok(saleService.createSale(request.partId(), request.quantity(), request.price(), request.customer(), request.note()));
    }

    public record SaleRequest(Long partId, Integer quantity, BigDecimal price, String customer, String note) {
    }
}
