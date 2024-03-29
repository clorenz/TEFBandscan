package de.christophlorenz.tefbandscan.repository;

public interface Repository {

    public void initialize() throws RepositoryException;

    public String read() throws RepositoryException;

    public void write(String data) throws RepositoryException;
}
