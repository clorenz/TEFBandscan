package de.christophlorenz.tefbandscan.service.handler;

import de.christophlorenz.tefbandscan.repository.RepositoryException;
import de.christophlorenz.tefbandscan.service.ScannerService;
import de.christophlorenz.tefbandscan.service.ServiceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class LineHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(LineHandler.class);

    ScannerService scannerService;

    public void setScannerService(ScannerService scannerService) {
        this.scannerService = scannerService;
    }

    public void handle(String line) {
        if (line==null || line.isBlank()) {
            return;
        }

        String action = line.substring(0,1).toUpperCase();
        String data = line.substring(1);
        switch(action) {
            case "0" -> {
                if (!"OK".equalsIgnoreCase(data)) {
                    scannerService.handleStatus(data);
                } else {
                    LOGGER.info("Received OK");
                }
            }
            case "B" -> LOGGER.info("Stereo toggle=" + data);
            case "M" -> {
                if (!"0".equals(data)) {
                    scannerService.handleFrequencyChange();
                    scannerService.setFrequency(data);
                }
            }
            case "P" -> scannerService.handlePI(data);
            case "Q" -> LOGGER.info("Got Squelch confirmation=" + line);
            case "R" -> scannerService.handleRDSData(data);
            case "S" -> scannerService.handleStatus(data);
            case "T" -> {
                scannerService.handleFrequencyChange();
                scannerService.setFrequency(data);
            }
            default -> LOGGER.warn("Unknown action=" + action + " for line=" + line);
        }
    }
}
