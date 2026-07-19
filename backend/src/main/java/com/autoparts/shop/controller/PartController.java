package com.autoparts.shop.controller;

import com.autoparts.shop.common.ApiResult;
import com.autoparts.shop.entity.Part;
import com.autoparts.shop.mapper.PartMapper;
import com.autoparts.shop.security.RequireRole;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/parts")
public class PartController {
    private final PartMapper partMapper;

    public PartController(PartMapper partMapper) {
        this.partMapper = partMapper;
    }

    @GetMapping
    public ApiResult<List<Part>> list(@RequestParam(required = false) String keyword,
                                      @RequestParam(required = false) String stockStatus) {
        LambdaQueryWrapper<Part> query = new LambdaQueryWrapper<Part>()
                .like(keyword != null && !keyword.isBlank(), Part::getName, keyword)
                .or(keyword != null && !keyword.isBlank(), q -> q.like(Part::getModels, keyword).or().like(Part::getCode, keyword))
                .orderByDesc(Part::getUpdatedAt);
        List<Part> parts = partMapper.selectList(query);
        if ("low".equals(stockStatus)) {
            parts = parts.stream().filter(p -> p.getStock() > 0 && p.getStock() <= p.getWarningStock()).toList();
        } else if ("empty".equals(stockStatus)) {
            parts = parts.stream().filter(p -> p.getStock() <= 0).toList();
        }
        return ApiResult.ok(parts);
    }

    @PostMapping
    @RequireRole({"ADMIN", "MANAGER"})
    public ApiResult<Part> save(@RequestBody Part part) {
        if (part.getId() == null) {
            partMapper.insert(part);
        } else {
            partMapper.updateById(part);
        }
        return ApiResult.ok(part);
    }

    @DeleteMapping("/{id}")
    @RequireRole("ADMIN")
    public ApiResult<Void> delete(@PathVariable Long id) {
        partMapper.deleteById(id);
        return ApiResult.ok();
    }
}
