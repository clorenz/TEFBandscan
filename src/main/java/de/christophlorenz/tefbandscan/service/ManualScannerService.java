package de.christophlorenz.tefbandscan.service;

import de.christophlorenz.tefbandscan.model.Bandscan;
import de.christophlorenz.tefbandscan.model.BandscanEntry;
import de.christophlorenz.tefbandscan.model.Bandwidth;
import de.christophlorenz.tefbandscan.model.Status;
import de.christophlorenz.tefbandscan.repository.BandscanRepository;
import de.christophlorenz.tefbandscan.repository.CommunicationRepository;
import de.christophlorenz.tefbandscan.repository.RepositoryException;
import de.christophlorenz.tefbandscan.service.handler.LineHandler;
import de.christophlorenz.tefbandscan.service.handler.RDSHandler;
import de.christophlorenz.tefbandscan.service.handler.StatusHandler;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

@Service
public class ManualScannerService extends AbstractBaseScannerService implements ScannerService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ManualScannerService.class);

    public ManualScannerService(@Lazy @Qualifier("currentBandscanRepository") BandscanRepository bandscanRepository,
                                @Lazy @Qualifier("currentCommunicationRepository") CommunicationRepository communicationRepository,
                                RDSHandler rdsHandler,
                                StatusHandler statusHandler,
                                LineHandler lineHandler) {
        super(bandscanRepository, communicationRepository, lineHandler, rdsHandler, statusHandler);
        lineHandler.setScannerService(this);
    }

    boolean isLogged=false;
    BandscanEntry latestLog = null;
    boolean latestLogIsNewEntry = false;
    int lastRdsPsErrors=999;

    @Override
    public void scan() throws ServiceException {
        // Send bandwidth to 133kHz, which is optimal for RDS
        setBandwidth(Bandwidth.BANDWIDTH_133);

        boolean interrupted=false;
        while (!interrupted) {
            try {
                lineHandler.handle(communicationRepository.read());
                Status currentStatus = getCurrentStatus();
                statusHistory.setCurrentStatus(currentStatus);
                int currentRdsPsErrors = rdsHandler.getPsErrors();
                if ((!isLogged) && hasStabilized()) {
                    Pair<BandscanEntry, Boolean> logResult = generateLog();
                    latestLog = logResult.getLeft();
                    latestLogIsNewEntry = logResult.getRight();
                    isLogged=true;
                    blinkOnDevice();
                } else if (isLogged) {
                    // Let's see... Maybe we can improve the log...
                    if ( (latestLog.getRdsPi() == null && currentStatus.rdsPi() != null) ||
                            (latestLog.getRdsPi() != null && latestLog.getRdsPi().endsWith("?") && currentStatus.rdsPi() != null && !currentStatus.rdsPi().endsWith("?")))  {
                        LOGGER.info("Logging again, since PI was detected. Before: " + latestLog.getRdsPi() + ", now: " + currentStatus.rdsPi());
                        if (latestLogIsNewEntry) {
                            // We can remove the previous entry
                            bandscanRepository.removeEntry(latestLog);
                            LOGGER.info("Removed obsolete entry=" + latestLog);
                        }
                        Pair<BandscanEntry, Boolean> logResult = generateLog();
                        latestLog = logResult.getLeft();
                        latestLogIsNewEntry = logResult.getRight();
                        isLogged = true;
                        blinkOnDevice();
                    } else if ((latestLog.getRdsPs() == null && currentStatus.rdsPs() != null) ||
                            ( (lastRdsPsErrors > currentRdsPsErrors) && !StringUtils.equals(latestLog.getRdsPs(), currentStatus.rdsPs())) ) {
                        LOGGER.info("Logging again, since PS was detected or improved. Before: " + latestLog.getRdsPs() + " (Errors=" + lastRdsPsErrors + "), now: " + currentStatus.rdsPs() + " (Errors=" + currentRdsPsErrors + ")");
                        Pair<BandscanEntry, Boolean> logResult = generateLog();
                        latestLog = logResult.getLeft();
                        latestLogIsNewEntry = logResult.getRight();
                        lastRdsPsErrors = currentRdsPsErrors;
                        isLogged = true;
                        blinkOnDevice();
                    }
                }
            } catch (RepositoryException e) {
                LOGGER.error("Error: " + e, e);
                interrupted = true;
            }
        }
    }

    @Override
    public void handleFrequencyChange() {
        statusHandler.reset();
        statusHistory.reset();
        rdsHandler.reset();
        unBlinkOnDevice();
        isLogged=false;
    }

    private void blinkOnDevice()   {
        try {
            //communicationRepository.write("Q100");
            //communicationRepository.read();
            //communicationRepository.read();
            communicationRepository.write("L");
        } catch (RepositoryException e) {
            LOGGER.warn("Cannot blink on device: " + e);
        }
    }

    private void unBlinkOnDevice() {
        try {
            //communicationRepository.write("Q0");
            //communicationRepository.read();
            communicationRepository.write("x");
            LOGGER.info("Unblinked");
        } catch (RepositoryException e) {
            LOGGER.warn("Cannot unblink on device: " + e);
        }
    }

    private boolean hasStabilized() {
        return (statusHandler.getCurrentFrequency() != null) && statusHistory.isStable();
    }

    protected Logger getLogger() {
        return LOGGER;
    }
}
