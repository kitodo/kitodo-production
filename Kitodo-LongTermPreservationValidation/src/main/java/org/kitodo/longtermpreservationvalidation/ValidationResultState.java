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

package org.kitodo.longtermpreservationvalidation;

import org.kitodo.api.validation.State;
import org.kitodo.config.KitodoConfig;

/**
 * The validation result state is used to map the two-valued result state of
 * JHove to the Kitodo validation interface.
 */
public class ValidationResultState {
    /**
     * Constant for the error case.
     */
    public static final ValidationResultState ERROR = new ValidationResultState() {
        @Override
        public State toState() {
            return State.ERROR;
        }
    };

    /**
     * Saves the value of well-formedness.
     */
    private TernaryValue wellFormed;

    /**
     * Saves the validation value.
     */
    private TernaryValue valid;

    /**
     * Creates a validation result state with the specified wellformedness and
     * validity values.
     *
     * @param wellFormed
     *            whether the file is well formed
     * @param valid
     *            whether the file is valid
     */
    public ValidationResultState(int wellFormed, int valid) {
        this.wellFormed = TernaryValue.valueOf(wellFormed);
        this.valid = TernaryValue.valueOf(valid);
    }

    /**
     * Generates an empty result. The value must be contributed elsewhere.
     */
    private ValidationResultState() {
    }

    /**
     * Formulates the bivalent validation result of the JHove library as an
     * expression in English. The result is returned along with (possibly)
     * further messages in the result object to allow the operator to better
     * estimate the validation result.
     * 
     * @return the validation result in English
     */
    public String getResultString() {
        return "Examination result:" + wellFormed.toModalAdverb() + " well-formed," + valid.toModalAdverb() + " valid";
    }

    /**
     * Returns the state of the validation result state.
     *
     * @return the state
     */
    public State toState() {
        return State.valueOf(KitodoConfig.getParameter(LongTermPreservationValidationConfigParameter.valueOf(this)));
    }

    @Override
    public String toString() {
        return wellFormed.toString() + '/' + valid.toString();
    }

}
