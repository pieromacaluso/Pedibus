package it.polito.ai.mmap.pedibus.configuration;

import it.polito.ai.mmap.pedibus.exception.*;
import it.polito.ai.mmap.pedibus.objectDTO.ErrorDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.sql.Timestamp;
import java.time.DateTimeException;

@ControllerAdvice
public class RestResponseEntityExceptionHandler extends ResponseEntityExceptionHandler {

    private Logger logger = LoggerFactory.getLogger(this.getClass());


    @ExceptionHandler(value = {
            IllegalArgumentException.class,
            IllegalStateException.class,
            DateTimeException.class,
            NullPointerException.class,
            LineaNotFoundException.class,
            PrenotazioneNotValidException.class,
            PrenotazioneNotFoundException.class,
            FermataNotFoundException.class,
            UserAlreadyPresentException.class,
            RegistrationNotValidException.class,
            RegistrationOOTException.class,
            ChildAlreadyPresentException.class,
            ChildNotFoundException.class,
            UsernameNotFoundException.class})
    protected ResponseEntity<Object> handleConflict(RuntimeException ex, WebRequest request) {
        ErrorDTO e = ErrorDTO.builder()
                .exception(ex.getClass().getSimpleName())
                .timestamp(new Timestamp(System.currentTimeMillis()))
                .errorMessage(ex.getMessage())
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .error(HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase())
                .path(((ServletWebRequest) request).getRequest().getRequestURI())
                .build();
//        ex.printStackTrace();
        logger.error(e.toString());
        return handleExceptionInternal(ex, e, new HttpHeaders(), HttpStatus.INTERNAL_SERVER_ERROR, request);
    }

    @ExceptionHandler(value = {
            AuthenticationException.class})
    protected ResponseEntity<Object> handleUnauthorized(RuntimeException ex, WebRequest request) {
        ErrorDTO e = ErrorDTO.builder()
                .exception(ex.getClass().getSimpleName())
                .timestamp(new Timestamp(System.currentTimeMillis()))
                .errorMessage(ex.getMessage())
                .status(HttpStatus.UNAUTHORIZED.value())
                .error(HttpStatus.UNAUTHORIZED.getReasonPhrase())
                .path(((ServletWebRequest) request).getRequest().getRequestURI())
                .build();
//        ex.printStackTrace();
        logger.error(e.toString());
        return handleExceptionInternal(ex, e, new HttpHeaders(), HttpStatus.UNAUTHORIZED, request);
    }

    @ExceptionHandler(value = {
            PermissionDeniedException.class})
    protected ResponseEntity<Object> handleForbidden(RuntimeException ex, WebRequest request) {
        ErrorDTO e = ErrorDTO.builder()
                .exception(ex.getClass().getSimpleName())
                .timestamp(new Timestamp(System.currentTimeMillis()))
                .errorMessage(ex.getMessage())
                .status(HttpStatus.FORBIDDEN.value())
                .error(HttpStatus.FORBIDDEN.getReasonPhrase())
                .path(((ServletWebRequest) request).getRequest().getRequestURI())
                .build();
//        ex.printStackTrace();
        logger.error(e.toString());
        return handleExceptionInternal(ex, e, new HttpHeaders(), HttpStatus.FORBIDDEN, request);
    }

    @ExceptionHandler(value = {
            TokenNotFoundException.class,
            RecoverProcessNotValidException.class})
    protected ResponseEntity<Object> handleNotFound(RuntimeException ex, WebRequest request) {
        ErrorDTO e = ErrorDTO.builder()
                .exception(ex.getClass().getSimpleName())
                .timestamp(new Timestamp(System.currentTimeMillis()))
                .errorMessage(ex.getMessage())
                .status(HttpStatus.NOT_FOUND.value())
                .error(HttpStatus.NOT_FOUND.getReasonPhrase())
                .path(((ServletWebRequest) request).getRequest().getRequestURI())
                .build();
//        ex.printStackTrace();
        logger.error(e.toString());
        return handleExceptionInternal(ex, e, new HttpHeaders(), HttpStatus.NOT_FOUND, request);
    }
}
