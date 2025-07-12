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
 * An immutable, no-setters beans implementing the validation condition
 * interface.
 */
public class LtpValidationCondition implements LtpValidationConditionInterface {

    private String property;
    private LtpValidationConditionOperation operation;
    private List<String> values;
    private LtpValidationConditionSeverity severity;

    /**
     * Creates a new validation condition.
     * 
     * @param property
     *            the property that is checked
     * @param operation
     *            the operation a property is checked against
     * @param values
     *            the values that a property is checked against
     * @param severity
     *            the failure severity of this condition
     */
    public LtpValidationCondition(String property, LtpValidationConditionOperation operation, List<String> values,
            LtpValidationConditionSeverity severity) {
        this.property = property;
        this.operation = operation;
        this.values = values;
        this.severity = severity;
    }

    /**
     * Return the property of the file that is checked (e.g., ImageWidth,
     * ColorSpace, etc.)
     * 
     * @return the property of the the file that is checked
     */
    @Override
    public String getProperty() {
        return property;
    }

    /**
     * Return the operation that is used to check the property against the
     * condition values (e.g., equal, not_equal, smaller_than, etc.)
     * 
     * @return the operation that is used to check the property against the
     *         condition values
     */
    @Override
    public LtpValidationConditionOperation getOperation() {
        return operation;
    }

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
    @Override
    public List<String> getValues() {
        return values;
    }

    /**
     * Return the severity of the validation condition, whether the condition is
     * critical and should be treated as an error, or whether the condition is
     * optional and should be treated as a warning.
     * 
     * @return the severity of the validation condition
     */
    @Override
    public LtpValidationConditionSeverity getSeverity() {
        return severity;
    }

}
