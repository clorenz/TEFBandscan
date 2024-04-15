package de.christophlorenz.tefbandscan.service;

import de.christophlorenz.tefbandscan.model.Bandwidth;
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
public class AutoScannerService extends AbstractBaseScannerService implements ScannerService {

    private static final Logger LOGGER = LoggerFactory.getLogger(AutoScannerService.class);
    private static final long TIMEOUT_MILLIS = 30000;// 30 seconds
    private static final int BAND_END=108000;

    private Status currentStatus;
    private long lastFrequencyChangeTime=0;

    public AutoScannerService(
            @Lazy @Qualifier("currentBandscanRepository") BandscanRepository bandscanRepository,
            @Lazy @Qualifier("currentCommunicationRepository") CommunicationRepository communicationRepository,
            RDSHandler rdsHandler,
            StatusHandler statusHandler,
            LineHandler lineHandler) {
        super(bandscanRepository, communicationRepository, lineHandler, rdsHandler, statusHandler);
        lineHandler.setScannerService(this);
    }

    @Override
    public void scan() throws ServiceException {
        setBandwidth(Bandwidth.BANDWIDTH_114);
        int frequency=87500;
        lastFrequencyChangeTime= System.currentTimeMillis();
        setFrequency(frequency);

        boolean interrupted=false;
        while (!interrupted) {
            try {
                lineHandler.handle(communicationRepository.read());
                statusHistory.setCurrentStatus(getCurrentStatus());
                if (isTimeout() || hasStabilized()) {
                    if (isValidEntry()) {
                        generateLog();
                    }
                    if (frequency >= BAND_END) {
                        interrupted=true;
                    } else {
                        frequency += 100;
                        setFrequency(frequency);
                    }
                }
            } catch (RepositoryException e) {
                LOGGER.error("Error: " + e, e);
                interrupted = true;
            }
        }
    }

    private boolean isValidEntry() {
        return (statusHistory.getAverageSignal()>15 && statusHistory.getAverageGGI()<20 && statusHistory.getAverageSnr()>=15);
    }

    @Override
    public void handleFrequencyChange() {
        statusHandler.reset();
        statusHistory.reset();
        rdsHandler.reset();
    }

    private void setFrequency(int frequency) throws ServiceException {
        try {
            communicationRepository.write("T" + frequency);
            lastFrequencyChangeTime = System.currentTimeMillis();
            statusHistory.reset();
            statusHandler.reset();
            rdsHandler.reset();
        } catch (RepositoryException e) {
            throw new ServiceException("Cannot set start frequency: " +e, e);
        }
    }

    private boolean isTimeout() {
        return (System.currentTimeMillis()-lastFrequencyChangeTime >= TIMEOUT_MILLIS);
    }

    private boolean hasStabilized() {
        return (statusHandler.getCurrentFrequency() != null) && statusHistory.isStable();
    }

    protected Logger getLogger() {
        return LOGGER;
    }
}
