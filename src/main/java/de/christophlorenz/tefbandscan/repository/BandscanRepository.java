package de.christophlorenz.tefbandscan.repository;

import de.christophlorenz.tefbandscan.model.BandscanEntry;

import java.util.List;

public interface BandscanRepository {

    public void init(String name) throws RepositoryException;

    public void addEntry(Integer frequencyKHz, String rdsPI, String rdsPS, Integer quality) throws RepositoryException;

    public List<BandscanEntry> getEntries();
}
