package de.christophlorenz.tefbandscan.model;


import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;

public class BandscanEntry implements Comparable<BandscanEntry>{

    private Integer frequencyKHz;
    private String rdsPi;
    private String rdsPs;
    private int quality;
    private LocalDateTime timestamp;

    public BandscanEntry() {
    }

    public BandscanEntry(Integer frequencyKHz, String rdsPI, String rdsPS, Integer quality) {
        this(frequencyKHz, rdsPI, rdsPS, quality, LocalDateTime.now());
    }

    public BandscanEntry(int frequencyKHz, String rdsPI, String rdsPS, int quality, LocalDateTime timestamp) {
        this.frequencyKHz= frequencyKHz;
        this.rdsPi= rdsPI;
        if (rdsPi != null && rdsPi.isBlank()) {
            rdsPi = null;
        }
        this.rdsPs = rdsPS;
        if (rdsPs != null && rdsPs.isBlank()) {
            rdsPs = null;
        }
        this.quality = quality;
        this.timestamp = timestamp;
    }

    public Integer getFrequencyKHz() {
        return frequencyKHz;
    }

    public void setFrequencyKHz(Integer frequencyKHz) {
        this.frequencyKHz = frequencyKHz;
    }

    public String getRdsPi() {
        return rdsPi;
    }

    public void setRdsPi(String rdsPi) {
        this.rdsPi = rdsPi;
    }

    public String getRdsPs() {
        return rdsPs;
    }

    public void setRdsPs(String rdsPs) {
        this.rdsPs = rdsPs;
    }

    public int getQuality() {
        return quality;
    }

    public void setQuality(int quality) {
        this.quality = quality;
    }

    public Integer frequencyKHz() {
        return frequencyKHz;
    }

    public String rdsPi() {
        return rdsPi;
    }

    public String rdsPs() {
        return rdsPs;
    }

    public int quality() {
        return quality;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BandscanEntry that = (BandscanEntry) o;
        return quality == that.quality && Objects.equals(frequencyKHz, that.frequencyKHz) && Objects.equals(rdsPi, that.rdsPi) && Objects.equals(rdsPs, that.rdsPs) && Objects.equals(timestamp, that.timestamp);
    }

    @Override
    public int hashCode() {
        return Objects.hash(frequencyKHz, rdsPi, rdsPs, quality, timestamp);
    }

    @Override
    public String toString() {
        return "BandscanEntry{" +
                "frequencyKHz=" + frequencyKHz +
                ", rdsPi='" + rdsPi + '\'' +
                ", rdsPs='" + rdsPs + '\'' +
                ", quality=" + quality +
                ", timestamp=" + timestamp +
                '}';
    }

    @Override
    public int compareTo(BandscanEntry bandscanEntry) {
        if (bandscanEntry==null || bandscanEntry.getFrequencyKHz()==null) {
            return 1;
        }
        if (bandscanEntry.getFrequencyKHz().equals(frequencyKHz)) {
            return StringUtils.compare(rdsPi, bandscanEntry.getRdsPi());
        }
        return NumberUtils.compare(frequencyKHz, bandscanEntry.getFrequencyKHz());
    }
}
