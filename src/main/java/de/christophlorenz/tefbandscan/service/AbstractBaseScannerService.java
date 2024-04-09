package de.christophlorenz.tefbandscan.service;

import de.christophlorenz.tefbandscan.model.BandscanEntry;
import de.christophlorenz.tefbandscan.model.Status;
import de.christophlorenz.tefbandscan.model.StatusHistory;
import de.christophlorenz.tefbandscan.repository.BandscanRepository;
import de.christophlorenz.tefbandscan.repository.CommunicationRepository;
import de.christophlorenz.tefbandscan.service.handler.LineHandler;
import de.christophlorenz.tefbandscan.service.handler.RDSHandler;
import de.christophlorenz.tefbandscan.service.handler.StatusHandler;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractBaseScannerService implements ScannerService {

    private static final Logger LOGGER = LoggerFactory.getLogger(AutoScannerService.class);

    protected final BandscanRepository bandscanRepository;
    protected final CommunicationRepository communicationRepository;
    protected final LineHandler lineHandler;
    protected final RDSHandler rdsHandler;
    protected final StatusHandler statusHandler;

    protected final StatusHistory statusHistory;

    public AbstractBaseScannerService(BandscanRepository bandscanRepository,
                                      CommunicationRepository communicationRepository,
                                      LineHandler lineHandler,
                                      RDSHandler rdsHandler,
                                      StatusHandler statusHandler) {
        this.bandscanRepository = bandscanRepository;
        this.communicationRepository = communicationRepository;
        this.lineHandler = lineHandler;
        this.rdsHandler = rdsHandler;
        this.statusHandler = statusHandler;
        statusHistory = new StatusHistory();
    }

    @Override
    public void handlePI(String pi) {
        rdsHandler.handlePI(pi);
    }

    @Override
    public void handleRDSData(String rds) {
        rdsHandler.handleRDSData(rds);
    }

    @Override
    public void handleStatus(String status) {
        statusHandler.handleStatus(status);
    }

    @Override
    public void setFrequency(String frequency) {
        if (frequency == null || frequency.isBlank()) {
            return;
        }

        int frequencyInKHz = Integer.parseInt(frequency);
        if (frequencyInKHz < 65000) {
            // Bug in firmware, sometimes, the last digit is missing!
            frequencyInKHz *= 10;
        }

        statusHandler.handleFrequency(frequencyInKHz);
    }

    public Pair<BandscanEntry,Boolean> generateLog() throws ServiceException {
        try {
            BandscanEntry bandscanEntry = new BandscanEntry(statusHandler.getCurrentFrequency(), rdsHandler.getPi(), rdsHandler.getPs(),
                    Math.round(statusHistory.getAverageSignal()),
                    statusHistory.getAverageGGI());
            boolean isNewEntry = bandscanRepository.addEntry(bandscanEntry);
            getLogger().info("Logged " + statusHandler.getCurrentFrequency() + "=" + bandscanEntry);
            return Pair.of(bandscanEntry, isNewEntry);
        } catch (Exception e) {
            throw new ServiceException("Cannot generate log: " + e, e);
        }
    }

    @Override
    public Status getCurrentStatus() {
        return new Status(
                statusHandler.getCurrentFrequency(),
                rdsHandler.getPi(),
                rdsHandler.getPs(),
                statusHandler.getSignalStrength(),
                statusHandler.getCci(),
                statusHandler.getBandwidth());
    }

    protected int psLength(String ps) {
        if (ps == null) {
            return 0;
        }
        return ps.replaceAll("\\s","").length();
    }

    protected Logger getLogger() {
        return LOGGER;
    }
}
