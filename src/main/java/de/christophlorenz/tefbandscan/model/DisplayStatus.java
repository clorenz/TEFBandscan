package de.christophlorenz.tefbandscan.model;

import de.christophlorenz.tefbandscan.model.rds.PSWithErrors;

public class DisplayStatus {

    private Integer frequency;
    private String pi;
    private PSWithErrors psWithErrors;
    private Integer rdsErrors;
    private Float signal;
    private Integer averageSignal;
    private boolean validSignalStrength;
    private Integer cci;
    private Integer averageCci;
    private boolean validCci;
    private Integer snr;
    private Integer averageSnr;
    private boolean validSnr;
    private Integer modulation;
    private Integer averageModulation;
    private boolean validModulation;
    private Integer offset;
    private Integer averageOffset;
    private boolean validOffset;

    public DisplayStatus(Status status, StatusHistory statusHistory) {
        this.frequency = status.frequency();
        this.pi = status.rdsPi();
        this.psWithErrors = status.psWithErrors();
        this.rdsErrors = status.rdsErrors();
        this.signal = status.signal();
        this.averageSignal = statusHistory.getAverageSignal();
        this.validSignalStrength = statusHistory.isValidSignalStrength();
        this.cci = status.cci();
        this.averageCci = statusHistory.getAverageCCI();
        this.validCci = statusHistory.isValidCci();
        this.snr = status.snr();
        this.averageSnr = statusHistory.getAverageSnr();
        this.validSnr = statusHistory.isValidSnr();
        this.modulation = status.modulation();
        this.averageModulation = statusHistory.getAverageModulation();
        this.validModulation = statusHistory.hasTrueModulation();
        this.offset = status.offset();
        this.averageOffset = statusHistory.getAverageOffset();
        this.validOffset = statusHistory.hasNoLargeOffset();
    }

    public String getFrequency() {
        if (frequency == null) {
            return "---.---";
        }

        return String.format("%.02f MHz", ((float) frequency / 1000f));
    }

    public String getPi() {
        return pi;
    }

    public PSWithErrors getPsWithErrors() {
        return psWithErrors;
    }

    public Integer getRdsErrors() {
        return rdsErrors;
    }

    public String getSignal() {
        return signal != null ? (Math.round(signal) + "") : "";
    }

    public Integer getAverageSignal() {
        return averageSignal;
    }

    public boolean isValidSignalStrength() {
        return validSignalStrength;
    }

    public String getCci() {
        return cci != null ? (cci + "") : "";
    }

    public Integer getAverageCci() {
        return averageCci;
    }

    public boolean isValidCci() {
        return validCci;
    }

    public String getSnr() {
        return snr != null ? (snr + "") : null;
    }

    public Integer getAverageSnr() {
        return averageSnr;
    }

    public boolean isValidSnr() {
        return validSnr;
    }

    public String getModulation() {
        return modulation != null ? (modulation + "") : "";
    }

    public Integer getAverageModulation() {
        return averageModulation;
    }

    public boolean isValidModulation() {
        return validModulation;
    }

    public String getOffset() {
        return offset != null ? (offset + "") : "";
    }

    public Integer getAverageOffset() {
        return averageOffset;
    }

    public boolean isValidOffset() {
        return validOffset;
    }
}
