package it.polito.ai.mmap.pedibus.exception;

import it.polito.ai.mmap.pedibus.configuration.PedibusString;

public class ChildNotFoundException extends RuntimeException {
    public ChildNotFoundException(String cf){super(PedibusString.CHILD_NOT_FOUND(cf));}
}
