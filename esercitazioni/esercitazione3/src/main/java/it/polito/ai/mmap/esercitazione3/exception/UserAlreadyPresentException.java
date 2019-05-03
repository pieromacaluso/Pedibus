package it.polito.ai.mmap.esercitazione3.exception;

/**
 * Eccezione lanciata in fase di registrazione per segnalare che la mail è già stata usata
 */
public class UserAlreadyPresentException extends RuntimeException
{
    public UserAlreadyPresentException() {super();}
    public UserAlreadyPresentException(String s) {super(s);}

}
