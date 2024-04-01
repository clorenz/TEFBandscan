package de.christophlorenz.tefbandscan.scanner;

import de.christophlorenz.tefbandscan.repository.BandscanRepository;
import de.christophlorenz.tefbandscan.repository.CSVBandscanRepository;
import de.christophlorenz.tefbandscan.repository.CommunicationRepository;
import de.christophlorenz.tefbandscan.service.ScannerService;
import de.christophlorenz.tefbandscan.service.ServiceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component
public class ScannerTask implements Runnable {

    private static final Logger LOGGER = LoggerFactory.getLogger(ScannerTask.class);

    private final BandscanRepository bandscanRepository;
    private final CommunicationRepository communicationRepository;
    private final ScannerService scannerService;

    public ScannerTask(CSVBandscanRepository csvBandscanRepository, @Qualifier("currentScanner") ScannerService scannerService,
                       @Qualifier("currentBandscanRepository") BandscanRepository bandscanRepository,
                       @Qualifier("currentCommunicationRepository") CommunicationRepository communicationRepository) {
        this.bandscanRepository = bandscanRepository;
        this.communicationRepository = communicationRepository;
        this.scannerService = scannerService;
    }

    @Override
    public void run() {
        try {
            scannerService.scan();
        } catch (ServiceException e) {
            LOGGER.error("Exception during scan: " + e, e);
        }
    }
}
