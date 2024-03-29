package de.christophlorenz.tefbandscan.service;

import de.christophlorenz.tefbandscan.repository.Repository;
import de.christophlorenz.tefbandscan.repository.RepositoryException;
import de.christophlorenz.tefbandscan.service.handler.RDSHandler;
import de.christophlorenz.tefbandscan.service.handler.StatusHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class ScannerService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ScannerService.class);
    private static final int LOG_INTERVAL = 1000;           // 1 second

    private final RDSHandler rdsHandler;
    private final StatusHandler statusHandler;

    private Repository repository;

    public ScannerService(RDSHandler rdsHandler, StatusHandler statusHandler) {
        this.rdsHandler = rdsHandler;
        this.statusHandler = statusHandler;
    }

    public void setRepository(Repository repository) {
        this.repository = repository;
    }


    public void scan() throws ServiceException, InterruptedException {
        long time = System.currentTimeMillis();
        String line;
        while (true) {
            try {
                if (!((line = repository.read()) != null)) break;
            } catch (RepositoryException e) {
                throw new ServiceException("Cannot read line: " + e, e);
            }
            if (line.isBlank()) continue;
            handleLine(line);
            if (System.currentTimeMillis() - time > LOG_INTERVAL) {
                time = System.currentTimeMillis();
                LOGGER.info("Status=" + statusHandler.toString());
                LOGGER.info("RDS=" + rdsHandler.toString());
            }
            Thread.sleep(1);
        }
    }

    private void handleLine(String line) {
        String action = line.substring(0,1).toUpperCase();
        switch (action) {
            case "M" -> {
                rdsHandler.reset();
                statusHandler.reset();
            }
            case "P" -> rdsHandler.handlePI(line);
            case "R" -> rdsHandler.handleRDSData(line);
            case "S" -> statusHandler.handleStatus(line);
            case "T" -> statusHandler.handleFrequency(line);
            default -> LOGGER.warn("Unknown action=" + action + " for line=" + line);
        }
    }
}
