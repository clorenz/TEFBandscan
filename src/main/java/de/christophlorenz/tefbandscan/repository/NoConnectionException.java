package de.christophlorenz.tefbandscan.repository;

public class NoConnectionException extends RepositoryException{

    public NoConnectionException(String msg) {
        super(msg);
    }
}
