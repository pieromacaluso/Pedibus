package it.polito.ai.mmap.pedibus.exception;

public class RegistrationNotValidException extends RuntimeException {
    /**
     * Constructs a {@code RegistrationNotValidException} with no detail message.
     */
    public RegistrationNotValidException() {
        super("Recover Process has ended unexpectedly.");
    }

    /**
     * Constructs a {@code RegistrationNotValidException} with the specified
     * detail message.
     *
     * @param s the detail message.
     */
    public RegistrationNotValidException(String s) {
        super(s);
    }
}
