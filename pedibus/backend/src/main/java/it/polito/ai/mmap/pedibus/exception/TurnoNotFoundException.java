package it.polito.ai.mmap.pedibus.exception;

import it.polito.ai.mmap.pedibus.configuration.PedibusString;

public class TurnoNotFoundException extends RuntimeException {
    /**
     * Constructs a {@code DispNotValidException} with no detail message.
     */
    public TurnoNotFoundException() {
        super(PedibusString.TURNO_NOT_FOUND);
    }
}
