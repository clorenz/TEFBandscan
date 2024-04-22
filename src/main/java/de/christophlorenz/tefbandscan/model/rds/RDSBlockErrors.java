package de.christophlorenz.tefbandscan.model.rds;

public class RDSBlockErrors {

    int errorsA=0;
    int errorsB=0;
    int errorsC=0;
    int errorsD=0;

    public RDSBlockErrors(String hexData) {
        if (hexData == null || hexData.isBlank()) {
            return;
        }
        int errors = Integer.parseInt(hexData, 16);
        errorsA = (errors >> 6) % 4;
        errorsB = (errors >> 4) % 4;
        errorsC = (errors >> 2) % 4;
        errorsD = errors % 4;
    }

    public int getErrorsA() {
        return errorsA;
    }

    public boolean isValidA() {
        return errorsA < 3;
    }

    public int getErrorsB() {
        return errorsB;
    }

    public boolean isValidB() {
        return errorsB < 3;
    }

    public int getErrorsC() {
        return errorsC;
    }

    public boolean isValidC() {
        return errorsC < 3;
    }

    public int getErrorsD() {
        return errorsD;
    }

    public boolean isValidD() {
        return errorsD < 3;
    }

    @Override
    public String toString() {
        return "RDSBlockErrors{" +
                "errorsA=" + errorsA +
                ", errorsB=" + errorsB +
                ", errorsC=" + errorsC +
                ", errorsD=" + errorsD +
                '}';
    }
}
