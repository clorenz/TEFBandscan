package de.christophlorenz.tefbandscan.service;

import de.christophlorenz.tefbandscan.model.Status;
import de.christophlorenz.tefbandscan.repository.BandscanRepository;
import de.christophlorenz.tefbandscan.repository.CommunicationRepository;
import de.christophlorenz.tefbandscan.repository.RepositoryException;
import de.christophlorenz.tefbandscan.service.handler.LineHandler;
import de.christophlorenz.tefbandscan.service.handler.RDSHandler;
import de.christophlorenz.tefbandscan.service.handler.StatusHandler;
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

    @Override
    public void scan() throws ServiceException {
        boolean interrupted=false;
        while (!interrupted) {
            try {
                lineHandler.handle(communicationRepository.read());
                statusHistory.setCurrentStatus(getCurrentStatus());
                if ((!isLogged) && hasStabilized()) {
                    generateLog();
                    isLogged=true;
                    blinkOnDevice();
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
            communicationRepository.write("Q100");
            communicationRepository.read();
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

    private boolean hasStabilized() {
        return (statusHandler.getCurrentFrequency() != null) && statusHistory.isStable();
    }
}
