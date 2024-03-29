package de.christophlorenz.tefbandscan.service.handler.rds;

public abstract class AbstractRdsHandler {

    protected boolean calculateIsBlockA(String rdsB) {
        int rdsBValue = Integer.parseInt(rdsB, 16);
        return (rdsBValue >>> 7) % 2 == 0;
    }
}
