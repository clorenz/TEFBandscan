package de.christophlorenz.tefbandscan.model;


import org.apache.commons.lang3.StringUtils;

import java.time.LocalDateTime;
import java.util.Objects;

public class BandscanEntry implements Comparable<BandscanEntry>{

    private Integer frequencyKHz;
    private String rdsPi;
    private String rdsPs;
    private int signalStrength;
    private int cci;
    private LocalDateTime timestamp;

    public BandscanEntry() {
    }

    public BandscanEntry(Integer frequencyKHz, String rdsPI, String rdsPS, Integer signalStrength, Integer cci) {
        this(frequencyKHz, rdsPI, rdsPS, signalStrength, cci, LocalDateTime.now());
    }

    public BandscanEntry(int frequencyKHz, String rdsPI, String rdsPS, int signalStrength, int cci, LocalDateTime timestamp) {
        this.frequencyKHz= frequencyKHz;
        this.rdsPi= rdsPI;
        if (rdsPi != null && rdsPi.isBlank()) {
            rdsPi = null;
        }
        this.rdsPs = rdsPS;
        if (rdsPs != null && rdsPs.isBlank()) {
            rdsPs = null;
        }
        this.signalStrength = signalStrength;
        this.cci = cci;
        this.timestamp = timestamp;
    }

    public Integer getFrequencyKHz() {
        return frequencyKHz;
    }

    public void setFrequencyKHz(Integer frequencyKHz) {
        this.frequencyKHz = frequencyKHz;
    }

    public String getRdsPi() {
        return (rdsPi == null || rdsPi.isBlank()) ? null : rdsPi;
    }

    public void setRdsPi(String rdsPi) {
        this.rdsPi = rdsPi;
    }

    public String getRdsPs() {
        return (rdsPs == null || rdsPs.isBlank()) ? null : rdsPs;
    }

    public void setRdsPs(String rdsPs) {
        this.rdsPs = rdsPs;
    }

    public int getSignalStrength() {
        return signalStrength;
    }

    public void setSignalStrength(int signalStrength) {
        this.signalStrength = signalStrength;
    }

    public int getCci() {
        return cci;
    }

    public void setCci(int cci) {
        this.cci = cci;
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

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public String getPrimaryKey() {
        return frequencyKHz + "|" + rdsPi;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BandscanEntry that = (BandscanEntry) o;
        return signalStrength == that.signalStrength && cci == that.cci && Objects.equals(frequencyKHz, that.frequencyKHz) && Objects.equals(rdsPi, that.rdsPi) && Objects.equals(rdsPs, that.rdsPs) && Objects.equals(timestamp, that.timestamp);
    }

    @Override
    public int hashCode() {
        return Objects.hash(frequencyKHz, rdsPi, rdsPs, signalStrength, cci, timestamp);
    }

    @Override
    public String toString() {
        return "BandscanEntry{" +
                "frequencyKHz=" + frequencyKHz +
                ", rdsPi='" + rdsPi + '\'' +
                ", rdsPs='" + rdsPs + '\'' +
                ", signalStrength=" + signalStrength +
                ", cci=" + cci +
                ", timestamp=" + timestamp +
                '}';
    }

    @Override
    public int compareTo(BandscanEntry other) {
        return StringUtils.compare(getPrimaryKey(), other.getPrimaryKey());
    }
}
