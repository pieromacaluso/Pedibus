package it.polito.ai.mmap.pedibus.exception;

public class DispNotFoundException extends RuntimeException {
    /**
     * Constructs a {@code DispNotFoundException} with no detail message.
     */
    public DispNotFoundException() {
        super();
    }

    /**
     * Constructs a {@code DispNotFoundException} with the specified
     * detail message.
     *
     * @param s the detail message.
     */
    public DispNotFoundException(String s) {
        super(s);
    }
}
