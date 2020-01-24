package it.polito.ai.mmap.esercitazione3.exception;

public class RecoverProcessNotValidException extends RuntimeException {

    /**
     * Constructs a {@code RecoverProcessNotValidException} with no detail message.
     */
    public RecoverProcessNotValidException() {
        super("Recover Process has ended unexpectedly.");
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
