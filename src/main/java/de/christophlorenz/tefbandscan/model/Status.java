package de.christophlorenz.tefbandscan.model;

// TODO: Hier sollte der "Logged"-Status mit dabei sein!

public record Status(Integer frequency, String rdsPi, String rdsPs, Integer rdsErrors, Float signal, Integer cci, Integer bandwidth, Integer snr, int rdsErrorRate) {
}
