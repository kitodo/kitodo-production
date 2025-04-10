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


public class LtpValidationCondition implements LtpValidationConditionInterface {

    private String property;
    private LtpValidationConditionOperation operation;
    private List<String> values;
    private LtpValidationConditionSeverity severity;

    public LtpValidationCondition(String property, LtpValidationConditionOperation operation, List<String> values, LtpValidationConditionSeverity severity) {
        this.property = property;
        this.operation = operation;
        this.values = values;
        this.severity = severity;
    }

    public String getProperty() {
        return property;
    };

    public LtpValidationConditionOperation getOperation() {
        return operation;
    };

    public List<String> getValues() {
        return values;
    };

    public LtpValidationConditionSeverity getSeverity() {
        return severity;
    }
    
}
