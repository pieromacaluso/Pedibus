package it.polito.ai.mmap.pedibus.exception;

/**
 * OOT OutOfTime
 */
public class RegistrationOOTException extends RuntimeException {
    /**
     * Constructs a {@code RegistrationNotValidException} with no detail message.
     */
    public RegistrationOOTException() {
        super("Recover Process has ended unexpectedly.");
    }

    /**
     * Constructs a {@code RegistrationNotValidException} with the specified
     * detail message.
     *
     * @param s the detail message.
     */
    public RegistrationOOTException(String s) {
        super(s);
    }
}
