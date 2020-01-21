package it.polito.ai.mmap.pedibus.exception;

import it.polito.ai.mmap.pedibus.configuration.PedibusString;

/**
 * Eccezione lanciata in fase di registrazione per segnalare che la mail è già stata usata
 */
public class UserAlreadyPresentException extends RuntimeException
{
    public UserAlreadyPresentException(String user) {super(PedibusString.USER_DUPLICATE(user));}

}
