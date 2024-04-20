package de.christophlorenz.tefbandscan.model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public record Bandscan(List<BandscanEntry> bandscanEntries) {

    private static final Logger LOGGER = LoggerFactory.getLogger(Bandscan.class);

    public Bandscan() {
        this(new ArrayList<>());
    }

    public boolean addBandscanEntry(BandscanEntry bandscanEntry) {
        String primaryKey = bandscanEntry.getPrimaryKey();
        if (!containsPrimaryKey(primaryKey)) {
            bandscanEntries.add(bandscanEntry);
            return true;
        } else {
            // Remove existing entry and add new
            BandscanEntry entryToRemove = bandscanEntries().stream()
                    .filter(e -> e.getPrimaryKey().equals(primaryKey)).findFirst()
                    .orElse(null);
            if (entryToRemove != null) {
                String ps = entryToRemove.getRdsPs();
                if (ps!=null && (bandscanEntry.getRdsPs()==null || bandscanEntry.getRdsPs().isBlank())) {
                    bandscanEntry.setRdsPs(ps);
                    LOGGER.info("Keeping existing PS=" + ps + " for " + bandscanEntry.getPrimaryKey());
                }
                bandscanEntries.remove(entryToRemove);
            }
            bandscanEntries.add(bandscanEntry);
            LOGGER.info("Updated bandscanEntry for primaryKey=" + primaryKey + ": " + bandscanEntry);
            return false;
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


    public void removeBandscanEntry(BandscanEntry bandscanEntry) {
        String primaryKey = bandscanEntry.getPrimaryKey();
        BandscanEntry entryToRemove = bandscanEntries().stream()
                .filter(e -> e.getPrimaryKey().equals(primaryKey)).findFirst()
                .orElse(null);
        if (entryToRemove != null) {
            bandscanEntries.remove(entryToRemove);
        }
    }

    public BandscanEntry getByFrequencyAndPI(Integer frequency, String pi) {
        return bandscanEntries.stream()
                .filter(b ->
                        Objects.equals(b.getFrequencyKHz(), frequency) &&
                                Objects.equals(b.getRdsPi(), pi)
                )
                        .findFirst()
                                .orElse(null);
    }
}
