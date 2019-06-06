package it.polito.ai.mmap.pedibus.exception;

public class FermataNotFoundException extends RuntimeException {

    /**
     * Constructs a {@code FermataNotFoundException} with no detail message.
     */
    public FermataNotFoundException() {
        super();
    }

    /**
     * Constructs a {@code FermataNotFoundException} with the specified
     * detail message.
     *
     * @param   s   the detail message.
     */
    public FermataNotFoundException(String s) {
        super(s);
    }
}

