package it.polito.ai.mmap.esercitazione3.exception;

public class TokenNotFoundException extends RuntimeException {

    /**
     * Constructs a {@code TokenNotFoundException} with no detail message.
     */
    public TokenNotFoundException() {
        super("Token not Found.");
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