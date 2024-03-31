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

    public void handleStatus(String line) {
        String statusContents = line.substring(1);

        stereo = extractStereoFlag(statusContents);
        signalStrength = extractSignalStrength(statusContents);
        if (signalStrength != null ) {
            signalStrength = signalStrength -11.25f + 0.35f;     // don't know, wny...
        }
        bandwidth = extractBandwidth(statusContents);
    }

    public void handleFrequency(String line) {
        frequencyKhz = Integer.parseInt(line.substring(1).replaceFirst("\\D.*$",""));
    }

    private boolean extractStereoFlag(String statusContents) {
        return "s".equalsIgnoreCase(statusContents.substring(0, 1));
    }

    private Float extractSignalStrength(String statusContents) {
        String[] dataParts = statusContents.substring(1).split(",");
        try {
            return Float.parseFloat(dataParts[0]);
        } catch (Exception e) {
            LOGGER.error("Got invalid status contents='" + statusContents + "': "+ e);
        }
        return null;
    }

    private Integer extractBandwidth(String statusContents) {
        String[] dataParts = statusContents.substring(1).split(",");
        if (dataParts.length > 2) {
            return Integer.parseInt(dataParts[3]);
        } else {
            return null;
        }
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
}
