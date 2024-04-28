package de.christophlorenz.tefbandscan.model;

import de.christophlorenz.tefbandscan.config.ThresholdsConfig;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class StatusHistory {

    private static final Logger LOGGER = LoggerFactory.getLogger(StatusHistory.class);
    private List<Status> statuses = new ArrayList<>();
    private ThresholdsConfig.Thresholds thresholds;
    public static final int MAX_OFFSET = 25;

    private Pair<Float,Float> cci = null;
    private Pair<Float,Float> modulation = null;
    private Pair<Float,Float> offset = null;
    private Pair<Float,Float> rdsErrors =  null;
    private Pair<Float,Float> signal = null;
    private Pair<Float,Float> snr = null;

    public void setThresholds(ThresholdsConfig.Thresholds thresholds) {
        this.thresholds = thresholds;
        LOGGER.info("Setting thresholds to " + thresholds);
    }

    public void setCurrentStatus(Status currentStatus) {
        statuses.add(0, currentStatus);
        if (statuses.size() > thresholds.samples()) {
            statuses.remove(statuses.size() -1);
        }

        if (hasEnoughData()) {
            // Do the calculations

            // Signal
            signal =
                    calculateMeanAndStandardDeviation(statuses.stream().map(Status::signal)
                            .filter(Objects::nonNull)
                            .toArray(Float[]::new));

            // CCI
            cci =
                    calculateMeanAndStandardDeviation(statuses.stream().map(Status::cci)
                            .filter(Objects::nonNull)
                            .map(Integer::floatValue)
                            .toArray(Float[]::new));

            // SNR
            snr =
                    calculateMeanAndStandardDeviation(statuses.stream().map(Status::snr)
                            .filter(Objects::nonNull)
                            .map(Integer::floatValue)
                            .toArray(Float[]::new));

            // Offset
            offset =
                    calculateMeanAndStandardDeviation(statuses.stream().map(Status::offset)
                            .filter(Objects::nonNull)
                            .map(Integer::floatValue)
                            .toArray(Float[]::new));

            // RDS errors
            rdsErrors =
                    calculateMeanAndStandardDeviation(statuses.stream().map(Status::rdsErrors)
                            .filter(Objects::nonNull)
                            .map(Integer::floatValue)
                            .toArray(Float[]::new));

            // Modulation
            modulation =
                    calculateMeanAndStandardDeviation(statuses.stream().map(Status::modulation)
                            .filter(Objects::nonNull)
                            .map(Integer::floatValue)
                            .toArray(Float[]::new));
        }
    }

    public void reset() {
        statuses.clear();
        cci = null;
        offset = null;
        signal = null;
        snr = null;
        rdsErrors = null;
        modulation = null;
    }

    public boolean isStable() {
        // Standardabweichung von Signalstärke und Bandbreite berechnen
        if (!hasEnoughData()) {
            return false;
        }
        return /*(bandwidth.getRight() < 0.1) &&*/ (signal.getRight() < 2.0);
    }

    public boolean hasEnoughData() {
        return statuses.size() >= thresholds.samples();
    }

    public Integer getAverageSignal() {
        if (signal == null) {
            return null;
        }
        return signal.getLeft().intValue();
    }

    public Integer getAverageCCI() {
        if (cci == null) {
            return null;
        }

        return cci.getLeft().intValue();
    }

    public Integer getAverageSnr() {
        if (snr == null) {
            return null;
        }

        return snr.getLeft().intValue();
    }

    public Integer getAverageOffset() {
        if (offset == null) {
            return null;
        }

        return offset.getLeft().intValue();
    }

    public Integer getAverageRdsErrors() {
        if (rdsErrors == null) {
            return null;
        }

        return rdsErrors.getLeft().intValue();
    }

    public Integer getAverageModulation() {
        if (modulation == null) {
            return null;
        }

        return modulation.getLeft().intValue();
    }

    public boolean hasTrueModulation() {
        if (modulation == null) {
            return true;
        }

        if (modulation.getLeft() <= 55) {
            return true;
        }


        // If we have an average(!) modulation from more than 55kHz, we must
        // look at the CCI. Only when it's below 15, we have a true, very strong
        if (cci==null) {
            return true;
        }

        return cci.getLeft() <= 15;
    }

    public boolean isValidSignalStrength() {
        return (signal != null) && (signal.getLeft() >= thresholds.signal());
    }

    public boolean isValidCci() {
        return (cci != null) && (cci.getLeft() <= thresholds.cci());
    }

    public boolean isValidSnr() {
        return (snr != null) && (snr.getLeft() >= thresholds.snr());
    }

    // -----------------------------------------------------


    private static Pair<Float,Float> calculateMeanAndStandardDeviation(Float[] array) {
        // get the sum of array
        int length = 0;
        float sum = 0.0f;
        for (Float i : array) {
            if (i != null) {
                sum += i;
                length++;
            }
        }

        if (length==0) {
            return Pair.of(0f,0f);
        }

        // get the mean of array
        float mean = sum / length;

        // calculate the standard deviation
        float standardDeviation = 0.0f;
        for (float num : array) {
            standardDeviation += Math.pow(num - mean, 2);
        }

        return Pair.of(mean, (float) Math.sqrt(standardDeviation / length));
    }

    private static Pair<Float,Float> calculateMeanAndRange(Float[] array) {
        Float min = Float.MAX_VALUE;
        Float max = Float.MIN_VALUE;

        // get the sum of array
        int length = 0;
        float sum = 0.0f;
        for (Float i : array) {
            if (i != null) {
                sum += i;
                length++;
                if (i<min) {
                    min = i;
                }
                if (i>max) {
                    max = i;
                }
            }
        }

        if (length==0) {
            return Pair.of(0f,0f);
        }

        // get the mean of array
        float mean = sum / length;

        return Pair.of(mean, max-min);
    }

    private Pair<Integer,Integer> getAverageModulationAndPercentualRange() {
        if (statuses == null || statuses.isEmpty()) {
            return null;
        }
        try {
            Pair<Float, Float> offset =
                    calculateMeanAndRange(statuses.stream().map(Status::modulation)
                            .filter(Objects::nonNull)
                            .map(Integer::floatValue)
                            .toArray(Float[]::new));
            Float rangePercent = 100 * ((float)offset.getRight() / (float)offset.getLeft());

            return Pair.of(offset.getLeft().intValue(), rangePercent.intValue());
        } catch (Exception e) {
            // ignore
        }
        return null;
    }

    public boolean isValidEntry() {
        // if (getAverageSignal() == null ||
        //        getAverageSnr() == null ||
        //        getAverageCCI() == null) {
        //    return false;
        //}
        return (
                isValidSignalStrength()
                        && hasTrueModulation()
                        //&& isValidCci()
                        && isValidSnr()
                        && hasNoLargeOffset()
               );
    }

    public boolean hasNoLargeOffset() {
        return ( offset != null) && (offset.getLeft() <= MAX_OFFSET);
    }
}
