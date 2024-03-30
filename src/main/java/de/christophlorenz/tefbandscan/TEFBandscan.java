package de.christophlorenz.tefbandscan;

import de.christophlorenz.tefbandscan.config.Tef6686Config;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Controller;

@EnableConfigurationProperties({Tef6686Config.class})
@SpringBootApplication
@EnableAsync
@Controller
public class TEFBandscan {

    public static void main(String[] args) {
        SpringApplication.run(TEFBandscan.class, args);
    }

}
