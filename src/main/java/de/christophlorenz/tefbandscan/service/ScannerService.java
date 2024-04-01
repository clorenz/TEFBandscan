package de.christophlorenz.tefbandscan.service;

import de.christophlorenz.tefbandscan.model.Status;

public interface ScannerService {
    Status getCurrentStatus();

    void scan() throws ServiceException;

    void handlePI(String pi);

    void handleRDSData(String rds);

    void handleStatus(String status);

    void handleFrequencyChange();

    void setFrequency(String frequency);
}
