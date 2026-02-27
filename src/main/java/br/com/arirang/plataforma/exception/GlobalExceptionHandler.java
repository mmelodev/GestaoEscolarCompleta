package br.com.arirang.plataforma.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@ControllerAdvice
public class GlobalExceptionHandler {

  private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

  private ResponseEntity<ApiError> buildResponse(HttpStatus status,
                                                 String message,
                                                 HttpServletRequest request,
                                                 Map<String, Object> details,
                                                 Throwable throwable) {
    String path = request != null ? request.getRequestURI() : "N/A";
    String traceId = UUID.randomUUID().toString();

    if (status.is5xxServerError()) {
      logger.error("[{}] {} - {}", traceId, path, message, throwable);
    } else {
      logger.warn("[{}] {} - {}", traceId, path, message);
    }

    ApiError error = ApiError.of(
            status.value(),
            status.getReasonPhrase(),
            message,
            path,
            details,
            traceId
    );

    return ResponseEntity.status(status).body(error);
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ApiError> handleValidationException(MethodArgumentNotValidException ex,
                                                            HttpServletRequest request) {
    Map<String, Object> fieldErrors = new HashMap<>();
    for (FieldError fe : ex.getBindingResult().getFieldErrors()) {
      fieldErrors.put(fe.getField(), fe.getDefaultMessage());
    }
    return buildResponse(HttpStatus.BAD_REQUEST, "Erro de validação", request, fieldErrors, ex);
  }

  @ExceptionHandler(HttpMessageNotReadableException.class)
  public ResponseEntity<ApiError> handleMessageNotReadable(HttpMessageNotReadableException ex,
                                                           HttpServletRequest request) {
    return buildResponse(HttpStatus.BAD_REQUEST, "Corpo da requisição inválido", request, null, ex);
  }

  @ExceptionHandler(ResourceNotFoundException.class)
  public ResponseEntity<ApiError> handleNotFound(ResourceNotFoundException ex,
                                                 HttpServletRequest request) {
    return buildResponse(HttpStatus.NOT_FOUND, ex.getMessage(), request, null, ex);
  }

  @ExceptionHandler(BusinessException.class)
  public ResponseEntity<ApiError> handleBusiness(BusinessException ex,
                                                 HttpServletRequest request) {
    return buildResponse(HttpStatus.BAD_REQUEST, ex.getMessage(), request, ex.getDetails(), ex);
  }

  @ExceptionHandler(FileUploadException.class)
  public ResponseEntity<ApiError> handleFileUpload(FileUploadException ex,
                                                   HttpServletRequest request) {
    return buildResponse(HttpStatus.BAD_REQUEST, ex.getMessage(), request, null, ex);
  }

  @ExceptionHandler(MaxUploadSizeExceededException.class)
  public ResponseEntity<ApiError> handleMaxUpload(MaxUploadSizeExceededException ex,
                                                  HttpServletRequest request) {
    return buildResponse(HttpStatus.PAYLOAD_TOO_LARGE,
            "Arquivo excede o tamanho máximo permitido", request, null, ex);
  }

  @ExceptionHandler(DataIntegrityViolationException.class)
  public ResponseEntity<ApiError> handleDataIntegrity(DataIntegrityViolationException ex,
                                                      HttpServletRequest request) {
    return buildResponse(HttpStatus.CONFLICT,
            "Violação de integridade de dados", request, null, ex);
  }

  @ExceptionHandler(AccessDeniedException.class)
  public ResponseEntity<ApiError> handleAccessDenied(AccessDeniedException ex,
                                                     HttpServletRequest request) {
    return buildResponse(HttpStatus.FORBIDDEN,
            "Acesso negado: " + ex.getMessage(), request, null, ex);
  }

  @ExceptionHandler(IllegalArgumentException.class)
  public ResponseEntity<ApiError> handleIllegalArgumentException(IllegalArgumentException ex,
                                                                 HttpServletRequest request) {
    return buildResponse(HttpStatus.BAD_REQUEST, ex.getMessage(), request, null, ex);
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ApiError> handleGenericException(Exception ex,
                                                         HttpServletRequest request) {
    return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR,
            "Ocorreu um erro inesperado", request, null, ex);
  }
}