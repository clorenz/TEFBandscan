package de.christophlorenz.tefbandscan.scanner;

import de.christophlorenz.tefbandscan.repository.*;
import de.christophlorenz.tefbandscan.service.ScannerService;
import de.christophlorenz.tefbandscan.service.ServiceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.stereotype.Component;
import sun.misc.Signal;

import java.util.List;

@Component
public class ScannerTask implements Runnable {

    private static final Logger LOGGER = LoggerFactory.getLogger(ScannerTask.class);

    private final TCPCommunicationRepository tcpRepository;
    private final CSVRepository csvRepository;

    public ScannerTask(CSVRepository csvRepository, TCPCommunicationRepository tcpRepository) {
        this.csvRepository = csvRepository;
        this.tcpRepository = tcpRepository;
    }

    private CommunicationRepository communicationRepository;
    private BandscanRepository bandscanRepository;
    @Autowired
    private ScannerService service;

    @Autowired
    ApplicationArguments applicationArguments;

    @Override
    public void run() {
        String database = "default";
        List<String> databaseNames = applicationArguments.getOptionValues("database");
        if (databaseNames != null && databaseNames.isEmpty()) {
            database = databaseNames.get(0);
        }
        bandscanRepository = csvRepository;
        try {
            bandscanRepository.init(database);
        } catch (RepositoryException e) {
            System.err.println("Cannot work with CSV based database: " + e.getMessage());
            Runtime.getRuntime().halt(1);
        }

        communicationRepository = tcpRepository;
        try {
            communicationRepository.initialize();
            service.setCommunicationRepository(communicationRepository);
            service.setBandscanRepository(bandscanRepository);
        } catch (RepositoryException e) {
            System.err.println("Cannot connect to TEF6686: " + e.getMessage());
            Runtime.getRuntime().halt(2);
        }

        boolean interrupted=false;
        while ( !interrupted ) {
            try {
                service.scan(false);
                Thread.sleep(1);
            } catch (InterruptedException e) {
                interrupted=true;
            } catch (ServiceException e) {
                LOGGER.error("Error during scan: " + e, e);
                interrupted=true;
            }
        }
    }
}
