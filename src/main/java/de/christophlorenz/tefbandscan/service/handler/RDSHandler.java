package de.christophlorenz.tefbandscan.service.handler;

import de.christophlorenz.tefbandscan.service.handler.rds.PS;
import org.springframework.stereotype.Component;

@Component
public class RDSHandler {

    private final PS ps;

    public RDSHandler(PS ps) {
        this.ps = ps;
    }

    private String pi;

    public void handlePI(String line) {
        this.pi = line.substring(1);
    }

    public String getPi() {
        return pi;
    }

    public void handleRDSData(String line) {
        // RDS Block A == PI
        // hex characters 1-4 = RDS Block B
        String rdsB = line.substring(1, 5);
        // hex characters 5-8 = RDS Block C
        String rdsC = line.substring(5,9);
        // hexCharacters 9-12 = RDS Block D
        String rdsD = line.substring(9,13);
        // RDS Error rate = hexCharacters 13-14
        int rdsErrorRate = Integer.parseInt(line.substring(13,14));

        int groupType = calculateGroupType(rdsB);
        switch (groupType) {
            case 0 -> { if (rdsErrorRate==0 ) { ps.calculatePS(rdsB, rdsD); }}
        }
    }

    public void reset() {
        pi = null;
        ps.reset();
    }



    @Override
    public String toString() {
        return "RDSHandler{" +
                "PI='" + pi + '\'' +
                ",PS='" + ps.getPs() + '\'' +
                '}';
    }

    private int calculateGroupType(String rdsB) {
        return Integer.parseInt(rdsB.substring(0, 1), 16);
    }

    public String getPs() {
        return ps.getPs();
    }
}
