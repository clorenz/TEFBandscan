package de.christophlorenz.tefbandscan.model;

import java.util.List;

public record Bandscan(List<BandscanEntry> bandscanEntries) {
    public void addBandscanEntry(BandscanEntry bandscanEntry) {
        // TODO: Only, if no entry exists for primary key
        bandscanEntries.add(bandscanEntry);
    }
}
