package de.christophlorenz.tefbandscan.repository;

import java.io.IOException;

public class RepositoryException extends Exception {
    public RepositoryException(String msg, Exception e) {
        super(msg, e);
    }
}
