package de.christophlorenz.tefbandscan.model;

public record Status(Integer frequency, String rdsPi, String rdsPs, Float signal, Integer bandwidth) {
}
