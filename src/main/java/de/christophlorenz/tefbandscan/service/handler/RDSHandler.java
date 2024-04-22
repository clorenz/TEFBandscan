package de.christophlorenz.tefbandscan.service.handler;

import de.christophlorenz.tefbandscan.model.rds.PSWithErrors;
import de.christophlorenz.tefbandscan.model.rds.RDSBlockErrors;
import de.christophlorenz.tefbandscan.service.handler.rds.PI;
import de.christophlorenz.tefbandscan.service.handler.rds.PS;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class RDSHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(RDSHandler.class);

    private final PI pi;

    private final PS ps;

    private RDSBlockErrors rdsBlockErrors;

    public RDSHandler(PI pi, PS ps) {
        this.pi = pi;
        this.ps = ps;
    }

    public void handlePI(String rawPi) {
        pi.setPi(rawPi);
    }

    public String getPi() {
        return pi.getPi();
    }

    public void handleRDSData(String line) {
        // RDS Block A == PI
        // hex characters 1-4 = RDS Block B
        String rdsB = line.substring(0, 4);
        // hex characters 5-8 = RDS Block C
        String rdsC = line.substring(4,8);
        // hexCharacters 9-12 = RDS Block D
        String rdsD = line.substring(8,12);
        // RDS Error rate = hexCharacters 13-14

        rdsBlockErrors = new RDSBlockErrors(line.substring(12,14));

        int groupType = calculateGroupType(rdsB);
        int version= calculateVersion(rdsB);

        String group = groupType + (version==0?"A":"B");

        switch (group) {
            case "0A" -> ps.calculatePS(rdsB, rdsD, rdsBlockErrors);
            case "0B" -> LOGGER.info("Found group 0B");
        }
    }

    public void reset() {
        pi.reset();
        ps.reset();
        rdsBlockErrors = null;
    }



    @Override
    public String toString() {
        return "RDSHandler{" +
                "PI=" + pi +
                ", PS=" + ps +
                ", RDSBlockErrors=" + rdsBlockErrors +
                '}';
    }

    private int calculateGroupType(String rdsB) {
        return Integer.parseInt(rdsB.substring(0, 1), 16);
    }

    protected int calculateVersion(String rdsB) {
        return (Integer.parseInt(rdsB, 16) >> 12) % 2;
    }

    public String getPs() {
        return ps.getPs();
    }

    public Integer getRdsErrorRate() {
        if (rdsBlockErrors == null) {
            return null;
        }

        int errorBitSum =
            rdsBlockErrors.getErrorsA()
                + rdsBlockErrors.getErrorsB()
                + rdsBlockErrors.getErrorsC()
                + rdsBlockErrors.getErrorsD();

        // The maximum error bit sum can be 12   (4x3), which is calculated as 100%
        return Math.round((float)errorBitSum * 100f / 12f);
    }

    public Integer getPsErrors() {
        return ps.getPsErrors();
    }

    public Integer getPiErrors() {
        return pi.getPiErrors();
    }

    public PSWithErrors getPsWithErrors() {
        return ps.getPsWithErrors();
    }
}
