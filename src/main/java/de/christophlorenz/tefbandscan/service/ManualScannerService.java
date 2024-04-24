package de.christophlorenz.tefbandscan.service;

import de.christophlorenz.tefbandscan.config.ThresholdsConfig;
import de.christophlorenz.tefbandscan.model.*;
import de.christophlorenz.tefbandscan.repository.BandscanRepository;
import de.christophlorenz.tefbandscan.repository.CommunicationRepository;
import de.christophlorenz.tefbandscan.repository.RepositoryException;
import de.christophlorenz.tefbandscan.service.handler.LineHandler;
import de.christophlorenz.tefbandscan.service.handler.RDSHandler;
import de.christophlorenz.tefbandscan.service.handler.StatusHandler;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

@Service
public class ManualScannerService extends AbstractBaseScannerService implements ScannerService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ManualScannerService.class);

    private final ThresholdsConfig thresholdsConfig;

    public ManualScannerService(@Lazy @Qualifier("currentBandscanRepository") BandscanRepository bandscanRepository,
                                @Lazy @Qualifier("currentCommunicationRepository") CommunicationRepository communicationRepository,
                                RDSHandler rdsHandler,
                                StatusHandler statusHandler,
                                LineHandler lineHandler,
                                ThresholdsConfig thresholdsConfig) {
        super(bandscanRepository, communicationRepository, lineHandler, rdsHandler, statusHandler);
        this.thresholdsConfig = thresholdsConfig;
        lineHandler.setScannerService(this);
        statusHistory.setThresholds(thresholdsConfig.manual());
    }

    @Override
    public void scan() throws ServiceException {
        setBandwidth(Bandwidth.BANDWIDTH_133);

        boolean interrupted=false;
        while (!interrupted) {
            try {
                lineHandler.handle(communicationRepository.read());
                Status currentStatus = getCurrentStatus();
                statusHistory.setCurrentStatus(currentStatus);
                LogQuality logQuality = isLoggable(currentStatus, thresholdsConfig.manual());
                if (logQuality != LogQuality.NOP) {
                    logged=true;
                    // We can log!
                    Pair<BandscanEntry, Boolean> logResult = generateLog();
                    LOGGER.info("Logged " + logResult);
                    if (logQuality == LogQuality.STANDARD) {
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
        logged=false;
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

    protected Logger getLogger() {
        return LOGGER;
    }
}
