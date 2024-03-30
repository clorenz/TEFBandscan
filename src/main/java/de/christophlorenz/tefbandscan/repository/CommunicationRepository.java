package de.christophlorenz.tefbandscan.repository;

public interface CommunicationRepository {

    public void initialize() throws RepositoryException;

    public String read() throws RepositoryException;

    public void write(String data) throws RepositoryException;
}
