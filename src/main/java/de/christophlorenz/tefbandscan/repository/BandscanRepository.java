package de.christophlorenz.tefbandscan.repository;

import de.christophlorenz.tefbandscan.model.BandscanEntry;

import java.util.List;

public interface BandscanRepository {

    public void init(String name) throws RepositoryException;

    public boolean addEntry(Integer frequencyKHz, String rdsPI, String rdsPS, Integer psErrors, Integer rdsErrors, Integer quality, Integer CCI, Integer snr) throws RepositoryException;

    public boolean addEntry(BandscanEntry bandscanEntry) throws RepositoryException;

    public List<BandscanEntry> getEntries();

    public void removeEntry(BandscanEntry latestLog) throws RepositoryException;

    BandscanEntry getByFrequencyAndPI(Integer frequency, String pi);
}
