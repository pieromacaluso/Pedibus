package it.polito.ai.mmap.pedibus.exception;

public class PrenotazioneNotValidException extends RuntimeException {
    /**
     * Constructs a {@code PrenotazioneNotValidException} with no detail message.
     */
    public PrenotazioneNotValidException() {
        super();
    }

    /**
     * Constructs a {@code PrenotazioneNotValidException} with the specified
     * detail message.
     *
     * @param   s   the detail message.
     */
    public PrenotazioneNotValidException(String s) {
        super(s);
    }
}
