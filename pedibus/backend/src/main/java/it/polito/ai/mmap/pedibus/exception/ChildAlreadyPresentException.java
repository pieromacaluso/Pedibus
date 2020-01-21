package it.polito.ai.mmap.pedibus.exception;

import it.polito.ai.mmap.pedibus.configuration.PedibusString;

/**
 * Eccezione lanciata in fase di registrazione bambino da parte di un utente
 */
public class ChildAlreadyPresentException extends RuntimeException {
    public ChildAlreadyPresentException(String cf){super(PedibusString.CHILD_DUPLICATE(cf));}
}
