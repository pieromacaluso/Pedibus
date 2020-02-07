package it.polito.ai.mmap.pedibus.exception;

import it.polito.ai.mmap.pedibus.configuration.PedibusString;

public class TurnoClosedException extends RuntimeException {
    /**
     * Constructs a {@code DispNotValidException} with no detail message.
     */
    public TurnoClosedException() {
        super(PedibusString.TURNO_CLOSED);
    }
}