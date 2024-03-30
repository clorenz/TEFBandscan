package de.christophlorenz.tefbandscan.service;

import de.christophlorenz.tefbandscan.model.Bandscan;
import de.christophlorenz.tefbandscan.model.BandscanEntry;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class BandscanService {

    private Bandscan bandscan;

    public BandscanService() {
        bandscan = new Bandscan(new ArrayList<>());
    }

    public void addEntry(Integer frequencyKHz, String rdsPI, String rdsPS, Integer quality) {
        if (frequencyKHz == null) {
            return;
        }
        BandscanEntry bandscanEntry = new BandscanEntry(frequencyKHz, rdsPI, rdsPS, quality);
        bandscan.addBandscanEntry(bandscanEntry);
    }

    public List<BandscanEntry> getEntries() {
        return bandscan.bandscanEntries();
    }
}
