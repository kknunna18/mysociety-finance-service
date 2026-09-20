package com.mysociety.finance.api;

import org.springframework.http.*; import org.springframework.web.bind.MethodArgumentNotValidException; import org.springframework.web.bind.annotation.*;
import java.net.URI;
@RestControllerAdvice
public class ApiExceptionHandler {
    @ExceptionHandler(MethodArgumentNotValidException.class)
    ProblemDetail validation(MethodArgumentNotValidException ex) {
        ProblemDetail problem=ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST,"Request validation failed");
        problem.setType(URI.create("urn:mysociety:finance:validation-error"));
        problem.setProperty("errors",ex.getBindingResult().getFieldErrors().stream().map(e -> e.getField()+": "+e.getDefaultMessage()).toList());
        return problem;
    }
    @ExceptionHandler(IllegalStateException.class)
    ProblemDetail conflict(IllegalStateException ex) { ProblemDetail p=ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT,ex.getMessage());p.setType(URI.create("urn:mysociety:finance:invalid-state"));return p; }
}
