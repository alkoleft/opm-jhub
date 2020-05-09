package io.oscript.hub.api.exceptions;

import io.oscript.hub.api.response.Response;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@ControllerAdvice
public class RestExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler({RuntimeException.class})
    public ResponseEntity<Response> handleInternal(final RuntimeException ex, final WebRequest request) {
        logger.error("RuntimeException", ex);

        return new ResponseEntity<>(
                Response.errorResult(ex.getClass().getSimpleName()),
                HttpStatus.INTERNAL_SERVER_ERROR
        );
    }

    @ExceptionHandler({EntityNotFoundException.class})
    public ResponseEntity<Response> handleInternal(final EntityNotFoundException ex, final WebRequest request) {
        logger.error("NotFoundException", ex);

        return new ResponseEntity<>(
                Response.errorResult(ex.getMessage()),
                HttpStatus.NOT_FOUND
        );
    }
}
