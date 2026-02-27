package br.com.arirang.plataforma.exception;

import java.util.Map;

public class BusinessException extends RuntimeException {
    private final Map<String, Object> details;

    public BusinessException(String message) {
        super(message);
        this.details = null;
    }

    public BusinessException(String message, Map<String, Object> details) {
        super(message);
        this.details = details;
    }

    public Map<String, Object> getDetails() {
        return details;
    }
}


