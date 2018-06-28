package org.kitodo.longtimepreservationvalidationmodule;

import edu.harvard.hul.ois.jhove.RepInfo;

import org.kitodo.api.validation.State;

public enum ValidationResultState {
    ERROR(State.ERROR),
    NOT_WELL_FORMED(State.ERROR),
    UNKNOWN(State.WARNING),
    VALID(State.SUCCESS),
    WELL_FORMED(State.SUCCESS),
    WELL_FORMED_BUT_NOT_VALID(State.WARNING);

    public static ValidationResultState valueOf(int wellFormed, int valid) {
        switch (wellFormed) {
            case RepInfo.UNDETERMINED:
                return UNKNOWN;
            case RepInfo.FALSE:
                return NOT_WELL_FORMED;
            case RepInfo.TRUE:
                switch (valid) {
                    case RepInfo.UNDETERMINED:
                        return WELL_FORMED;
                    case RepInfo.FALSE:
                        return WELL_FORMED_BUT_NOT_VALID;
                    case RepInfo.TRUE:
                        return VALID;
                }
        }
        throw new IllegalStateException("Complete switch");
    }

    private State s;

    private ValidationResultState(State s) {
        this.s = s;
    }

    public State toState() {
        return s;
    }
}
