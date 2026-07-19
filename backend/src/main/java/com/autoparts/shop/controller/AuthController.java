package com.autoparts.shop.controller;

import com.autoparts.shop.common.ApiResult;
import com.autoparts.shop.security.CurrentUser;
import com.autoparts.shop.security.UserContext;
import com.autoparts.shop.service.AuthService;
import jakarta.validation.constraints.NotBlank;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ApiResult<Map<String, Object>> login(@RequestBody LoginRequest request) {
        return ApiResult.ok(authService.login(request.username(), request.password()));
    }

    @GetMapping("/me")
    public ApiResult<CurrentUser> me() {
        return ApiResult.ok(UserContext.get());
    }

    public record LoginRequest(@NotBlank String username, @NotBlank String password) {
    }
}
