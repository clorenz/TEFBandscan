package de.christophlorenz.tefbandscan.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.ConstructorBinding;

@ConfigurationProperties(prefix = "thresholds")
public record ThresholdsConfig(Thresholds auto, Thresholds manual){

    @ConstructorBinding
    public ThresholdsConfig{}

    public record Thresholds(int samples, int signal, int snr, int cci) {
    }
}
