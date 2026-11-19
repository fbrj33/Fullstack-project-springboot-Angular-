package com.sip.aurahub.dto;

import java.time.LocalDateTime;
import java.util.Map;

public record ApiError(LocalDateTime timestamp, int status, String message, Map<String, String> details) {
    public static ApiError of(int status, String message) {
        return new ApiError(LocalDateTime.now(), status, message, null);
    }
}
