package de.christophlorenz.tefbandscan;

import de.christophlorenz.tefbandscan.config.Tef6686Config;
import de.christophlorenz.tefbandscan.config.ThresholdsConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Controller;

@EnableConfigurationProperties(value = {Tef6686Config.class, ThresholdsConfig.class })
@SpringBootApplication
@EnableAsync
@Controller
public class TEFBandscan {

    public static void main(String[] args) {
        SpringApplication.run(TEFBandscan.class, args);
    }

}
