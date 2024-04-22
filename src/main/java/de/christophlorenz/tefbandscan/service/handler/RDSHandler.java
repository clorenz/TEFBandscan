package de.christophlorenz.tefbandscan.service.handler;

import de.christophlorenz.tefbandscan.model.rds.PSWithErrors;
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

    private int rdsErrorRate;

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
        rdsErrorRate = Integer.parseInt(line.substring(12,14), 16);

        // FIXME: This is a range of two bits per block: 2 Bits for A, 2 for B, 2 for C and 2 for C.
        // FIXME: Only value "3" is interesing, because it indicates broken data! Therefor, it is
        // wrong to treat tdsErrorRate as one integer!
        //
        RDSBlockErrors rdsBlockErrors = calculateRdsBlockErrors(line.substring(12,14));

        int groupType = calculateGroupType(rdsB);
        int version= calculateVersion(rdsB);

        String group = groupType + (version==0?"A":"B");

        switch (group) {
            case "0A" -> {
                // TODO: On high RDS errors, write empty PS fields; overwrite them only on low RDS errors
                // Maybe track for each character pair the RDS error value
                ps.calculatePS(rdsB, rdsD, rdsBlockErrors);
            }
        }
    }

    public void reset() {
        pi.reset();
        ps.reset();
        rdsErrorRate=0;
    }



    @Override
    public String toString() {
        return "RDSHandler{" +
                "PI=" + pi +
                ", PS=" + ps +
                ", errorRate=" + rdsErrorRate +
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
        return rdsErrorRate;
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
