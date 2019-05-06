package it.polito.ai.mmap.esercitazione3.exception;

public class RecoverProcessNotValid extends RuntimeException {

    /**
     * Constructs a {@code RecoverProcessNotValid} with no detail message.
     */
    public RecoverProcessNotValid() {
        super("Recover Process has ended unexpectedly.");
    }

    /**
     * Constructs a {@code RecoverProcessNotValid} with the specified
     * detail message.
     *
     * @param s the detail message.
     */
    public RecoverProcessNotValid(String s) {
        super(s);
    }
}
