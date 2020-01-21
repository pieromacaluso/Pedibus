package it.polito.ai.mmap.pedibus.exception;

public class SysAdminException extends RuntimeException {
    /**
     * Constructs a {@code RegistrationNotValidException} with no detail message.
     */
    public SysAdminException() {
        super("SysAdmin Errore generico");
    }

    /**
     * Constructs a {@code RegistrationNotValidException} with the specified
     * detail message.
     *
     * @param s the detail message.
     */
    public SysAdminException(String s) {
        super(s);
    }
}
