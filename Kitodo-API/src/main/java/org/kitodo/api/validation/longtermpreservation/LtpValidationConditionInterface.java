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

import java.util.List;

/**
 * Describes a condition that has to be met when validating files for long term
 * preservation (ltp).
 * 
 * <p>
 * For example, a validation condition for a TIF file might be that the file is
 * stored in one of several acceptable color spaces (RGB, CMYK).
 * 
 * <p>
 * A condition is modelled by the simple format "{property} {operation}
 * {value(s)}". Example: "{ColorSpace} {contained in} {RGB,CMYK}".
 */
public interface LtpValidationConditionInterface {

    /**
     * Return the property of the file that is checked (e.g., ImageWidth,
     * ColorSpace, etc.)
     * 
     * @return the property of the the file that is checked
     */
    public String getProperty();

    /**
     * Return the operation that is used to check the property against the
     * condition values (e.g., equal, not_equal, smaller_than, etc.)
     * 
     * @return the operation that is used to check the property against the
     *         condition values
     */
    public LtpValidationConditionOperation getOperation();

    /**
     * Return a list of values that are checked against the property of the
     * file.
     * 
     * <p>
     * May be a single value (comparing via equal), two values (comparing as
     * interval) or multiple values (comparing as set) depending on the
     * operation
     * </p>
     * 
     * @return the list of values
     */
    public List<String> getValues();

    /**
     * Return the severity of the validation condition, whether the condition is
     * critical and should be treated as an error, or whether the condition is
     * optional and should be treated as a warning.
     * 
     * @return the severity of the validation condition
     */
    public LtpValidationConditionSeverity getSeverity();

}
