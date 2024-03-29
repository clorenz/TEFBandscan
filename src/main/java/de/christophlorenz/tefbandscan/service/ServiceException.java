package de.christophlorenz.tefbandscan.service;

import de.christophlorenz.tefbandscan.repository.RepositoryException;

public class ServiceException extends Exception {
    public ServiceException(String msg, Exception e) {
        super(msg, e);
    }
}
