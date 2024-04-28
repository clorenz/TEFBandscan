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
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class AutoScannerService extends AbstractBaseScannerService implements ScannerService {

    private static final Logger LOGGER = LoggerFactory.getLogger(AutoScannerService.class);
    private static final long TIMEOUT_MILLIS = 30000;// 30 seconds
    private static final int BAND_START = 87500;
    private static final int BAND_END=108000;

    private Status currentStatus;
    private final ThresholdsConfig.Thresholds thresholds;
    private long lastFrequencyChangeTime=0;


    public AutoScannerService(
            @Lazy @Qualifier("currentBandscanRepository") BandscanRepository bandscanRepository,
            @Lazy @Qualifier("currentCommunicationRepository") CommunicationRepository communicationRepository,
            RDSHandler rdsHandler,
            StatusHandler statusHandler,
            LineHandler lineHandler,
            ThresholdsConfig thresholdsConfig) {
        super(bandscanRepository, communicationRepository, lineHandler, rdsHandler, statusHandler);
        lineHandler.setScannerService(this);
        this.thresholds = thresholdsConfig.auto();
        statusHistory.setThresholds(thresholds);
    }

    @Override
    public void scan() throws ServiceException {
        scanStart = new Date();
        setBandwidth(Bandwidth.BANDWIDTH_133);
        int frequency=87500;
        lastFrequencyChangeTime= System.currentTimeMillis();
        setFrequency(frequency);
        handleFrequencyChange();

        boolean interrupted=false;
        while (!interrupted) {
            try {
                lineHandler.handle(communicationRepository.read());
                Status currentStatus = getCurrentStatus();
                statusHistory.setCurrentStatus(currentStatus);
                LogQuality logQuality = LogQuality.NOP;
                if (statusHistory.isValidEntry()) {
                    logQuality = isLoggable(currentStatus, thresholds);
                }
                if (logQuality != LogQuality.NOP) {
                    logged=true;
                    // We can log!
                    Pair<BandscanEntry, Boolean> logResult = generateLog();
                    LOGGER.info("Logged " + logResult);
                    if (isPerfectLog(logResult.getLeft())) {
                        LOGGER.info("Perfect log. Switch to next frequency");
                        frequency = nextFrequency(frequency);
                    } else {
                        LOGGER.info("Not yet perfect log. Allowing more attempts");
                    }
                } else {
                    if (definityNoValidSignal()) {
                        LOGGER.info("There's definitly no valid signal. Going on to next frequency.");
                        frequency = nextFrequency(frequency);
                    } else if (isTimeout()) {
                        LOGGER.info("Timeout after " + TIMEOUT_MILLIS + "ms.");
                        frequency = nextFrequency(frequency);
                    }
                }
            } catch (RepositoryException e) {
                LOGGER.error("Error: " + e, e);
                interrupted = true;
            }
        }
    }

    private boolean definityNoValidSignal() {
        if (statusHistory.hasEnoughData()) {
            if (statusHistory.getAverageOffset() != null && Math.abs(statusHistory.getAverageOffset()) >= StatusHistory.MAX_OFFSET) {
                LOGGER.info("Detected average offset of " + statusHistory.getAverageOffset() + "kHz.");
                return true;
            }

            if (statusHistory.getAverageSnr() < thresholds.snr()) {
                LOGGER.info("Average S/N=" + statusHistory.getAverageSnr() + " is below threshold=" + thresholds.snr());
                return true;
            }

            if (!statusHistory.hasTrueModulation()) {
                LOGGER.info("Modulation too high average: " + statusHistory.getAverageModulation());
                return true;
            }
        }
        return false;
    }

    private boolean isPerfectLog(BandscanEntry bandscanEntry) {
        return Integer.valueOf(0).equals(bandscanEntry.getPsErrors())
                && Integer.valueOf(0).equals(bandscanEntry.getRdsErrors());
    }

    private int nextFrequency(int frequency) throws ServiceException {
        if (frequency >= BAND_END) {
            frequency = BAND_START;
            LOGGER.info("Restarting automatic bandscan at " + BAND_START);
        } else {
            frequency += 100;
            LOGGER.info("Increasing frequency to " + frequency);
        }
        setFrequency(frequency);
        handleFrequencyChange();
        return frequency;
    }

    @Override
    public void handleFrequencyChange() {
        statusHandler.reset();
        statusHistory.reset();
        rdsHandler.reset();
        logged=false;
    }

    private void setFrequency(int frequency) throws ServiceException {
        try {
            handleFrequencyChange();
            communicationRepository.write("T" + frequency);
            //lineHandler.handle(communicationRepository.read());
            //Status currentStatus = getCurrentStatus();
            //statusHistory.setCurrentStatus(currentStatus);
            lastFrequencyChangeTime = System.currentTimeMillis();
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
