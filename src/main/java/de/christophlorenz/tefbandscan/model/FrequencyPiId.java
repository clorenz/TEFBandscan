package de.christophlorenz.tefbandscan.model;

import java.io.Serializable;
import java.util.Objects;

public class FrequencyPiId implements Serializable {
    private Integer frequencyKHz;
    private String rdsPi;

    public FrequencyPiId() {
    }

    public FrequencyPiId(Integer frequencyKHz, String rdsPi) {
        this.frequencyKHz = frequencyKHz;
        this.rdsPi = rdsPi;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FrequencyPiId that = (FrequencyPiId) o;
        return Objects.equals(frequencyKHz, that.frequencyKHz) && Objects.equals(rdsPi, that.rdsPi);
    }

    @Override
    public int hashCode() {
        return Objects.hash(frequencyKHz, rdsPi);
    }

    @Override
    public String toString() {
        return "FrequencyPiId{" +
                "frequencyKHz=" + frequencyKHz +
                ", rdsPi='" + rdsPi + '\'' +
                '}';
    }
}
