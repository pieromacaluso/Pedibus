package it.polito.ai.mmap.esercitazione3.exception;

import org.springframework.security.core.AuthenticationException;

public class PermissionDeniedException extends AuthenticationException {
    public PermissionDeniedException(String msg) {
        super(msg);
    }

    public PermissionDeniedException(String msg, Throwable t) {
        super(msg, t);
    }
}

