package de.christophlorenz.tefbandscan.repository;

import de.christophlorenz.tefbandscan.model.Bandscan;
import de.christophlorenz.tefbandscan.model.BandscanEntry;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Repository("inmemory")
public class InMemoryBandscanRepository implements BandscanRepository {

    private Bandscan bandscan;

    public InMemoryBandscanRepository() {
        bandscan = new Bandscan();
    }

    @Override
    public void init(String name) {
        // do nothing
    }

    @Override
    public void addEntry(Integer frequencyKHz, String rdsPI, String rdsPS, Integer quality) {
        if (frequencyKHz == null) {
            return;
        }
        BandscanEntry bandscanEntry = new BandscanEntry(frequencyKHz, rdsPI, rdsPS, quality);
        bandscan.addBandscanEntry(bandscanEntry);
    }

    @Override
    public List<BandscanEntry> getEntries() {
        return bandscan.bandscanEntries();
    }
}
