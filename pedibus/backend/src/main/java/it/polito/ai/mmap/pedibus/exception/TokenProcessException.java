package it.polito.ai.mmap.pedibus.exception;

import it.polito.ai.mmap.pedibus.configuration.PedibusString;

public class TokenProcessException extends RuntimeException {
    /**
     * Constructs a {@code RegistrationNotValidException} with no detail message.
     */
    public TokenProcessException() {
        super(PedibusString.TOKEN_PROCESS_FAILED);
    }

    /**
     * Constructs a {@code RegistrationNotValidException} with the specified
     * detail message.
     *
     * @param s the detail message.
     */
    public TokenProcessException(String s) {
        super(s);
    }
}
