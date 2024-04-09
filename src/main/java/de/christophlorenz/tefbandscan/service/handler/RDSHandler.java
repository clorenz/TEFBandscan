package de.christophlorenz.tefbandscan.service.handler;

import de.christophlorenz.tefbandscan.service.handler.rds.PS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class RDSHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(RDSHandler.class);

    private final PS ps;

    public RDSHandler(PS ps) {
        this.ps = ps;
    }

    private String pi;

    public void handlePI(String pi) {
        if (pi == null || pi.contains("???")) {
            return;
        }
        this.pi = pi;
    }

    public String getPi() {
        return (pi == null || pi.isBlank()) ? null : pi;
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
        int rdsErrorRate = Integer.parseInt(line.substring(12,13));

        int groupType = calculateGroupType(rdsB);
        int version= calculateVersion(rdsB);

        String group = groupType + (version==0?"A":"B");

        switch (group) {
            case "0A" -> {
                if (rdsErrorRate==0 ) {
                    ps.calculatePS(rdsB, rdsD);
                }
            }
        }
    }

    public void reset() {
        pi = null;
        ps.reset();
    }



    @Override
    public String toString() {
        return "RDSHandler{" +
                "PI='" + (pi != null ? pi : "") + '\'' +
                ",PS='" + ps.getPs() + '\'' +
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
}
