package it.polito.ai.mmap.pedibus.exception;

import it.polito.ai.mmap.pedibus.configuration.PedibusString;

public class ReservationNotValidException extends RuntimeException {
    /**
     * Constructs a {@code ReservationNotValidException} with no detail message.
     */
    public ReservationNotValidException() {
        super();
    }

    /**
     * Constructs a {@code ReservationNotValidException} with the specified
     * detail message.
     *
     * @param   s   the detail message.
     */
    public ReservationNotValidException(String s) {
        super(s);
    }
}
