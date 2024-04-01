package de.christophlorenz.tefbandscan.service;

public class ServiceException extends Exception {
    public ServiceException(String msg, Exception e) {
        super(msg, e);
    }
}
