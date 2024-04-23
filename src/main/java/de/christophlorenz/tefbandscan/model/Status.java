package de.christophlorenz.tefbandscan.model;

// TODO: Hier sollte der "Logged"-Status mit dabei sein!

import de.christophlorenz.tefbandscan.model.rds.PSWithErrors;

public record Status(Integer frequency, String rdsPi, Integer piErrors, String rdsPs, Integer psErrors, PSWithErrors psWithErrors, Integer rdsErrors, Float signal, Integer cci, Integer bandwidth, Integer snr, boolean logged) {
}
