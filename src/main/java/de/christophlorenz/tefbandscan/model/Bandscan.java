package de.christophlorenz.tefbandscan.model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public record Bandscan(List<BandscanEntry> bandscanEntries) {

    private static final Logger LOGGER = LoggerFactory.getLogger(Bandscan.class);

    public Bandscan() {
        this(new ArrayList<>());
    }

    public void addBandscanEntry(BandscanEntry bandscanEntry) {
        String primaryKey = bandscanEntry.getPrimaryKey();
        if (!containsPrimaryKey(primaryKey)) {
            bandscanEntries.add(bandscanEntry);
        } else {
            // Remove existing entry and add new
            BandscanEntry entryToRemove = bandscanEntries().stream()
                    .filter(e -> e.getPrimaryKey().equals(primaryKey)).findFirst()
                    .orElse(null);
            if (entryToRemove != null) {
                bandscanEntries.remove(entryToRemove);
                LOGGER.info("Removed bandscanEntry for primaryKey=" + primaryKey + ": " + entryToRemove);
            }
            bandscanEntries.add(bandscanEntry);
        }
    }

    private boolean containsPrimaryKey(String primaryKey) {
        for (BandscanEntry entry: bandscanEntries) {
            if (primaryKey.equals(entry.getPrimaryKey())) {
                return true;
            }
        }
        return false;
    }


}
