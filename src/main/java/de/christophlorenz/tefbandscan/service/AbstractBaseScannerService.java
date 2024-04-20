package de.christophlorenz.tefbandscan.service;

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

import java.util.Objects;

public abstract class AbstractBaseScannerService implements ScannerService {

    private static final Logger LOGGER = LoggerFactory.getLogger(AutoScannerService.class);

    protected final BandscanRepository bandscanRepository;
    protected final CommunicationRepository communicationRepository;
    protected final LineHandler lineHandler;
    protected final RDSHandler rdsHandler;
    protected final StatusHandler statusHandler;

    protected final StatusHistory statusHistory;

    private Status lastStatus;


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
            BandscanEntry bandscanEntry = new BandscanEntry(
                    statusHandler.getCurrentFrequency(),
                    rdsHandler.getPi(),
                    rdsHandler.getPs(),
                    rdsHandler.getPsErrors(),
                    statusHistory.getAverageRdsErrors(),
                    Math.round(statusHistory.getAverageSignal()),
                    statusHistory.getAverageCCI(),
                    statusHistory.getAverageSnr());
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
                rdsHandler.getPiErrors(),
                rdsHandler.getPs(),
                rdsHandler.getPsErrors(),
                rdsHandler.getPsWithErrors(),
                rdsHandler.getRdsErrorRate(),
                statusHandler.getSignalStrength(),
                statusHandler.getCci(),
                statusHandler.getBandwidth(),
                statusHandler.getSnr());
    }

    protected int psLength(String ps) {
        if (ps == null) {
            return 0;
        }
        return ps.replaceAll("\\s","").length();
    }

    protected void setBandwidth(Bandwidth bandwidth) throws ServiceException {
        try {
            communicationRepository.write("W" + bandwidth.getkHz());
            LOGGER.info("Setting bandwidth to W" + bandwidth.getkHz() + " resulted in=" + communicationRepository.read());
        } catch (RepositoryException e) {
            throw new ServiceException("Cannot set bandwidth to W" + bandwidth.getkHz() + ": " + e, e);
        }
    }

    protected Logger getLogger() {
        return LOGGER;
    }

    protected LogQuality isLoggable(Status currentStatus) {
        if ((statusHandler.getCurrentFrequency() == null) || (!statusHistory.isStable())) {
            return LogQuality.NOP;
        }

        BandscanEntry existingBandscanEntry = bandscanRepository.getByFrequencyAndPI(currentStatus.frequency(), currentStatus.rdsPi());
        if (existingBandscanEntry == null) {
            LOGGER.info("No previous bandscan entry for QRG and PI detected. Can log anyways");
            return LogQuality.STANDARD;
        }

        if (!Objects.equals(existingBandscanEntry.getRdsPs(), currentStatus.rdsPs()) &&
                currentStatus.psErrors() != null &&
                currentStatus.rdsPs() != null &&
                (existingBandscanEntry.getPsErrors() == null ||
                        (existingBandscanEntry.getPsErrors() > currentStatus.psErrors()))) {
            LOGGER.info("Detected positive RDS PS change (" + existingBandscanEntry.getRdsPs() + "->" + currentStatus.rdsPs() + "). Can log.");
                    return LogQuality.STANDARD;
        }


        if (Objects.equals(existingBandscanEntry.getRdsPs(), currentStatus.rdsPs()) &&
                currentStatus.psErrors() != null &&
                ((existingBandscanEntry.getPsErrors() == null) ||
                existingBandscanEntry.getPsErrors() > currentStatus.psErrors())) {
            LOGGER.info("Detected RDS PS error decrease (" + existingBandscanEntry.getPsErrors() + "->" + currentStatus.psErrors() + "). Can log silently.");
            return LogQuality.SILENT;
        }

        if ( (existingBandscanEntry.getSignalStrength() < statusHistory.getAverageSignal().intValue()) &&
                Objects.equals(existingBandscanEntry.getRdsPs(), currentStatus.rdsPs())){
            LOGGER.info("Detected signal increase (" + existingBandscanEntry.getSignalStrength() + "->" + statusHistory.getAverageSignal() + "). Can log silently.");
            return LogQuality.SILENT;
        }

        if ( (existingBandscanEntry.getSnr() == null || existingBandscanEntry.getSnr() < statusHistory.getAverageSnr()) &&
                (Objects.equals(existingBandscanEntry.getRdsPs(), currentStatus.rdsPs()))) {
            LOGGER.info("Detected S/N increase (" + existingBandscanEntry.getSnr() + "->" + statusHistory.getAverageSnr() + "). Can log silently.");
            return LogQuality.SILENT;
        }

        if ( (existingBandscanEntry.getCci() == 0 || existingBandscanEntry.getCci() > statusHistory.getAverageCCI()) &&
                (Objects.equals(existingBandscanEntry.getRdsPs(), currentStatus.rdsPs())) ){
            LOGGER.info("Detected CCI decrease (" + existingBandscanEntry.getCci() + "->" + statusHistory.getAverageCCI() + "). Can log silently.");
            return LogQuality.SILENT;
        }

        if (Objects.equals(existingBandscanEntry.getRdsPs(), currentStatus.rdsPs())) {
            return LogQuality.NOP;
        }

        return LogQuality.NOP;
    }

}
