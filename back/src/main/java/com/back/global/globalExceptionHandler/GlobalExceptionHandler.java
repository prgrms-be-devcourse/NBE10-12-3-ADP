package com.back.global.globalExceptionHandler;

import com.back.global.exception.ServiceException;
import com.back.global.rsData.RsData;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.ConstraintViolationException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.Comparator;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

import static org.springframework.http.HttpStatus.*;

@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {

    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<RsData<Void>> handle(NoSuchElementException ex) {
        String message = ex.getMessage();
        if (message == null) message = "해당 데이터가 존재하지 않습니다.";

        return new ResponseEntity<>(new RsData<>("404-1", message), NOT_FOUND);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<RsData<Void>> handle(ConstraintViolationException ex) {
        String message = ex.getConstraintViolations()
                .stream()
                .map(violation -> {
                    String field = violation.getPropertyPath().toString().split("\\.", 2)[1];
                    String[] messageTemplateBits = violation.getMessageTemplate().split("\\.");
                    String code = messageTemplateBits[messageTemplateBits.length - 2];
                    String _message = violation.getMessage();
                    return "%s-%s-%s".formatted(field, code, _message);
                })
                .sorted(Comparator.comparing(String::toString))
                .collect(Collectors.joining("\n"));

        return new ResponseEntity<>(new RsData<>("400-1", message), BAD_REQUEST);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<RsData<Void>> handle(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult()
                .getAllErrors()
                .stream()
                .filter(error -> error instanceof FieldError)
                .map(error -> (FieldError) error)
                .map(error -> error.getField() + "-" + error.getCode() + "-" + error.getDefaultMessage())
                .sorted()
                .collect(Collectors.joining("\n"));

        return new ResponseEntity<>(new RsData<>("400-1", message), BAD_REQUEST);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<RsData<Void>> handle(HttpMessageNotReadableException ex) {
        return new ResponseEntity<>(
                new RsData<>("400-2", "요청 본문이 올바르지 않습니다."),
                BAD_REQUEST
        );
    }

    @ExceptionHandler(MissingRequestHeaderException.class)
    public ResponseEntity<RsData<Void>> handle(MissingRequestHeaderException ex) {
        return new ResponseEntity<>(
                new RsData<>("400-2", "헤더에 %s이 필요합니다.".formatted(ex.getHeaderName())),
                BAD_REQUEST
        );
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<RsData<Void>> handle(NoResourceFoundException ex) {
        return new ResponseEntity<>(
                new RsData<>("404-1", "리소스를 찾을 수 없습니다."),
                NOT_FOUND
        );
    }

    @ExceptionHandler(ServiceException.class)
    public RsData<Void> handle(ServiceException ex, HttpServletResponse response) {
        RsData<Void> rsData = ex.getRsData();
        response.setStatus(rsData.statusCode());
        return rsData;
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<RsData<Void>> handle(Exception ex) {
        return new ResponseEntity<>(
                new RsData<>("500-1", "일시적인 장애가 발생했습니다."),
                INTERNAL_SERVER_ERROR
        );
    }
}