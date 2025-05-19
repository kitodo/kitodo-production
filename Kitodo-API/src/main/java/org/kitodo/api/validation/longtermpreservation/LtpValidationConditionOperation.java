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
 * Types of operations of a validation condition.
 */
public enum LtpValidationConditionOperation {

    /**
     * The condition value is verified to be exactly equal to the extracted value.
     */
    EQUAL,

    /**
     * The extracted value is verified to be any one of the condition values.
     */
    ONE_OF,

    /**
     * The extracted value is verified to be none of the condition values.
     */
    NONE_OF,

    /**
     * The extracted value is verified to be smaller than the condition value.
     */
    SMALLER_THAN,

    /**
     * The extracted value is verified to be larger than the condition value.
     */
    LARGER_THAN,

    /**
     * The extracted value is verified to be in between the interval of two condition values.
     */
    IN_BETWEEN,

    /** 
     * The extracted value matches the provided regular expression without considering letter casing.
     */
    MATCHES
}
