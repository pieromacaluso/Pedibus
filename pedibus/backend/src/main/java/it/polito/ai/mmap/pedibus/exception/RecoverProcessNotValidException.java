package it.polito.ai.mmap.pedibus.exception;

import it.polito.ai.mmap.pedibus.configuration.PedibusString;

public class RecoverProcessNotValidException extends RuntimeException {

    /**
     * Constructs a {@code RecoverProcessNotValidException} with no detail message.
     */
    public RecoverProcessNotValidException() {
        super(PedibusString.RECOVER_FAILED);
    }

    /**
     * Constructs a {@code RecoverProcessNotValidException} with the specified
     * detail message.
     *
     * @param s the detail message.
     */
    public RecoverProcessNotValidException(String s) {
        super(s);
    }
}
