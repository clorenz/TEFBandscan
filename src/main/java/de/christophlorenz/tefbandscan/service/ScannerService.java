package de.christophlorenz.tefbandscan.service;

import de.christophlorenz.tefbandscan.model.Status;
import de.christophlorenz.tefbandscan.model.StatusHistory;

import java.util.Date;

public interface ScannerService {
    Status getCurrentStatus();

    StatusHistory getStatusHistory();

    void scan() throws ServiceException;

    void handlePI(String pi);

    void handleRDSData(String rds);

    void handleStatus(String status);

    void handleFrequencyChange();

    void setFrequency(String frequency);

    void setCurrentBandWidth(String currentBandWidth);

    Date getScanStart();
}
