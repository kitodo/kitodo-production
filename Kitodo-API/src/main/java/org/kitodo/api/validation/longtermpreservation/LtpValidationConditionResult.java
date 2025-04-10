package org.kitodo.api.validation.longtermpreservation;

import java.util.Objects;

public class LtpValidationConditionResult {
    
    boolean passed;

    LtpValidationConditionError error;

    String value;

    public LtpValidationConditionResult(boolean passed, LtpValidationConditionError error, String value) {
        this.passed = passed;
        this.error = error;
        this.value = value;
    }

    public boolean getPassed() {
        return passed;
    }

    public LtpValidationConditionError getError() {
        return error;
    }

    public String getValue() {
        return value;
    }

    public String toString() {
        String errorName = Objects.nonNull(error) ? error.name() : "none";
        return "[LtpValidationConditionResult passed=" + passed + " error=" + errorName + " value='" + value + "']";
    }
}
