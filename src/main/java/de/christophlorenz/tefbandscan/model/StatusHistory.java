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

        Pair<Float,Float> bandwidth =
                calculateMeanAndStandardDeviation(Arrays.stream(statuses).map(Status::bandwidth)
                        .filter(Objects::nonNull)
                        .map(Float::valueOf)
                        .toArray(Float[]::new));

        Pair<Float,Float> signal =
                calculateMeanAndStandardDeviation(Arrays.stream(statuses).map(Status::signal)
                        .filter(Objects::nonNull)
                        .toArray(Float[]::new));

        return (bandwidth.getRight() < 0.1) && (signal.getRight() < 2.0);
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
}
