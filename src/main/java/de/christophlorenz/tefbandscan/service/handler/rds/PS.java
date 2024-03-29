package de.christophlorenz.tefbandscan.service.handler.rds;

import org.springframework.stereotype.Component;

@Component
public class PS extends AbstractRdsHandler {

    char[] ps;

    public PS() {
        ps = new char[8];
    }

    public void calculatePS(String rdsB, String rdsD) {
        int rdsBValue = Integer.parseInt(rdsB, 16);
        boolean isBlockA = calculateIsBlockA(rdsB);
        int position = rdsBValue % 4;
        char char1 = (char) Integer.parseInt(rdsD.substring(0,2), 16);
        char char2 = (char) Integer.parseInt(rdsD.substring(2,4), 16);
        ps[position*2] = char1;
        ps[position*2+1] = char2;
    }

    public void reset() {
        for (int i=0; i<ps.length; i++) {
            ps[i] = 0;
        }
    }

    public String getPs() {
        StringBuffer ret = new StringBuffer();
        for (int i=0; i<8; i++) {
            if (ps[i] != 0) {
                ret.append(ps[i]);
            }
        }
        if (ret.length()==8) {
            return ret.toString();
        }

        return "";
    }
}
