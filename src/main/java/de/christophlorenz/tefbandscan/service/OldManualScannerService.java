package de.christophlorenz.tefbandscan.service;

import de.christophlorenz.tefbandscan.model.Status;
import de.christophlorenz.tefbandscan.repository.BandscanRepository;
import de.christophlorenz.tefbandscan.repository.CommunicationRepository;
import de.christophlorenz.tefbandscan.repository.RepositoryException;
import de.christophlorenz.tefbandscan.service.handler.RDSHandler;
import de.christophlorenz.tefbandscan.service.handler.StatusHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

public class OldManualScannerService extends AbstractBaseScannerService implements ScannerService {

    private static final Logger LOGGER = LoggerFactory.getLogger(OldManualScannerService.class);
    private static final int LOG_INTERVAL = 1000;           // 1 second
    private static final long TIMEOUT_MILLIS = 2000;         // 2 seconds
    private static final long TIMEOUT_MILLIS_RDS = 30000;   // 30 seconds - should be enough for stable PS decoding



    private long lastAutoTimestamp=0;
    private boolean isLogged=false;

    public OldManualScannerService(RDSHandler rdsHandler, StatusHandler statusHandler,
                                   @Lazy @Qualifier("currentCommunicationRepository") CommunicationRepository communicationRepository,
                                   @Lazy @Qualifier("currentBandscanRepository") BandscanRepository bandscanRepository) {
        super(bandscanRepository, communicationRepository, null, rdsHandler, statusHandler);
    }

    public void scan() throws ServiceException {
        boolean auto=false;
        Integer startFrequency = null;
        if (auto) {
            startFrequency = 87500;
            try {
                communicationRepository.write("T" + startFrequency);     // TODO refactor this into an API!
                lastAutoTimestamp = System.currentTimeMillis();
            } catch (RepositoryException e) {
                throw new ServiceException("Cannot start automatic scan: " + e, e);
            }
        }


        boolean interrupted = false;
        long time = System.currentTimeMillis();
        String line;
        while (!interrupted) {
            try {
                if (!((line = communicationRepository.read()) != null)) break;
            } catch (RepositoryException e) {
                throw new ServiceException("Cannot read line: " + e, e);
            }
            if (line.isBlank()) {
                try {
                    Thread.sleep(1);
                } catch (InterruptedException e) {
                    interrupted = true;
                }
                continue;
            }
            //LOGGER.debug(line);
            handleLine(line);
            if (!isLogged) {
                if ((statusHistory.isStable() && statusHandler.getCurrentFrequency() != null) || isTimeout(lastAutoTimestamp, statusHandler.getSignalStrength())) {
                    if (!auto) {
                        LOGGER.info("Stable entry detected for " + statusHandler.getCurrentFrequency() + "(" + rdsHandler.getPi() + "). Will log. You can go on now");
                        blinkOnDevice();
                        generateLog();
                        rdsHandler.reset();
                        statusHandler.reset();
                        statusHistory.reset();
                        isLogged = true;
                    } else {
                        LOGGER.info("Stable entry detected. Continuing with next frequency");
                        generateLog();
                        if (startFrequency < 108000) {
                            startFrequency += 100;
                            try {
                                communicationRepository.write("T" + startFrequency);
                                lastAutoTimestamp = System.currentTimeMillis();
                            } catch (RepositoryException e) {
                                throw new ServiceException("Cannot set frequency to " + startFrequency + ": " + e, e);
                            }
                        } else {
                            LOGGER.info("Scan finished");
                        }
                    }
                }
            }
            if (!isLogged && System.currentTimeMillis() - time > LOG_INTERVAL) {
                time = System.currentTimeMillis();
                LOGGER.info("Status=" + statusHandler.toString());
                LOGGER.info("RDS=" + rdsHandler.toString());
            }

            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
                interrupted=true;
            }
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

    private void blinkOnDevice()   {
        try {
            communicationRepository.write("Q100");
            communicationRepository.read();
        } catch (RepositoryException e) {
            LOGGER.warn("Cannot blink on device: " + e);
        }
    }

    private void unBlinkOnDevice() {
        try {
            communicationRepository.write("Q0");
            communicationRepository.read();
            communicationRepository.write("x");
            LOGGER.info("Unblinked");
        } catch (RepositoryException e) {
            LOGGER.warn("Cannot unblink on device: " + e);
        }
    }

    private void handleLine(String line) {
        String action = line.substring(0,1).toUpperCase();
        switch (action) {
            case "0" -> {
                if (!"0OK".equalsIgnoreCase(line)) {
                    statusHandler.handleStatus(line.substring(1));
                } else {
                    LOGGER.info("Received OK");
                }
            }
            case "B" -> {
                if ("B0".equalsIgnoreCase(line)) {
                    LOGGER.info("Stereo toggled=0");
                } else {
                    LOGGER.info("Stereo toggled=1");
                }
            }
            case "M" -> {
                LOGGER.info(line);
                if (!"M0".equalsIgnoreCase(line)) {
                    handleFrequencyChange();
                    if (line.length() > 5) {
                        statusHandler.handleFrequency(line.substring(1));
                    } else {
                        LOGGER.info("Don't handle frequency for invalid line=" + line);
                    }
                }
            }
            case "P" -> rdsHandler.handlePI(line.substring(1));
            case "Q" -> LOGGER.info("Got Squelch confirmation=" + line);
            case "R" -> rdsHandler.handleRDSData(line.substring(1));
            case "S" -> statusHandler.handleStatus(line.substring(1));
            case "T" -> {
                LOGGER.info(line);
                handleFrequencyChange();
                if (line.length() > 5) {
                    statusHandler.handleFrequency(line.substring(1));
                } else {
                    statusHandler.handleFrequency(line.substring(1) + "0");      // Bug in TEF Firmware
                }
            }
            default -> LOGGER.warn("Unknown action=" + action + " for line=" + line);
        }

        statusHistory.setCurrentStatus(getCurrentStatus());
    }


    @Override
    public void handleFrequencyChange() {
        if (statusHistory.hasEnoughData()) {
            // Avoid logging during (fast) tuning
            try {
                bandscanRepository.addEntry(statusHandler.getCurrentFrequency(), rdsHandler.getPi(), rdsHandler.getPs(), rdsHandler.getRdsErrorRate(), Math.round(statusHistory.getAverageSignal()), Math.round(statusHistory.getAverageGGI()), null);
            } catch (RepositoryException e) {
                LOGGER.error("Cannot make log entry: " + e, e);
            }
        }
        rdsHandler.reset();
        statusHandler.reset();
        statusHistory.reset();
        if (isLogged) {
            isLogged = false;
            unBlinkOnDevice();
        }
    }


    public Status getCurrentStatus() {
        return new Status(statusHandler.getCurrentFrequency(), rdsHandler.getPi(), rdsHandler.getPs(), rdsHandler.getRdsErrorRate(), statusHandler.getSignalStrength(), statusHandler.getCci(), statusHandler.getBandwidth(), statusHandler.getSnr(), rdsHandler.getRdsErrorRate());
    }
}
