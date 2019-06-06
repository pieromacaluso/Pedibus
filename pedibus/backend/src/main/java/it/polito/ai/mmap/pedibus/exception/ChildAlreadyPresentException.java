package it.polito.ai.mmap.pedibus.exception;

/**
 * Eccezione lanciata in fase di registrazione bambino da parte di un utente
 */
public class ChildAlreadyPresentException extends RuntimeException {
    public ChildAlreadyPresentException(String s){super(s);}
}
