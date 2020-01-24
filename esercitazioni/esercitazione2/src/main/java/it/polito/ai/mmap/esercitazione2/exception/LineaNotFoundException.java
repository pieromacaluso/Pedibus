package it.polito.ai.mmap.esercitazione2.exception;

public class LineaNotFoundException extends RuntimeException {

    /**
     * Constructs a {@code LineaNotFoundException} with no detail message.
     */
    public LineaNotFoundException() {
        super();
    }

    /**
     * Constructs a {@code LineaNotFoundException} with the specified
     * detail message.
     *
     * @param   s   the detail message.
     */
    public LineaNotFoundException(String s) {
        super(s);
    }
}
