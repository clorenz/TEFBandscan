package de.christophlorenz.tefbandscan.service.handler;

import de.christophlorenz.tefbandscan.service.ScannerService;
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

    private boolean ignoreNextLine=false;
    private int lastFrequencyKhz=0;

    public void handle(String line) {
        if (ignoreNextLine) {
            ignoreNextLine=false;
            return;
        }
        if (line==null || line.isBlank() || "OK".equalsIgnoreCase(line)) {
            return;
        }

        if (!line.matches("^\\D.*$")) {
            LOGGER.warn("Invalid line=" + line);
        }

        String action = line.substring(0,1).toUpperCase();
        String data = line.substring(1);
        switch(action) {
            case "0" -> {
                if (!"OK".equalsIgnoreCase(data)) {
                    scannerService.handleStatus(data.substring(1));
                } else {
                    LOGGER.info("Received OK");
                }
            }
            case "B" -> LOGGER.info("Stereo toggle=" + data);
            case "M" -> {
                if (!"0".equals(data)) {
                    LOGGER.info("M: Handle frequency changed for data=" + data);
                    scannerService.handleFrequencyChange();
                    scannerService.setFrequency(data);
                }
            }
            case "P" -> scannerService.handlePI(data);
            case "Q" -> {
                LOGGER.info("Got Squelch confirmation=" + line);
                // BUG in TEF6686_ESP32 firnware: If data is not equal to " - 1", then
                // it is appended WITHOUT(!) newline in the next line, meaning, that the
                // next line is garbage only!
                if (!data.equals(" - 1")) {
                    ignoreNextLine=true;
                }
            }
            case "R" -> scannerService.handleRDSData(data);
            case "S" -> scannerService.handleStatus(data);
            case "T" -> {
                int frequencyKhz = Integer.parseInt(data);
                if (frequencyKhz != lastFrequencyKhz) {
                    LOGGER.info("T: Handle frequency changed for data=" + data);
                    scannerService.handleFrequencyChange();
                    scannerService.setFrequency(data);
                    lastFrequencyKhz = frequencyKhz;
                };
            }
            default -> LOGGER.warn("Unknown action=" + action + " for line=" + line);
        }
    }
}
