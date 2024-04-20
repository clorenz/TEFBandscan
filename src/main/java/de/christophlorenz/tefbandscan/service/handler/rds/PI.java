package de.christophlorenz.tefbandscan.service.handler.rds;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

@Component
public class PI {

    public String pi;
    public Integer piErrors;


    public void setPi(String rawPi) {
        if (rawPi == null) {
            reset();
            return;
        }

        if (rawPi.contains("?")) {
            piErrors = StringUtils.countMatches(rawPi, "?");
        } else {
            piErrors = 0;
        }
        pi = rawPi.replaceAll("\\?", "");
    }

    public String getPi() {
        return pi;
    }

    public Integer getPiErrors() {
        return piErrors;
    }

    public void reset() {
        pi = null;
        piErrors = null;
    }
}
