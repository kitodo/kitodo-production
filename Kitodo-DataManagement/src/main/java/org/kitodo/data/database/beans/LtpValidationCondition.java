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

package org.kitodo.data.database.beans;

import java.util.List;
import java.util.Objects;

import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OrderColumn;
import javax.persistence.Table;

import org.kitodo.api.validation.longtermpreservation.LtpValidationConditionInterface;
import org.kitodo.api.validation.longtermpreservation.LtpValidationConditionOperation;
import org.kitodo.api.validation.longtermpreservation.LtpValidationConditionSeverity;

/**
 * The database bean storing a LTP validation condition.
 */
@Entity
@Table(name = "ltp_validation_condition")
public class LtpValidationCondition extends BaseBean implements LtpValidationConditionInterface {

    @Column(name = "property")
    private String property;

    @Column(name = "operation")
    @Enumerated(EnumType.STRING)
    private LtpValidationConditionOperation operation;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "ltp_validation_condition_value", joinColumns = @JoinColumn(name = "ltp_validation_condition_id"))
    @OrderColumn(name = "sorting")
    @Column(name = "value")
    private List<String> values;

    @Column(name = "severity")
    @Enumerated(EnumType.STRING)
    private LtpValidationConditionSeverity severity;

    @ManyToOne(optional = false) 
    @JoinColumn(name = "ltp_validation_configuration_id", nullable = false)
    private LtpValidationConfiguration ltpValidationConfiguration;
    
    /**
     * Return the property of the file that is checked (e.g., ImageWidth, ColorSpace, etc.)
     * 
     * @return the property of the the file that is checked
     */
    @Override
    public String getProperty() {
        return property;
    }
    
    /**
     * Return the operation that is used to check the property against the condition values
     * (e.g., equal, not_equal, smaller_than, etc.)
     * 
     * @return the operation that is used to check the property against the condition values
     */
    @Override
    public LtpValidationConditionOperation getOperation() {
        return operation;
    }

    /**
     * Return a list of values that are checked against the property of the file.
     * 
     * <p>May be a single value (comparing via equal), two values (comparing as interval) or 
     * multiple values (comparing as set) depending on the operation</p>
     * 
     * @return the list of values
     */
    @Override
    public List<String> getValues() {
        return values;
    }

    /**
     * Return the severity of the validation condition, whether the condition is critical
     * and should be treated as an error, or whether the condition is optional and should
     * be treated as a warning.
     * 
     * @return the severity of the validation condition
     */
    @Override
    public LtpValidationConditionSeverity getSeverity() {
        return severity;
    }

    /**
     * Return the LTP validation configuration this condition belongs to.
     * 
     * @return the LTP validation configuration this condition belongs to
     */
    public LtpValidationConfiguration getLtpValidationConfiguration() {
        return ltpValidationConfiguration;
    }

    /**
     * Sets the property of the file that is checked.
     * 
     * @param property the property that is checked
     */
    public void setProperty(String property) {
        this.property = property;
    }

    /**
     * Sets the condition operation that is applied when checking the condition.
     * 
     * @param operation the condition operation that is applied
     */
    public void setOperation(LtpValidationConditionOperation operation) {
        this.operation = operation;
    }

    /**
     * Sets the values that are used for comparison when checking the condition.
     * 
     * @param values the list of values that are used for comparison
     */
    public void setValues(List<String> values) {
        this.values = values;
    }

    /**
     * Sets the failure severity that is used in case the condition does not pass.
     * 
     * @param severity the failure severity
     */
    public void setSeverity(LtpValidationConditionSeverity severity) {
        this.severity = severity;
    }

    /**
     * Sets the LTP validation configuration this condition belongs to.
     * 
     * @param ltpValidationConfiguration the LTP validation configuration this condition belongs to
     */
    public void setLtpValidationConfiguration(LtpValidationConfiguration ltpValidationConfiguration) {
        this.ltpValidationConfiguration = ltpValidationConfiguration;
    }

    /**
     * Equals implementation based on the database id.
     */
    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }

        if (object instanceof LtpValidationCondition) {
            LtpValidationCondition condition = (LtpValidationCondition) object;
            return Objects.equals(this.getId(), condition.getId());
        }

        return false;
    }

    /**
     * Hash code implementation based on all properties of this conditon.
     */
    @Override
    public int hashCode() {
        return Objects.hash(id, property, operation, values, severity);
    }
}
