package it.polito.ai.mmap.pedibus.exception;

import it.polito.ai.mmap.pedibus.configuration.PedibusString;

/**
 * Eccezione lanciata in fase di registrazione bambino da parte di un utente
 */
public class DispAlreadyPresentException extends RuntimeException {
    public DispAlreadyPresentException() {
        super(PedibusString.DISP_DUPLICATE);
    }
}
