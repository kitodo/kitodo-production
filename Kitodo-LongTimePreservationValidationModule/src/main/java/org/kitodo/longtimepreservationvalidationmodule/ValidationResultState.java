/*
 * (c) Kitodo. Key to digital objects e. V. <contact@kitodo.org>
 *
 * This file is part of the Kitodo project.
 *
 * It is licensed under GNU General Public License version 3 or later.
 *
 * For the full copyright and license information, please read the
 * GPL3-License.txt file that was distributed with this source code.
 */

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
                    default:
                        throw new IllegalStateException("Complete switch");
                }
            default:
                throw new IllegalStateException("Complete switch");
        }
    }

    private State state;

    ValidationResultState(State state) {
        this.state = state;
    }

    public State toState() {
        return state;
    }
}
