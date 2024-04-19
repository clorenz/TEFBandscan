package de.christophlorenz.tefbandscan.service.handler.rds;

import org.springframework.stereotype.Component;

@Component
public class PS extends AbstractRdsHandler {

    char[] ps;
    Integer[] psErrorRate;

    public PS() {
        ps = new char[8];
        psErrorRate = new Integer[8];
    }

    public void calculatePS(String rdsB, String rdsD, int errorRate) {
        int rdsBValue = Integer.parseInt(rdsB, 16);
        int position = rdsBValue % 4;
        char char1 = (char) Integer.parseInt(rdsD.substring(0,2), 16);
        char char2 = (char) Integer.parseInt(rdsD.substring(2,4), 16);

        // Overwrite only when it was either empty, of the error rate was lower than last time
        if (psErrorRate[position*2] == null || psErrorRate[position*2] > errorRate) {
            ps[position * 2] = char1;
            ps[position * 2 + 1] = char2;
            psErrorRate[position * 2] = errorRate;
            psErrorRate[position * 2 + 1] = errorRate;
        }
    }

    public void reset() {
        for (int i=0; i<ps.length; i++) {
            ps[i] = 0;
            psErrorRate[i] = null;
        }
    }

    public String getPs() {
        StringBuffer ret = new StringBuffer();
        for (int i=0; i<8; i++) {
            if (ps[i] != 0) {
                ret.append(ps[i]);
            }
        }
        if (ret.length()==8 && !ret.toString().isBlank()) {
            return ret.toString();
        }

        return null;
    }

    public int getPsErrors() {
        int sum=0;
        for (int i=0; i<8; i++) {
            if (psErrorRate[i] == null) {
                sum += 100;
            } else {
                sum += psErrorRate[i];
            }
        }
        return sum;
    }
}
