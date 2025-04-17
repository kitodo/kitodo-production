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
 * The failure severity of a condition.
 */
public enum LtpValidationConditionSeverity {
    
    /**
     * A condition failure is treated as warning, meaning it is not considered critical and should not interrupt the workflow.
     */
    WARNING,

    /**
     * A condition failure is treated as an error, meaning it is considered critical and should interrupt the workflow.
     */
    ERROR

}
