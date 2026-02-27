package br.com.arirang.plataforma.exception;

/**
 * Exceção lançada quando há erro no upload de arquivos
 */
public class FileUploadException extends RuntimeException {
    
    public FileUploadException(String message) {
        super(message);
    }
    
    public FileUploadException(String message, Throwable cause) {
        super(message, cause);
    }
}

