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

package org.kitodo.api.validation.longtermpreservation;

import java.util.Objects;

/**
 * Contains information about the success or failure state after checking a validation condition.
 */
public class LtpValidationConditionResult {
    
    /**
     * Whether the validation condition matched with values extracted from the image.
     */
    boolean passed;

    /**
     * If non-null, the error type of why a condition did not match.
     */
    LtpValidationConditionError error;

    /**
     * The value extracted from an image to be shown to the user as justification why a condition did not pass.
     */
    String value;

    /**
     * Initialize a new condition validation result by providing all information.
     * 
     * @param passed whether the condition passed
     * @param error a potential error or null if no error
     * @param value the value extracted from the image
     */
    public LtpValidationConditionResult(boolean passed, LtpValidationConditionError error, String value) {
        this.passed = passed;
        this.error = error;
        this.value = value;
    }

    /**
     * Return whether the condition passed.
     * 
     * @return whether the condition passed
     */
    public boolean getPassed() {
        return passed;
    }

    /**
     * Return the error if a condition did not pass.
     * 
     * @return the error if a condition did not pass or null
     */
    public LtpValidationConditionError getError() {
        return error;
    }

    /**
     * Return the value extracted from the image.
     * 
     * @return the value extracted from the image
     */
    public String getValue() {
        return value;
    }

    /**
     * Simplified string representation of this condition result for debugging.
     */
    @Override
    public String toString() {
        String errorName = Objects.nonNull(error) ? error.name() : "none";
        return "[LtpValidationConditionResult passed=" + passed + " error=" + errorName + " value='" + value + "']";
    }
}
