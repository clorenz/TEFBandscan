package de.christophlorenz.tefbandscan.service.handler.rds;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class PI {

    private static final Logger LOGGER = LoggerFactory.getLogger(PI.class);

    public String pi;
    public Integer piErrors;


    public void setPi(String rawPi) {
        if (rawPi == null) {
            reset();
            return;
        }

        int currentPiErrors=0;
        if (rawPi.contains("?")) {
            currentPiErrors = StringUtils.countMatches(rawPi, "?");
        }

        if (pi == null || currentPiErrors < piErrors) {
            pi = rawPi.replaceAll("\\?", "");
            LOGGER.info("PI=" + pi + " for " + rawPi);
            piErrors = currentPiErrors;
        } else {
            if (!rawPi.replaceAll("\\?","").equals(pi)) {
                LOGGER.info("Refused to decrease PI from " + pi + " to " + rawPi);
            }
        }
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
