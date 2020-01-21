package it.polito.ai.mmap.pedibus.exception;

import it.polito.ai.mmap.pedibus.configuration.PedibusString;
import org.springframework.security.core.AuthenticationException;

public class PermissionDeniedException extends AuthenticationException {
    public PermissionDeniedException() {
        super(PedibusString.PERMISSION_DENIED);
    }
    public PermissionDeniedException(String msg) {
        super(msg);
    }

    public PermissionDeniedException(String msg, Throwable t) {
        super(msg, t);
    }
}

