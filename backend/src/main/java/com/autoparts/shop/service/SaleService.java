package com.autoparts.shop.service;

import com.autoparts.shop.entity.Part;
import com.autoparts.shop.entity.Sale;
import com.autoparts.shop.mapper.PartMapper;
import com.autoparts.shop.mapper.SaleMapper;
import com.autoparts.shop.security.UserContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
public class SaleService {
    private final PartMapper partMapper;
    private final SaleMapper saleMapper;

    public SaleService(PartMapper partMapper, SaleMapper saleMapper) {
        this.partMapper = partMapper;
        this.saleMapper = saleMapper;
    }

    @Transactional
    public Sale createSale(Long partId, Integer quantity, BigDecimal price, String customer, String note) {
        if (quantity == null || quantity <= 0) {
            throw new IllegalArgumentException("销售数量必须大于 0");
        }
        Part part = partMapper.selectById(partId);
        if (part == null) {
            throw new IllegalArgumentException("配件不存在");
        }
        if (part.getStock() < quantity) {
            throw new IllegalArgumentException("库存不足，当前库存：" + part.getStock());
        }
        BigDecimal salePrice = price == null ? part.getPrice() : price;
        part.setStock(part.getStock() - quantity);
        partMapper.updateById(part);

        Sale sale = new Sale();
        sale.setPartId(part.getId());
        sale.setPartName(part.getName());
        sale.setQuantity(quantity);
        sale.setPrice(salePrice);
        sale.setTotalAmount(salePrice.multiply(BigDecimal.valueOf(quantity)));
        sale.setCustomer(customer);
        sale.setNote(note);
        sale.setOperatorId(UserContext.get() == null ? null : UserContext.get().id());
        saleMapper.insert(sale);
        return sale;
    }
}
