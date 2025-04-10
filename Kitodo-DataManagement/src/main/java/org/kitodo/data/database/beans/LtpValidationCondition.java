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

@Entity
@Table(name = "ltp_validation_condition")
public class LtpValidationCondition extends BaseBean implements LtpValidationConditionInterface {

    @Column(name = "property")
    private String property;

    @Column(name = "operation")
    @Enumerated(EnumType.STRING)
    private LtpValidationConditionOperation operation;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name="ltp_validation_condition_value", joinColumns = @JoinColumn(name="ltp_validation_condition_id"))
    @OrderColumn(name = "sorting")
    @Column(name = "value")
    private List<String> values;

    @Column(name = "severity")
    @Enumerated(EnumType.STRING)
    private LtpValidationConditionSeverity severity;

    @ManyToOne(optional=false) 
    @JoinColumn(name="ltp_validation_configuration_id", nullable=false)
    private LtpValidationConfiguration ltpValidationConfiguration;
    
    public String getProperty() {
        return property;
    }
    
    public LtpValidationConditionOperation getOperation() {
        return operation;
    }
    
    public List<String> getValues() {
        return values;
    }
    
    public LtpValidationConditionSeverity getSeverity() {
        return severity;
    }
    
    public LtpValidationConfiguration getLtpValidationConfiguration() {
        return ltpValidationConfiguration;
    }

    public void setProperty(String property) {
        this.property = property;
    }

    public void setOperation(LtpValidationConditionOperation operation) {
        this.operation = operation;
    }
    
    public void setValues(List<String> values) {
        this.values = values;
    }
    
    public void setSeverity(LtpValidationConditionSeverity severity) {
        this.severity = severity;
    }
    
    public void setLtpValidationConfiguration(LtpValidationConfiguration ltpValidationConfiguration) {
        this.ltpValidationConfiguration = ltpValidationConfiguration;
    }

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

    @Override
    public int hashCode() {
        return Objects.hash(id, property, operation, severity);
    }
    
}
