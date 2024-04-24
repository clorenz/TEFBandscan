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
    private Integer snr;
    private Integer offset;

    public void handleStatus(String statusContents) {
        stereo = extractStereoFlag(statusContents);
        signalStrength = extractSignalStrength(statusContents.substring(1));
        if (signalStrength != null ) {
            signalStrength = signalStrength -11.25f + 0.35f;     // don't know, wny...
        }
        bandwidth = extractBandwidth(statusContents.substring(1));
        cci = extractCCI(statusContents.substring(1));
        snr = extractSNR(statusContents.substring(1));
        offset = extractOffset(statusContents.substring(1));
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
            String statusSignalPart = dataParts[0];
            if (statusSignalPart.startsWith("-")) {
                return 0f;
            } else {
                return Float.parseFloat(statusSignalPart);
            }
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

    private Integer extractSNR(String statusContents) {
        String[] dataParts = statusContents.split(",");
        if (dataParts.length > 4) {
            return Integer.parseInt(dataParts[4]);
        } else {
            return null;
        }
    }

    private Integer extractOffset(String statusContents) {
        String[] dataParts = statusContents.split(",");
        if (dataParts.length > 5) {
            return Integer.parseInt(dataParts[5]);
        } else {
            return null;
        }
    }

    public void reset() {
        frequencyKhz = null;
        stereo = null;
        signalStrength = null;
        bandwidth = null;
        snr = null;
        offset = null;
    }

    @Override
    public String toString() {
        return "StatusHandler{" +
                "frequency=" + (frequencyKhz != null ? ((float)frequencyKhz/1000f) : "") +
                ", stereo=" + stereo +
                ", signalStrength=" + signalStrength +
                ", CCI=" + cci +
                ", bandwidth=" + bandwidth +
                ", snr=" + snr +
                ", offset=" + offset +
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

    public Integer getSnr() {
        return snr;
    }

    public Integer getOffset() {
        return offset;
    }

    public void handleFrequency(int frequencyInKHz) {
        this.frequencyKhz = frequencyInKHz;
    }
}
