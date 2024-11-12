package com.onlyoffice.user.controller;

import com.onlyoffice.common.user.transfer.ErrorDetails;
import com.onlyoffice.user.exception.UserNotFoundException;
import io.github.resilience4j.ratelimiter.RequestNotPermitted;
import java.util.HashMap;
import java.util.concurrent.TimeoutException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@ControllerAdvice
public class GlobalControllerAdvice extends ResponseEntityExceptionHandler {
  protected ResponseEntity<Object> handleMethodArgumentNotValid(
      MethodArgumentNotValidException exception,
      HttpHeaders headers,
      HttpStatusCode status,
      WebRequest request) {
    var errors = new HashMap<String, String>();
    var errorList = exception.getAllErrors();
    errorList.forEach(
        error -> {
          if (error instanceof FieldError ferror)
            errors.put(ferror.getField(), error.getDefaultMessage());
        });

    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errors);
  }

  @ExceptionHandler(value = {UserNotFoundException.class})
  public ResponseEntity<ErrorDetails> handleUserNotFound(UserNotFoundException exception) {
    return ResponseEntity.status(HttpStatus.NOT_FOUND)
        .body(ErrorDetails.builder().details(exception.getMessage()).build());
  }

  @ExceptionHandler(value = {TimeoutException.class})
  public ResponseEntity<?> handleTimeout(TimeoutException exception) {
    return ResponseEntity.status(HttpStatus.REQUEST_TIMEOUT).build();
  }

  @ExceptionHandler(value = {RequestNotPermitted.class})
  public ResponseEntity<?> handleLimit(RequestNotPermitted exception) {
    return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).build();
  }

  @ExceptionHandler(value = {Exception.class})
  public ResponseEntity<?> handleUnknown(Exception exception) {
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
  }
}
