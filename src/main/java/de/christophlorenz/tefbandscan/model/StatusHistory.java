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

    public void setThresholds(ThresholdsConfig.Thresholds thresholds) {
        this.thresholds = thresholds;
        LOGGER.info("Setting thresholds to " + thresholds);
    }

    public void setCurrentStatus(Status currentStatus) {
        statuses.add(0, currentStatus);
        if (statuses.size() > thresholds.samples()) {
            statuses.remove(statuses.size() -1);
        }
    }

    public void reset() {
        statuses.clear();
    }

    public boolean isStable() {
        // Standardabweichung von Signalstärke und Bandbreite berechnen
        if (statuses.size() < thresholds.samples()) {
            return false;
        }


        /*
        Pair<Float,Float> bandwidth =
                calculateMeanAndStandardDeviation(Arrays.stream(statuses).map(Status::bandwidth)
                        .filter(Objects::nonNull)
                        .filter(b -> b>150)
                        .map(Float::valueOf)
                        .toArray(Float[]::new));

         */

        Pair<Float,Float> signal =
                calculateMeanAndStandardDeviation(statuses.stream().map(Status::signal)
                        .filter(Objects::nonNull)
                        .toArray(Float[]::new));

        return /*(bandwidth.getRight() < 0.1) &&*/ (signal.getRight() < 2.0);
    }


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

    public boolean hasEnoughData() {
        return statuses.size() >= thresholds.samples();
    }

    public Integer getAverageSignal() {
        if (statuses == null || statuses.isEmpty()) {
            return null;
        }
        try {
            Pair<Float, Float> signal =
                    calculateMeanAndStandardDeviation(statuses.stream().map(Status::signal)
                            .filter(Objects::nonNull)
                            .toArray(Float[]::new));
            return signal.getLeft().intValue();
        } catch (Exception e) {
            // LOGGER.warn("Average signal: " + e.getMessage() + " on statuses=" + Arrays.asList(statuses));
        }
        return null;
    }

    public Integer getAverageCCI() {
        if (statuses == null || statuses.isEmpty()) {
            return null;
        }
        try {
            Pair<Float, Float> cci =
                    calculateMeanAndStandardDeviation(statuses.stream().map(Status::cci)
                            .filter(Objects::nonNull)
                            .map(Integer::floatValue)
                            .toArray(Float[]::new));
            return cci.getLeft().intValue();
        } catch (Exception e) {
            // ignore
        }
        return null;
    }

    public Integer getAverageSnr() {
        if (statuses == null || statuses.isEmpty()) {
            return null;
        }
        try {
            Pair<Float, Float> snr =
                    calculateMeanAndStandardDeviation(statuses.stream().map(Status::snr)
                            .filter(Objects::nonNull)
                            .map(Integer::floatValue)
                            .toArray(Float[]::new));
            Integer ret = snr.getLeft().intValue();
            if (ret != null && ret > 0) {
                return ret;
            } else {
                return null;
            }
        } catch (Exception e) {
            // ignore
        }
        return null;
    }

    public int getAverageRdsErrors() {
        Pair<Float,Float> rdsErrors =
            calculateMeanAndStandardDeviation(statuses.stream().map(Status::rdsErrors)
                .filter(Objects::nonNull)
                .map(Integer::floatValue)
                .toArray(Float[]::new));
        return rdsErrors.getLeft().intValue();
    }

    public boolean isValidEntry() {
        //if (getAverageSignal() == null ||
        //        getAverageSnr() == null ||
        //        getAverageCCI() == null) {
        //    return false;
        //}
        return (
                isValidSignalStrength()
                        && isValidCci()
                        && isValidSnr());
    }

    public boolean isValidSignalStrength() {
        return (getAverageSignal() != null && getAverageSignal() >= thresholds.signal());
    }

    public boolean isValidCci() {
        return (getAverageCCI() != null && getAverageCCI() <= thresholds.cci());
    }

    public boolean isValidSnr() {
        return (getAverageSnr() != null && getAverageSnr() >= thresholds.snr());
    }
}
