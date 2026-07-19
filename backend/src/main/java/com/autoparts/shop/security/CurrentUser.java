package com.autoparts.shop.security;

public record CurrentUser(Long id, String username, String nickname, String role) {
}
