package com.autoparts.shop.config;

import com.autoparts.shop.entity.Category;
import com.autoparts.shop.entity.Part;
import com.autoparts.shop.entity.Supplier;
import com.autoparts.shop.entity.User;
import com.autoparts.shop.mapper.CategoryMapper;
import com.autoparts.shop.mapper.PartMapper;
import com.autoparts.shop.mapper.SupplierMapper;
import com.autoparts.shop.mapper.UserMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class DataInitializer implements CommandLineRunner {
    private final UserMapper userMapper;
    private final CategoryMapper categoryMapper;
    private final SupplierMapper supplierMapper;
    private final PartMapper partMapper;
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    public DataInitializer(UserMapper userMapper, CategoryMapper categoryMapper, SupplierMapper supplierMapper, PartMapper partMapper) {
        this.userMapper = userMapper;
        this.categoryMapper = categoryMapper;
        this.supplierMapper = supplierMapper;
        this.partMapper = partMapper;
    }

    @Override
    public void run(String... args) {
        createUser("admin", "123456", "管理员", "ADMIN");
        createUser("manager", "123456", "店长", "MANAGER");
        createUser("staff", "123456", "销售员", "STAFF");
        if (categoryMapper.selectCount(null) == 0) {
            createCategory("保养件", 1);
            createCategory("制动系统", 2);
        }
        if (supplierMapper.selectCount(null) == 0) {
            Supplier supplier = new Supplier();
            supplier.setName("老王汽配");
            supplier.setPhone("13800000000");
            supplier.setAddress("城南汽配城 A 区");
            supplier.setNote("常用件当天发货");
            supplierMapper.insert(supplier);
        }
        if (partMapper.selectCount(null) == 0) {
            Part part = new Part();
            part.setName("机油滤芯");
            part.setCode("OIL-001");
            part.setCategoryId(1L);
            part.setSupplierId(1L);
            part.setModels("大众、丰田常用");
            part.setStock(18);
            part.setWarningStock(5);
            part.setCost(new BigDecimal("18.00"));
            part.setPrice(new BigDecimal("35.00"));
            partMapper.insert(part);
        }
    }

    private void createUser(String username, String password, String nickname, String role) {
        Long count = userMapper.selectCount(new LambdaQueryWrapper<User>().eq(User::getUsername, username));
        if (count > 0) return;
        User user = new User();
        user.setUsername(username);
        user.setPasswordHash(encoder.encode(password));
        user.setNickname(nickname);
        user.setRole(role);
        user.setEnabled(true);
        userMapper.insert(user);
    }

    private void createCategory(String name, int sortOrder) {
        Category category = new Category();
        category.setName(name);
        category.setSortOrder(sortOrder);
        categoryMapper.insert(category);
    }
}
