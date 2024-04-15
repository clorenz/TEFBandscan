package de.christophlorenz.tefbandscan.config;

import de.christophlorenz.tefbandscan.repository.*;
import de.christophlorenz.tefbandscan.service.AutoScannerService;
import de.christophlorenz.tefbandscan.service.ManualScannerService;
import de.christophlorenz.tefbandscan.service.ScannerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.Serial;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Configuration
public class ApplicationConfig {

    private static final Logger LOGGER = LoggerFactory.getLogger(ApplicationConfig.class);

    private final ApplicationArguments applicationArguments;
    private final CSVBandscanRepository csvBandscanRepository;
    private final AutoScannerService autoScannerService;
    private final ManualScannerService manualScannerService;
    private final SerialCommunicationRepository serialCommunicationRepository;
    private final TCPCommunicationRepository tcpCommunicationRepository;

    public ApplicationConfig(ApplicationArguments applicationArguments, AutoScannerService autoScannerService, ManualScannerService manualScannerService,
                             CSVBandscanRepository csvBandscanRepository,
                             SerialCommunicationRepository serialCommunicationRepository,
                             TCPCommunicationRepository tcpCommunicationRepository) {
        this.applicationArguments = applicationArguments;
        this.autoScannerService = autoScannerService;
        this.manualScannerService = manualScannerService;
        this.csvBandscanRepository = csvBandscanRepository;
        this.serialCommunicationRepository = serialCommunicationRepository;
        this.tcpCommunicationRepository = tcpCommunicationRepository;
    }

    @Bean(name = "currentScanner")
    ScannerService getScannerService() {
        Set<String> optionNames = applicationArguments.getOptionNames();
        boolean autoScan;
        if (optionNames==null || optionNames.isEmpty()) {
            autoScan = false;
        } else {
            optionNames = optionNames.stream().map(String::toUpperCase).collect(Collectors.toSet());
            autoScan = optionNames.contains("AUTO");
        }

        if (autoScan) {
            LOGGER.info("Automatic scanning");
            return autoScannerService;
        } else {
            LOGGER.info("Manual scanning");
            return manualScannerService;
        }
    }

    @Bean(name = "currentBandscanRepository")
    BandscanRepository getBandscanRepository() {
        String database = "default";
        List<String> databaseNames = applicationArguments.getOptionValues("database");
        if (databaseNames != null && !databaseNames.isEmpty()) {
            database = databaseNames.get(0);
        }
        try {
            csvBandscanRepository.init(database);
        } catch (RepositoryException e) {
            System.err.println("Cannot work with CSV based database " + database + ": " + e.getMessage());
            Runtime.getRuntime().halt(1);
        }
        return csvBandscanRepository;
    }

    @Bean(name = "currentCommunicationRepository")
    CommunicationRepository getCommunicationRepository() {
        if (applicationArguments.getNonOptionArgs().contains("serial")) {
            try {
                serialCommunicationRepository.initialize();
            } catch (RepositoryException e) {
                System.err.println("Cannot initialize serial communication: " + e.getMessage());
                Runtime.getRuntime().halt(2);
            }
            LOGGER.info("Initializing serial communication");
            return serialCommunicationRepository;
        } else {
            try {
                tcpCommunicationRepository.initialize();
            } catch (RepositoryException e) {
                System.err.println("Cannot initialize TCP communication: " + e.getMessage());
                Runtime.getRuntime().halt(2);
            }
            LOGGER.info("Initializing WIFI communication");
            return tcpCommunicationRepository;
        }
    }
}
