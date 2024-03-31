package de.christophlorenz.tefbandscan.model;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public record Bandscan(List<BandscanEntry> bandscanEntries) {

    private static final Logger LOGGER = LoggerFactory.getLogger(Bandscan.class);

    private static Set<PrimaryKey> primaryKeys = new HashSet<>();
    public Bandscan() {
        this(new ArrayList<>());
    }

    public void addBandscanEntry(BandscanEntry bandscanEntry) {
        PrimaryKey primaryKey = new PrimaryKey(bandscanEntry.frequencyKHz(), bandscanEntry.rdsPi());
        if (!primaryKeys.contains(primaryKey)) {
            bandscanEntries.add(bandscanEntry);
            primaryKeys.add(primaryKey);
        } else {
            // Remove existing entry and add new
            BandscanEntry entryToRemove = bandscanEntries().stream()
                    .filter(e -> (
                                    NumberUtils.compare(bandscanEntry.getFrequencyKHz(), e.getFrequencyKHz()) == 0)
                            && (StringUtils.compare(bandscanEntry.rdsPi(), e.getRdsPi()) == 0)
                    ).findFirst()
                    .orElse(null);
            if (entryToRemove != null) {
                bandscanEntries.remove(entryToRemove);
                LOGGER.info("Removed obsolete entry=" + entryToRemove);
            }
            bandscanEntries.add(bandscanEntry);
            LOGGER.info("Updated entry=" + bandscanEntry);
        }
    }


    public record PrimaryKey(Integer frequencyKHz, String pi) {}
}
