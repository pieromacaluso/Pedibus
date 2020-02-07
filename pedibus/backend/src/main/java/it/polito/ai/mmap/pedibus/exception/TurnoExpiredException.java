package it.polito.ai.mmap.pedibus.exception;

import it.polito.ai.mmap.pedibus.configuration.PedibusString;

public class TurnoExpiredException extends RuntimeException {
    /**
     * Constructs a {@code DispNotValidException} with no detail message.
     */
    public TurnoExpiredException() {
        super(PedibusString.TURNO_EXPIRED);
    }
}