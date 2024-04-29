package de.christophlorenz.tefbandscan.repository;

public class ConnectionLostException extends RepositoryException {
    public ConnectionLostException(String msg) {
        super(msg);
    }
}
