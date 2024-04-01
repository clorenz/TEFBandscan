package de.christophlorenz.tefbandscan.service.handler;

import de.christophlorenz.tefbandscan.service.handler.rds.PS;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

@DisplayName("The RDS handler")
class RDSHandlerTest {

    private RDSHandler rdsHandler;

    @BeforeEach
    public void beforeEach() {
        PS psHandler = mock(PS.class);
        this.rdsHandler = new RDSHandler(psHandler);
    }

    @DisplayName("can calculate the versions A(=0) and B(=1)")
    @ParameterizedTest
    @CsvSource(value = { "FFFF|1", "EFFF|0"}, delimiter = '|')
    public void calculateVersion(String rdsB, int expectedVersion) {
        assertThat(rdsHandler.calculateVersion(rdsB)).isEqualTo(expectedVersion);
    }

}