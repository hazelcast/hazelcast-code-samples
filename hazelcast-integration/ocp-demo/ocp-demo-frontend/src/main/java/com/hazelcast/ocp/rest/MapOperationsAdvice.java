package com.hazelcast.ocp.rest;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

@ControllerAdvice
@Slf4j
public class MapOperationsAdvice {

    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(value = {IllegalStateException.class})
    protected void handleMapOperationsException(RuntimeException ex) {
        log.error("Exception forwarded to client {}", ex.getCause());
    }
}
