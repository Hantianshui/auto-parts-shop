package com.autoparts.shop.controller;

import com.autoparts.shop.common.ApiResult;
import com.autoparts.shop.entity.Part;
import com.autoparts.shop.entity.Sale;
import com.autoparts.shop.mapper.PartMapper;
import com.autoparts.shop.mapper.SaleMapper;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {
    private final PartMapper partMapper;
    private final SaleMapper saleMapper;

    public DashboardController(PartMapper partMapper, SaleMapper saleMapper) {
        this.partMapper = partMapper;
        this.saleMapper = saleMapper;
    }

    @GetMapping
    public ApiResult<Map<String, Object>> dashboard() {
        List<Part> parts = partMapper.selectList(null);
        List<Sale> sales = saleMapper.selectList(null);
        BigDecimal stockValue = parts.stream()
                .map(p -> p.getCost().multiply(BigDecimal.valueOf(p.getStock())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        LocalDate today = LocalDate.now();
        BigDecimal monthSales = sales.stream()
                .filter(s -> s.getCreatedAt().toLocalDate().getMonth() == today.getMonth())
                .map(Sale::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        Map<String, BigDecimal> salesByPart = sales.stream()
                .collect(Collectors.groupingBy(Sale::getPartName, Collectors.reducing(BigDecimal.ZERO, Sale::getTotalAmount, BigDecimal::add)));
        return ApiResult.ok(Map.of(
                "partCount", parts.size(),
                "lowStockCount", parts.stream().filter(p -> p.getStock() <= p.getWarningStock()).count(),
                "stockValue", stockValue,
                "monthSales", monthSales,
                "salesByPart", salesByPart
        ));
    }
}
