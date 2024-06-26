package de.christophlorenz.tefbandscan.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.ConstructorBinding;

@ConfigurationProperties(prefix = "tef6686")
public record Tef6686Config(String hostname, int port, String password, String serial){

    @ConstructorBinding
    public Tef6686Config{}
};
