package de.christophlorenz.tefbandscan.service.handler.rds;

import de.christophlorenz.tefbandscan.model.rds.PSWithErrors;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Component
public class PS extends AbstractRdsHandler {

    PSWithErrors psWithErrors;

    char[] ps;
    Integer[] psErrorRate;

    public PS() {
        psWithErrors = new PSWithErrors();
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

        psWithErrors.setAtPosition(position*2, char1, errorRate);
        psWithErrors.setAtPosition(position*2+1, char2, errorRate);
    }

    public void reset() {
        for (int i=0; i<ps.length; i++) {
            ps[i] = 0;
            psErrorRate[i] = null;
        }
        psWithErrors.reset();
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

    @Override
    public String toString() {
        StringBuffer ret = new StringBuffer("PS{");
        for ( int i=0; i<8; i++) {
            ret.append(ps[i]);
            ret.append("(");
            ret.append(psErrorRate[i] != null ? ""+psErrorRate[i] : "-");
            ret.append(")");
            if (i<7) {
                ret.append(" ");
            }
        }
        ret.append("}");
        return ret.toString();
    }

    public PSWithErrors getPsWithErrors() {
        return psWithErrors;
    }
}
