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
package com.onlyoffice.tenant.controller;

import com.onlyoffice.common.tenant.transfer.response.ErrorDetails;
import com.onlyoffice.tenant.exception.DocSpaceServiceException;
import com.onlyoffice.tenant.exception.TenantNotFoundException;
import com.onlyoffice.tenant.exception.UserServiceException;
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

  @ExceptionHandler(
      value = {
        TenantNotFoundException.class,
        DocSpaceServiceException.class,
        UserServiceException.class
      })
  public ResponseEntity<ErrorDetails> handleTenantNotFound(Throwable exception) {
    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
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
