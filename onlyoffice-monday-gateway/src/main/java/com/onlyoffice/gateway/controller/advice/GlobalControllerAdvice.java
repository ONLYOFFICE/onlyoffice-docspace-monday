/**
 * (c) Copyright Ascensio System SIA 2025
 *
 * <p>Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of the License at
 *
 * <p>http://www.apache.org/licenses/LICENSE-2.0
 *
 * <p>Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.onlyoffice.gateway.controller.advice;

import io.github.resilience4j.ratelimiter.RequestNotPermitted;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ValidationException;
import java.util.HashMap;
import java.util.Objects;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

/**
 * Global exception handler for controllers that provides consistent error responses across the
 * application.
 *
 * <p>This class centralizes exception handling for all controllers, ensuring that clients receive
 * appropriate HTTP status codes and error messages. It also provides logging of exceptions for
 * monitoring and debugging purposes.
 */
@Slf4j
@ControllerAdvice
public class GlobalControllerAdvice {

  /**
   * Handles requests for resources that don't exist.
   *
   * @param ex the exception thrown when a resource is not found
   * @return a standardized error response with 404 status
   */
  @ExceptionHandler(value = {NoResourceFoundException.class})
  @ResponseStatus(HttpStatus.NOT_FOUND)
  public org.springframework.web.ErrorResponse handleNoResourceFoundException(
      NoResourceFoundException ex) {
    return org.springframework.web.ErrorResponse.create(ex, HttpStatus.NOT_FOUND, ex.getMessage());
  }

  /**
   * Handles requests with unsupported HTTP methods.
   *
   * @param ex the exception thrown when an unsupported HTTP method is used
   * @param request the current HTTP request
   * @return a response entity with 405 Method Not Allowed status and allowed methods
   */
  @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
  public ResponseEntity<?> handleMethodNotSupportedException(
      HttpRequestMethodNotSupportedException ex, HttpServletRequest request) {
    var body = new HashMap<String, Object>();
    body.put("error", "Method Not Allowed");
    body.put("message", ex.getMessage());
    body.put("path", request.getRequestURI());
    body.put("supportedMethods", ex.getSupportedHttpMethods());

    return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).body(body);
  }

  /**
   * Handles requests with missing required parameters.
   *
   * @param ex the exception thrown when a required parameter is missing
   * @param request the current HTTP request
   * @return a response entity with 400 Bad Request status and error details
   */
  @ExceptionHandler(MissingServletRequestParameterException.class)
  public ResponseEntity<?> handleMissingParameterException(
      MissingServletRequestParameterException ex, HttpServletRequest request) {
    var body = new HashMap<String, Object>();
    body.put("error", "Bad Request");
    body.put("message", "Required parameter '" + ex.getParameterName() + "' is missing");
    body.put("path", request.getRequestURI());

    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
  }

  /**
   * Handles bean validation errors in request bodies.
   *
   * @param e the exception containing validation errors
   * @return a response entity with 400 Bad Request status and validation error details
   */
  @ExceptionHandler(value = {MethodArgumentNotValidException.class})
  public ResponseEntity<?> handleValidationException(MethodArgumentNotValidException e) {
    var body = new HashMap<String, Object>();
    var errors =
        e.getBindingResult().getFieldErrors().stream()
            .collect(
                Collectors.toMap(
                    FieldError::getField,
                    fieldError -> {
                      String defaultMessage = fieldError.getDefaultMessage();
                      return defaultMessage != null ? defaultMessage : "Invalid value";
                    },
                    (error1, error2) -> error1));

    body.put("error", "Validation Failed");
    body.put("details", errors);

    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
  }

  /**
   * Handles validation errors in handler method parameters.
   *
   * @param e the exception containing validation errors for method parameters
   * @return a response entity with 400 Bad Request status and validation error details
   */
  @ExceptionHandler(value = HandlerMethodValidationException.class)
  public ResponseEntity<?> handleValidationException(HandlerMethodValidationException e) {
    var body = new HashMap<String, Object>();
    var errors = new HashMap<String, String>();

    e.getAllValidationResults()
        .forEach(
            result -> {
              String paramName = result.getMethodParameter().getParameterName();
              String errorMessage =
                  result.getResolvableErrors().stream()
                      .map(MessageSourceResolvable::getDefaultMessage)
                      .filter(Objects::nonNull)
                      .collect(Collectors.joining(", "));

              errors.put(paramName != null ? paramName : "unknown", errorMessage);
            });

    body.put("error", "Parameter Validation Failed");
    body.put("details", errors);

    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
  }

  /**
   * Handles general validation exceptions.
   *
   * @param e the validation exception
   * @return a response entity with 400 Bad Request status and error message
   */
  @ExceptionHandler(value = ValidationException.class)
  public ResponseEntity<?> handleValidationException(ValidationException e) {
    var body = new HashMap<String, Object>();
    body.put("error", "Validation Failed");
    body.put("message", e.getMessage());

    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
  }

  /**
   * Handles request timeout exceptions.
   *
   * @param ex the timeout exception
   * @return a response entity with 408 Request Timeout status
   */
  @ExceptionHandler(TimeoutException.class)
  public ResponseEntity<?> handleTimeout(TimeoutException ex) {
    return ResponseEntity.status(HttpStatus.REQUEST_TIMEOUT).build();
  }

  /**
   * Handles rate limiting exceptions when too many requests are made.
   *
   * @param ex the rate limiting exception
   * @return a response entity with 429 Too Many Requests status
   */
  @ExceptionHandler(RequestNotPermitted.class)
  public ResponseEntity<?> handleRateLimitExceeded(RequestNotPermitted ex) {
    var body = new HashMap<String, Object>();
    body.put("error", "Too Many Requests");
    body.put("message", "Rate limit exceeded. Please try again later.");

    return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(body);
  }

  /**
   * Fallback handler for all unhandled exceptions.
   *
   * <p>This method catches any exceptions not specifically handled by other methods and returns a
   * generic 500 Internal Server Error response.
   *
   * @param ex the unhandled exception
   * @return a response entity with 500 Internal Server Error status
   */
  @ExceptionHandler(Exception.class)
  public ResponseEntity<?> handleGenericException(Exception ex) {
    log.error("Unhandled exception", ex);
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
  }
}
