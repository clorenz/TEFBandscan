package de.christophlorenz.tefbandscan.model;

public enum Bandwidth {

    BANDWIDTH_114(6, 114000),
    BANDWIDTH_133(7, 133000);

    public final int tefKey;
    public final int kHz;

    private Bandwidth(int tefKey, int kHz) {
        this.tefKey = tefKey;
        this.kHz = kHz;
    }

    public int getTEFKey() {
        return tefKey;
    }

    public int getkHz() {
        return kHz;
    }
}
