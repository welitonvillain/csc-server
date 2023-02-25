package br.com.angeloni.csc.server.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import javax.servlet.http.HttpServletRequest;

@ControllerAdvice
@Slf4j
public class RestExceptionHandler extends ResponseEntityExceptionHandler {

  @ExceptionHandler(value = HttpClientErrorException.class)
  ResponseEntity<byte[]> handleHttpClientException(HttpClientErrorException ex, HttpServletRequest request) {
    return new ResponseEntity<>(ex.getResponseBodyAsByteArray(), ex.getResponseHeaders(), ex.getStatusCode());
  }
}