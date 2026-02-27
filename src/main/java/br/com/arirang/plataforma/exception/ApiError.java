package br.com.arirang.plataforma.exception;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Representa uma resposta estruturada de erro para APIs REST.
 */
public record ApiError(
        LocalDateTime timestamp,
        int status,
        String error,
        String message,
        String path,
        Map<String, Object> details,
        String traceId) {

    public static ApiError of(int status,
                              String error,
                              String message,
                              String path,
                              Map<String, Object> details,
                              String traceId) {
        Map<String, Object> safeDetails = (details == null || details.isEmpty()) ? null : Map.copyOf(details);
        return new ApiError(
                LocalDateTime.now(),
                status,
                error,
                message,
                path,
                safeDetails,
                traceId
        );
    }
}


