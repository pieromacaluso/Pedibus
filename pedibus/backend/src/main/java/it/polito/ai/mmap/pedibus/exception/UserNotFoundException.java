package it.polito.ai.mmap.pedibus.exception;

import it.polito.ai.mmap.pedibus.configuration.PedibusString;

public class UserNotFoundException extends RuntimeException {

    /**
     * Constructs a {@code LineaNotFoundException} with no detail message.
     */
    public UserNotFoundException() {
        super(PedibusString.USER_NOT_FOUND);
    }
}
