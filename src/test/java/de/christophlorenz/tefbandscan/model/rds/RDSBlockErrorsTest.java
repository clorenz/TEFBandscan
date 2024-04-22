package de.christophlorenz.tefbandscan.model.rds;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("The RDS Block errors")
class RDSBlockErrorsTest {

    @DisplayName("are zero for a null input string")
    @Test
    public void allZeroForNullInput() {
        RDSBlockErrors rdsBlockErrors = new RDSBlockErrors(null);
        assertThat(rdsBlockErrors.getErrorsA()).isEqualTo(0);
        assertThat(rdsBlockErrors.getErrorsB()).isEqualTo(0);
        assertThat(rdsBlockErrors.getErrorsC()).isEqualTo(0);
        assertThat(rdsBlockErrors.getErrorsD()).isEqualTo(0);
    }

    @DisplayName("are zero for an empty input string")
    @Test
    public void allZeroForEmptyInput() {
        RDSBlockErrors rdsBlockErrors = new RDSBlockErrors("");
        assertThat(rdsBlockErrors.getErrorsA()).isEqualTo(0);
        assertThat(rdsBlockErrors.getErrorsB()).isEqualTo(0);
        assertThat(rdsBlockErrors.getErrorsC()).isEqualTo(0);
        assertThat(rdsBlockErrors.getErrorsD()).isEqualTo(0);
    }

    @DisplayName("can be calculated")
    @ParameterizedTest
    @CsvSource(value = { "00|0|0|0|0","01|0|0|0|1", "09|0|0|2|1","2B|0|2|2|3","35|0|3|1|1","FF|3|3|3|3"}, delimiter = '|')
    public void calculate(String hexString, int expectedA, int expectedB, int expectedC, int expectedD) {
        RDSBlockErrors actual = new RDSBlockErrors(hexString);
        assertThat(actual.getErrorsA()).isEqualTo(expectedA);
        assertThat(actual.getErrorsB()).isEqualTo(expectedB);
        assertThat(actual.getErrorsC()).isEqualTo(expectedC);
        assertThat(actual.getErrorsD()).isEqualTo(expectedD);
    }

}