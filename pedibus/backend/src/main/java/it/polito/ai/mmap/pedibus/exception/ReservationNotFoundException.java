package it.polito.ai.mmap.pedibus.exception;

public class ReservationNotFoundException extends RuntimeException {
    /**
     * Constructs a {@code ReservationNotFoundException} with no detail message.
     */
    public ReservationNotFoundException() {
        super();
    }

    /**
     * Constructs a {@code ReservationNotFoundException} with the specified
     * detail message.
     *
     * @param   s   the detail message.
     */
    public ReservationNotFoundException(String s) {
        super(s);
    }
}
