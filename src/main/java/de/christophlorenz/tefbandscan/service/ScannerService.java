package de.christophlorenz.tefbandscan.service;

import de.christophlorenz.tefbandscan.model.Status;
import de.christophlorenz.tefbandscan.model.StatusHistory;
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
    private static final long TIMEOUT_MILLIS = 2000;         // 2 seconds
    private static final long TIMEOUT_MILLIS_RDS = 30000;   // 30 seconds - should be enough for stable PS decoding

    private final BandscanService bandscanService;
    private final RDSHandler rdsHandler;
    private final StatusHandler statusHandler;

    private final StatusHistory statusHistory;

    private Repository repository;
    private long lastAutoTimestamp=0;

    public ScannerService(BandscanService bandscanService, RDSHandler rdsHandler, StatusHandler statusHandler) {
        this.bandscanService = bandscanService;
        this.rdsHandler = rdsHandler;
        this.statusHandler = statusHandler;
        statusHistory = new StatusHistory();
    }

    public void setRepository(Repository repository) {
        this.repository = repository;
    }


    public void scan(boolean auto) throws ServiceException, InterruptedException {
        Integer startFrequency = null;
        if (auto) {
            startFrequency = 87500;
            try {
                repository.write("T" + startFrequency);     // TODO refactor this into an API!
                lastAutoTimestamp = System.currentTimeMillis();
            } catch (RepositoryException e) {
                throw new ServiceException("Cannot start automatic scan: " + e, e);
            }
        }


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
            if ((statusHistory.isStable() && statusHandler.getCurrentFrequency() != null) || isTimeout(lastAutoTimestamp, statusHandler.getSignalStrength())) {
                if (!auto) {
                    LOGGER.info("Stable entry detected. Will log. You can go on now");
                    blinkOnDevice();
                    generateLog();
                    Thread.sleep(1000);
                } else {
                    LOGGER.info("Stable entry detected. Continuing with next frequency");
                    generateLog();
                    if (startFrequency < 108000) {
                        startFrequency += 100;
                        try {
                            repository.write("T" + startFrequency);
                            lastAutoTimestamp = System.currentTimeMillis();
                        } catch (RepositoryException e) {
                            throw new ServiceException("Cannot set frequency to " + startFrequency + ": " + e, e);
                        }
                    } else {
                        LOGGER.info("Scan finished");
                    }
                }
            }
            if (System.currentTimeMillis() - time > LOG_INTERVAL) {
                time = System.currentTimeMillis();
                LOGGER.info("Status=" + statusHandler.toString());
                LOGGER.info("RDS=" + rdsHandler.toString());
            }
            Thread.sleep(1);
        }
    }

    private boolean isTimeout(long lastAutoTimestamp, Float signalStrength) {
        if (lastAutoTimestamp == 0) {
            return false;           // manual shall never timeout
        }

        if (signalStrength != null && signalStrength <= 20) {
            // We will never receive RDS here!
            return (System.currentTimeMillis() - lastAutoTimestamp > TIMEOUT_MILLIS);
        }

        return (System.currentTimeMillis() - lastAutoTimestamp > TIMEOUT_MILLIS_RDS);

    }

    private void blinkOnDevice()  {
        try {
            repository.write("B1");
            repository.read();
            repository.write("B0");
            repository.read();
        } catch (RepositoryException e) {
            LOGGER.warn("Cannot blink on device: " + e);
        }
    }

    private void handleLine(String line) {
        String action = line.substring(0,1).toUpperCase();
        switch (action) {
            case "M" -> {
                if (statusHistory.hasEnoughData()) {
                    // Avoid logging during (fast) tuning
                    bandscanService.addEntry(statusHandler.getCurrentFrequency(), rdsHandler.getPi(), rdsHandler.getPs(), Math.round(statusHandler.getSignalStrength()));
                }
                rdsHandler.reset();
                statusHandler.reset();
                statusHistory.reset();
            }
            case "P" -> rdsHandler.handlePI(line);
            case "R" -> rdsHandler.handleRDSData(line);
            case "S" -> statusHandler.handleStatus(line);
            case "T" -> statusHandler.handleFrequency(line);
            default -> LOGGER.warn("Unknown action=" + action + " for line=" + line);
        }

        statusHistory.setCurrentStatus(getCurrentStatus());
    }

    private void generateLog() {
        try {
            bandscanService.addEntry(statusHandler.getCurrentFrequency(), rdsHandler.getPi(), rdsHandler.getPs(), Math.round(statusHandler.getSignalStrength()));
            rdsHandler.reset();
            statusHandler.reset();
            statusHistory.reset();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public Status getCurrentStatus() {
        return new Status(statusHandler.getCurrentFrequency(), rdsHandler.getPi(), rdsHandler.getPs(), statusHandler.getSignalStrength(), statusHandler.getBandwidth());
    }
}
