package it.polito.ai.mmap.pedibus.exception;

public class SchoolClosedException extends RuntimeException {

    /**
     * Constructs a {@code SchoolClosedException} with no detail message.
     */
    public SchoolClosedException() {
        super();
    }

    /**
     * Constructs a {@code SchoolClosedException} with the specified
     * detail message.
     *
     * @param   s   the detail message.
     */
    public SchoolClosedException(String s) {
        super(s);
    }
}
