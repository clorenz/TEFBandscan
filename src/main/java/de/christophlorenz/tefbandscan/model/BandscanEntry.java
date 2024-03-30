package de.christophlorenz.tefbandscan.model;

import jakarta.persistence.*;

@Entity
@Table(name = "bandscan_entries")
@IdClass(FrequencyPiId.class)
public class BandscanEntry {

    @Id
    private Integer frequencyKHz;
    @Id
    private String rdsPi;
    private String rdsPs;
    private int quality;

    public BandscanEntry() {
    }

    public BandscanEntry(Integer frequencyKHz, String rdsPI, String rdsPS, Integer quality) {
        this.frequencyKHz= frequencyKHz;
        this.rdsPi= rdsPI;
        this.rdsPs = rdsPS;
        this.quality = quality;
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
}
