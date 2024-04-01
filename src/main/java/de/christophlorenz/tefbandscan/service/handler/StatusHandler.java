package de.christophlorenz.tefbandscan.service.handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class StatusHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(StatusHandler.class);

    private Integer frequencyKhz;
    private Boolean stereo;
    private Float signalStrength;
    private Integer bandwidth;
    private Integer cci;

    public void handleStatus(String statusContents) {
        stereo = extractStereoFlag(statusContents);
        signalStrength = extractSignalStrength(statusContents.substring(1));
        if (signalStrength != null ) {
            signalStrength = signalStrength -11.25f + 0.35f;     // don't know, wny...
        }
        bandwidth = extractBandwidth(statusContents.substring(1));
        cci = extractCCI(statusContents.substring(1));
    }

    public void handleFrequency(String frequencyInKHz) {
        frequencyKhz = Integer.parseInt(frequencyInKHz.replaceFirst("\\D.*$",""));
    }

    private boolean extractStereoFlag(String statusContents) {
        return "s".equalsIgnoreCase(statusContents.substring(0, 1));
    }

    private Float extractSignalStrength(String statusContents) {
        String[] dataParts = statusContents.split(",");
        try {
            return Float.parseFloat(dataParts[0]);
        } catch (Exception e) {
            LOGGER.error("Got invalid status contents for signalStength='" + statusContents + "': "+ e);
        }
        return null;
    }

    private Integer extractBandwidth(String statusContents) {
        String[] dataParts = statusContents.split(",");
        if (dataParts.length > 3) {
            return Integer.parseInt(dataParts[3]);
        } else {
            return null;
        }
    }

    private Integer extractCCI(String statusContents) {
        String[] dataParts = statusContents.split(",");
        if (dataParts.length > 1) {
            try {
                return Integer.parseInt(dataParts[1]);
            } catch (Exception e) {
                LOGGER.error("Got invalid status contents for CCI='" + statusContents + "': "+ e);
            }
            return null;
        }
        return null;
    }

    public void reset() {
        frequencyKhz = null;
        stereo = null;
        signalStrength = null;
        bandwidth = null;
    }

    @Override
    public String toString() {
        return "StatusHandler{" +
                "frequency=" + (frequencyKhz != null ? ((float)frequencyKhz/1000f) : "") +
                ", stereo=" + stereo +
                ", signalStrength=" + signalStrength +
                ", CCI=" + cci +
                ", bandwidth=" + bandwidth +
                '}';
    }

    public Integer getCurrentFrequency() {
        return frequencyKhz;
    }

    public Float getSignalStrength() {
        return signalStrength;
    }

    public Integer getBandwidth() {
        return bandwidth;
    }

    public Integer getCci() {
        return cci;
    }

    public void handleFrequency(int frequencyInKHz) {
        this.frequencyKhz = frequencyInKHz;
    }
}
