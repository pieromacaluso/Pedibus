package it.polito.ai.mmap.pedibus.exception;

import it.polito.ai.mmap.pedibus.configuration.PedibusString;

public class TokenNotFoundException extends RuntimeException {

    /**
     * Constructs a {@code TokenNotFoundException} with no detail message.
     */
    public TokenNotFoundException() {
        super(PedibusString.TOKEN_NOT_FOUND);
    }

    /**
     * Constructs a {@code TokenNotFoundException} with the specified
     * detail message.
     *
     * @param s the detail message.
     */
    public TokenNotFoundException(String s) {
        super(s);
    }
}