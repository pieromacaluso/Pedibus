package it.polito.ai.mmap.pedibus.exception;

public class DispNotValidException extends RuntimeException {
    /**
     * Constructs a {@code DispNotValidException} with no detail message.
     */
    public DispNotValidException() {
        super();
    }

    /**
     * Constructs a {@code DispNotValidException} with the specified
     * detail message.
     *
     * @param s the detail message.
     */
    public DispNotValidException(String s) {
        super(s);
    }
}
