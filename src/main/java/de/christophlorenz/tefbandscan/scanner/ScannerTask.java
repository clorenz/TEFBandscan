package de.christophlorenz.tefbandscan.scanner;

import de.christophlorenz.tefbandscan.repository.Repository;
import de.christophlorenz.tefbandscan.repository.RepositoryException;
import de.christophlorenz.tefbandscan.repository.TCPRepository;
import de.christophlorenz.tefbandscan.service.ScannerService;
import de.christophlorenz.tefbandscan.service.ServiceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
public class ScannerTask implements Runnable {

    private static final Logger LOGGER = LoggerFactory.getLogger(ScannerTask.class);

    private final TCPRepository tcpRepository;

    public ScannerTask(TCPRepository tcpRepository) {
        this.tcpRepository = tcpRepository;
    }

    private Repository repository;
    @Autowired
    private ScannerService service;

    @Override
    public void run() {
        repository = tcpRepository;
        try {
            repository.initialize();
            service.setRepository(repository);
        } catch (RepositoryException e) {
            System.err.println("Cannot connect to TEF6686: " + e.getMessage());
            Runtime.getRuntime().halt(1);
        }

        boolean interrupted=false;
        while ( !interrupted ) {
            try {
                service.scan();
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
