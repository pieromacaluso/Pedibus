package it.polito.ai.mmap.esercitazione2.exception;

public class PrenotazioneNotFoundException extends RuntimeException {
    /**
     * Constructs a {@code PrenotazioneNotFoundException} with no detail message.
     */
    public PrenotazioneNotFoundException() {
        super();
    }

    /**
     * Constructs a {@code PrenotazioneNotFoundException} with the specified
     * detail message.
     *
     * @param   s   the detail message.
     */
    public PrenotazioneNotFoundException(String s) {
        super(s);
    }
}
