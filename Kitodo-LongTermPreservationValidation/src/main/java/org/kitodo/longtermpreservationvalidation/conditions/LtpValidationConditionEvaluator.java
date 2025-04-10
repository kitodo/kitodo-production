package org.kitodo.longtermpreservationvalidation.conditions;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.kitodo.api.validation.longtermpreservation.LtpValidationConditionError;
import org.kitodo.api.validation.longtermpreservation.LtpValidationConditionInterface;
import org.kitodo.api.validation.longtermpreservation.LtpValidationConditionOperation;
import org.kitodo.api.validation.longtermpreservation.LtpValidationConditionResult;
import org.kitodo.api.validation.longtermpreservation.LtpValidationConditionSeverity;
import org.kitodo.api.validation.longtermpreservation.LtpValidationResultState;

public class LtpValidationConditionEvaluator {
    
    public static List<LtpValidationConditionResult> evaluateValidationConditions(
        List<? extends LtpValidationConditionInterface> conditions, 
        Map<String, String> properties
    ) {
        return conditions.stream()
            .map((c) -> evaluateValidationCondition(c, properties))
            .collect(Collectors.toList());
    }

    public static LtpValidationResultState conditionSeverityToState(LtpValidationConditionSeverity severity) {
        if (severity == LtpValidationConditionSeverity.WARNING) {
            return LtpValidationResultState.WARNING;
        }
        return LtpValidationResultState.ERROR;        
    }

    public static LtpValidationConditionResult evaluateValidationCondition(
        LtpValidationConditionInterface condition, 
        Map<String, String> properties
    ) {
        String property = condition.getProperty();

        if (!properties.containsKey(property)) {
            // property does not exist
            return new LtpValidationConditionResult(
                false, 
                LtpValidationConditionError.PROPERTY_DOES_NOT_EXIST, 
                null
            );
        }

        String value = properties.get(property).toLowerCase();

        // check equal operation
        if (condition.getOperation().equals(LtpValidationConditionOperation.EQUAL)) {
            if (Objects.isNull(condition.getValues()) || condition.getValues().size() != 1) {
                return new LtpValidationConditionResult(
                    false, 
                    LtpValidationConditionError.INCORRECT_NUMBER_OF_CONDITION_VALUES, 
                    value
                );
            }

            if (value.equals(condition.getValues().get(0).toLowerCase())) {
                return new LtpValidationConditionResult(true, null, value);
            }

            return new LtpValidationConditionResult(
                false, 
                LtpValidationConditionError.CONDITION_FALSE, 
                value
            );
        }

        // unknown operation
        return new LtpValidationConditionResult(
            false, 
            LtpValidationConditionError.UNKNOWN_OPERATION, 
            value
        );
    }

    public static LtpValidationResultState summarizeValidationState(
        List<? extends LtpValidationConditionInterface> conditions, 
        List<LtpValidationConditionResult> conditionResults
    ) {
        LtpValidationResultState worstState = LtpValidationResultState.VALID;

        int i = 0;
        for (LtpValidationConditionInterface condition : conditions) {
            LtpValidationConditionResult conditionResult = conditionResults.get(i);
            if (!conditionResult.getPassed()) {
                if (condition.getSeverity().equals(LtpValidationConditionSeverity.WARNING)) {
                    worstState = LtpValidationResultState.WARNING;
                }
                if (condition.getSeverity().equals(LtpValidationConditionSeverity.ERROR)) {
                    return LtpValidationResultState.ERROR;
                }
            }
            i += 1;
        }

        return worstState;
    }

}
