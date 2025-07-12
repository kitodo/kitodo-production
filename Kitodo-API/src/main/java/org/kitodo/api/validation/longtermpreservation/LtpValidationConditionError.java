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

/**
 * Various errors types when checking validation conditions.
 */
public enum LtpValidationConditionError {

    /**
     * The property could not be extracted from the image, meaning the condition
     * could not be checked.
     */
    PROPERTY_DOES_NOT_EXIST,

    /**
     * The user did not specify the correct amount of values for an operation
     * (e.g. 2 value for "within interval" operation).
     */
    INCORRECT_NUMBER_OF_CONDITION_VALUES,

    /**
     * Either the condition value or extracted value could not be parsed to a
     * number even though it is required based on the condition operation (e.g.
     * "larger than" operation).
     */
    NOT_A_NUMBER,

    /**
     * The condition could be checked but values did not match.
     */
    CONDITION_FALSE,

    /**
     * The condition contains a regular expression with invalid syntax.
     */
    PATTERN_INVALID_SYNTAX,

    /**
     * The condition operation is is not supported by the backend
     * implementation.
     */
    UNKNOWN_OPERATION
}
