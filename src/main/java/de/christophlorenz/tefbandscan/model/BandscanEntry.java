package de.christophlorenz.tefbandscan.model;

// TODO: Primary key is combination (frequencyKhz|rdsPI)

public record BandscanEntry(int frequencyKHz, String rdsPI, String rdsPS, int quality) {
}
