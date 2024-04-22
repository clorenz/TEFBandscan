package de.christophlorenz.tefbandscan.model;

import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Objects;

public class StatusHistory {

    private static final Logger LOGGER = LoggerFactory.getLogger(StatusHistory.class);
    private static final int EVALUATION_LENGTH=200;

    private Status[] statuses = new Status[EVALUATION_LENGTH];
    private int statuspoints=0;

    public void setCurrentStatus(Status currentStatus) {
        // Shift existing statuses
        System.arraycopy(statuses, 0, statuses, 1, statuses.length-1);
        statuses[0] = currentStatus;
        statuspoints++;
    }

    public void reset() {
        statuses = new Status[EVALUATION_LENGTH];
        statuspoints=0;
    }

    public boolean isStable() {
        // Standardabweichung von Signalstärke und Bandbreite berechnen
        if (statuspoints < EVALUATION_LENGTH) {
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
                calculateMeanAndStandardDeviation(Arrays.stream(statuses).map(Status::signal)
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
        return statuspoints >= EVALUATION_LENGTH;
    }

    public Integer getAverageSignal() {
        if (statuses == null || statuses.length==0) {
            return null;
        }
        try {
            Pair<Float, Float> signal =
                    calculateMeanAndStandardDeviation(Arrays.stream(statuses).map(Status::signal)
                            .filter(Objects::nonNull)
                            .toArray(Float[]::new));
            return signal.getLeft().intValue();
        } catch (Exception e) {
            // LOGGER.warn("Average signal: " + e.getMessage() + " on statuses=" + Arrays.asList(statuses));
        }
        return null;
    }

    public Integer getAverageCCI() {
        if (statuses == null || statuses.length==0) {
            return null;
        }
        Pair<Float, Float> cci =
                calculateMeanAndStandardDeviation(Arrays.stream(statuses).map(Status::cci)
                        .map(Integer::floatValue)
                        .filter(Objects::nonNull)
                        .toArray(Float[]::new));
        return cci.getLeft().intValue();
    }

    public Integer getAverageSnr() {
        if (statuses == null || statuses.length==0) {
            return null;
        }
        Pair<Float,Float> snr =
                calculateMeanAndStandardDeviation(Arrays.stream(statuses).map(Status::snr)
                        .filter(Objects::nonNull)
                        .map(Integer::floatValue)
                        .toArray(Float[]::new));
        Integer ret = snr.getLeft().intValue();
        if (ret != null && ret > 0) {
            return ret;
        } else {
            return null;
        }
    }

    public int getAverageRdsErrors() {
        Pair<Float,Float> rdsErrors =
            calculateMeanAndStandardDeviation(Arrays.stream(statuses).map(Status::rdsErrors)
                .filter(Objects::nonNull)
                .map(Integer::floatValue)
                .toArray(Float[]::new));
        return rdsErrors.getLeft().intValue();
    }
}
