package de.christophlorenz.tefbandscan.repository;

import de.christophlorenz.tefbandscan.model.BandscanEntry;

import java.util.List;

public interface BandscanRepository {

    public void addEntry(Integer frequencyKHz, String rdsPI, String rdsPS, Integer quality);

    public List<BandscanEntry> getEntries();
}
