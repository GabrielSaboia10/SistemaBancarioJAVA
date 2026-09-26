package com.banco.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.core.PropertyReferenceException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> validacao(MethodArgumentNotValidException ex, HttpServletRequest req) {
        Map<String, String> campos = new LinkedHashMap<>();
        ex.getBindingResult().getFieldErrors()
                .forEach(e -> campos.putIfAbsent(e.getField(), e.getDefaultMessage()));
        ApiError body = new ApiError(Instant.now(), 400, "Bad Request",
                "Dados inválidos", req.getRequestURI(), campos);
        return ResponseEntity.badRequest().body(body);
    }

    @ExceptionHandler({HttpMessageNotReadableException.class, MethodArgumentTypeMismatchException.class,
            PropertyReferenceException.class})
    public ResponseEntity<ApiError> requisicaoMalFormada(Exception ex, HttpServletRequest req) {
        return responder(HttpStatus.BAD_REQUEST, "Requisição mal formada", req);
    }

    @ExceptionHandler(RegraNegocioException.class)
    public ResponseEntity<ApiError> regraNegocio(RegraNegocioException ex, HttpServletRequest req) {
        return responder(HttpStatus.UNPROCESSABLE_CONTENT, ex.getMessage(), req);
    }

    @ExceptionHandler(RecursoNaoEncontradoException.class)
    public ResponseEntity<ApiError> naoEncontrado(RecursoNaoEncontradoException ex, HttpServletRequest req) {
        return responder(HttpStatus.NOT_FOUND, ex.getMessage(), req);
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ApiError> rotaInexistente(NoResourceFoundException ex, HttpServletRequest req) {
        return responder(HttpStatus.NOT_FOUND, "Rota não encontrada", req);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiError> metodoNaoSuportado(HttpRequestMethodNotSupportedException ex, HttpServletRequest req) {
        return responder(HttpStatus.METHOD_NOT_ALLOWED, "Método não suportado nesta rota", req);
    }

    @ExceptionHandler(ConflitoException.class)
    public ResponseEntity<ApiError> conflito(ConflitoException ex, HttpServletRequest req) {
        return responder(HttpStatus.CONFLICT, ex.getMessage(), req);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiError> integridade(DataIntegrityViolationException ex, HttpServletRequest req) {
        log.warn("Violação de integridade em {}: {}", req.getRequestURI(), ex.getMostSpecificCause().getMessage());
        return responder(HttpStatus.CONFLICT, "Operação conflita com dados existentes", req);
    }

    @ExceptionHandler(CredenciaisInvalidasException.class)
    public ResponseEntity<ApiError> credenciais(CredenciaisInvalidasException ex, HttpServletRequest req) {
        return responder(HttpStatus.UNAUTHORIZED, ex.getMessage(), req);
    }

    @ExceptionHandler(AcessoBloqueadoException.class)
    public ResponseEntity<ApiError> acessoBloqueado(AcessoBloqueadoException ex, HttpServletRequest req) {
        return responder(HttpStatus.FORBIDDEN, ex.getMessage(), req);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiError> acessoNegado(AccessDeniedException ex, HttpServletRequest req) {
        return responder(HttpStatus.FORBIDDEN, "Acesso negado", req);
    }

    @ExceptionHandler(MuitasTentativasException.class)
    public ResponseEntity<ApiError> muitasTentativas(MuitasTentativasException ex, HttpServletRequest req) {
        return responder(HttpStatus.TOO_MANY_REQUESTS, ex.getMessage(), req);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> inesperado(Exception ex, HttpServletRequest req) {
        log.error("Erro inesperado em {}", req.getRequestURI(), ex);
        return responder(HttpStatus.INTERNAL_SERVER_ERROR, "Erro interno do servidor", req);
    }

    private static ResponseEntity<ApiError> responder(HttpStatus status, String mensagem, HttpServletRequest req) {
        return ResponseEntity.status(status)
                .body(ApiError.of(status.value(), status.getReasonPhrase(), mensagem, req.getRequestURI()));
    }
}
