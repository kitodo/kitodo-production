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

    private static LtpValidationConditionResult getConditionFalseResult(String value) {
        return new LtpValidationConditionResult(
            false, 
            LtpValidationConditionError.CONDITION_FALSE, 
            value
        );
    }

    private static LtpValidationConditionResult getConditionIncorrectNumberOfValuesResult(String value) {
        return new LtpValidationConditionResult(
            false, 
            LtpValidationConditionError.INCORRECT_NUMBER_OF_CONDITION_VALUES, 
            value
        );
    }

    private static LtpValidationConditionResult getConditionNotANumberResult(String value) {
        return new LtpValidationConditionResult(
            false, 
            LtpValidationConditionError.NOT_A_NUMBER, 
            value
        );
    }

    private static LtpValidationConditionResult evaluateEqualCondition(
        String value,
        LtpValidationConditionInterface condition, 
        Map<String, String> properties
    ) {
        if (Objects.isNull(condition.getValues()) || condition.getValues().size() != 1) {
            return getConditionIncorrectNumberOfValuesResult(value);
        }

        if (value.equals(condition.getValues().get(0).toLowerCase())) {
            return new LtpValidationConditionResult(true, null, value);
        }

        return getConditionFalseResult(value);
    }

    private static LtpValidationConditionResult evaluateOneOfCondition(
        String value,
        LtpValidationConditionInterface condition, 
        Map<String, String> properties
    ) {
        if (Objects.isNull(condition.getValues()) || condition.getValues().size() == 0) {
            return getConditionIncorrectNumberOfValuesResult(value);
        }

        if (condition.getValues().stream().anyMatch(value::equalsIgnoreCase)) {
            return new LtpValidationConditionResult(true, null, value);
        }

        return getConditionFalseResult(value);
    }

    private static LtpValidationConditionResult evaluateNoneOfCondition(
        String value,
        LtpValidationConditionInterface condition, 
        Map<String, String> properties
    ) {
        if (Objects.isNull(condition.getValues()) || condition.getValues().size() == 0) {
            return getConditionIncorrectNumberOfValuesResult(value);
        }

        if (condition.getValues().stream().noneMatch(value::equalsIgnoreCase)) {
            return new LtpValidationConditionResult(true, null, value);
        }

        return getConditionFalseResult(value);
    }

    private static LtpValidationConditionResult evaluateLargerThanCondition(
        String value,
        LtpValidationConditionInterface condition, 
        Map<String, String> properties
    ) {
        if (Objects.isNull(condition.getValues()) || condition.getValues().size() != 1) {
            return getConditionIncorrectNumberOfValuesResult(value);
        }

        try {
            Float valueFloat = Float.parseFloat(value);
            Float conditionFloat = Float.parseFloat(condition.getValues().get(0));
            if (valueFloat >= conditionFloat) {
                return new LtpValidationConditionResult(true, null, value);
            }
        } catch (NumberFormatException e) {
            return getConditionNotANumberResult(value);
        }

        return getConditionFalseResult(value);
    }

    private static LtpValidationConditionResult evaluateSmallerThanCondition(
        String value,
        LtpValidationConditionInterface condition, 
        Map<String, String> properties
    ) {
        if (Objects.isNull(condition.getValues()) || condition.getValues().size() != 1) {
            return getConditionIncorrectNumberOfValuesResult(value);
        }

        try {
            Float valueFloat = Float.parseFloat(value);
            Float conditionFloat = Float.parseFloat(condition.getValues().get(0));
            if (valueFloat <= conditionFloat) {
                return new LtpValidationConditionResult(true, null, value);
            }
        } catch (NumberFormatException e) {
            return getConditionNotANumberResult(value);
        }

        return getConditionFalseResult(value);
    }

    private static LtpValidationConditionResult evaluateInBetweenThanCondition(
        String value,
        LtpValidationConditionInterface condition, 
        Map<String, String> properties
    ) {
        if (Objects.isNull(condition.getValues()) || condition.getValues().size() != 2) {
            return getConditionIncorrectNumberOfValuesResult(value);
        }

        try {
            Float valueFloat = Float.parseFloat(value);
            Float fromFloat = Float.parseFloat(condition.getValues().get(0));
            Float toFloat = Float.parseFloat(condition.getValues().get(1));
            if (valueFloat >= fromFloat && valueFloat <= toFloat) {
                return new LtpValidationConditionResult(true, null, value);
            }
        } catch (NumberFormatException e) {
            return getConditionNotANumberResult(value);
        }

        return getConditionFalseResult(value);
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
            return evaluateEqualCondition(value, condition, properties);
        } else if (condition.getOperation().equals(LtpValidationConditionOperation.ONE_OF)) {
            return evaluateOneOfCondition(value, condition, properties);
        } else if (condition.getOperation().equals(LtpValidationConditionOperation.NONE_OF)) {
            return evaluateNoneOfCondition(value, condition, properties);
        } else if (condition.getOperation().equals(LtpValidationConditionOperation.LARGER_THAN)) {
            return evaluateLargerThanCondition(value, condition, properties);
        } else if (condition.getOperation().equals(LtpValidationConditionOperation.SMALLER_THAN)) {
            return evaluateSmallerThanCondition(value, condition, properties);
        } else if (condition.getOperation().equals(LtpValidationConditionOperation.IN_BETWEEN)) {
            return evaluateInBetweenThanCondition(value, condition, properties);
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
