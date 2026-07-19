package com.autoparts.shop.common;

public record ApiResult<T>(int code, String message, T data) {
    public static <T> ApiResult<T> ok(T data) {
        return new ApiResult<>(200, "OK", data);
    }

    public static ApiResult<Void> ok() {
        return new ApiResult<>(200, "OK", null);
    }

    public static ApiResult<Void> fail(int code, String message) {
        return new ApiResult<>(code, message, null);
    }
}
