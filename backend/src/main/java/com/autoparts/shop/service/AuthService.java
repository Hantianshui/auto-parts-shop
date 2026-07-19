package com.autoparts.shop.service;

import com.autoparts.shop.entity.User;
import com.autoparts.shop.mapper.UserMapper;
import com.autoparts.shop.security.JwtService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class AuthService {
    private final UserMapper userMapper;
    private final JwtService jwtService;
    private final BCryptPasswordEncoder passwordEncoder;

    public AuthService(UserMapper userMapper, JwtService jwtService, BCryptPasswordEncoder passwordEncoder) {
        this.userMapper = userMapper;
        this.jwtService = jwtService;
        this.passwordEncoder = passwordEncoder;
    }

    public Map<String, Object> login(String username, String password) {
        User user = userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getUsername, username));
        if (user == null || !Boolean.TRUE.equals(user.getEnabled()) || !passwordEncoder.matches(password, user.getPasswordHash())) {
            throw new IllegalArgumentException("用户名或密码错误");
        }
        return Map.of(
                "token", jwtService.createToken(user),
                "user", Map.of("id", user.getId(), "username", user.getUsername(), "nickname", user.getNickname(), "role", user.getRole())
        );
    }
}
