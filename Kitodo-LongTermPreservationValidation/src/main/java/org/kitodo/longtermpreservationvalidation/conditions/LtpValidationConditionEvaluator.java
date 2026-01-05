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

package org.kitodo.longtermpreservationvalidation.conditions;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import java.util.stream.Collectors;

import org.kitodo.api.validation.longtermpreservation.LtpValidationConditionError;
import org.kitodo.api.validation.longtermpreservation.LtpValidationConditionInterface;
import org.kitodo.api.validation.longtermpreservation.LtpValidationConditionOperation;
import org.kitodo.api.validation.longtermpreservation.LtpValidationConditionResult;
import org.kitodo.api.validation.longtermpreservation.LtpValidationConditionSeverity;
import org.kitodo.api.validation.longtermpreservation.LtpValidationResultState;

/**
 * Evalutes validation conditions by checking them against a map of extracted
 * property values.
 */
public class LtpValidationConditionEvaluator {

    /**
     * Evaluate validation conditions by checking them against a map of
     * extracted property values.
     * 
     * @param conditions
     *            the validation conditions to be checked
     * @param properties
     *            the property values extracted from an image to be checked
     *            against
     * 
     * @return the list of condition results containing the success or failure
     *         state for each condition
     */
    public static List<LtpValidationConditionResult> evaluateValidationConditions(
            List<? extends LtpValidationConditionInterface> conditions, Map<String, String> properties) {
        return conditions.stream().map((condition) -> evaluateValidationCondition(condition, properties))
                .collect(Collectors.toList());
    }

    /**
     * Returns that overall validation result state based of a condition
     * severity of a failed condition.
     * 
     * @param severity
     *            the condition failure severity
     * @return the validation result success or failure state
     */
    public static LtpValidationResultState conditionSeverityToState(LtpValidationConditionSeverity severity) {
        if (severity == LtpValidationConditionSeverity.WARNING) {
            return LtpValidationResultState.WARNING;
        }
        return LtpValidationResultState.ERROR;
    }

    /**
     * Short-hand for generating a condition result stating that a condition did
     * not pass.
     * 
     * @param value
     *            the value extracted from the image related to the condition
     * @return the condition result
     */
    private static LtpValidationConditionResult getConditionFalseResult(String value) {
        return new LtpValidationConditionResult(false, LtpValidationConditionError.CONDITION_FALSE, value);
    }

    /**
     * Short-hand for generating a condition result stating that an incorrect
     * number of values were provided such that the condition could not be
     * checked.
     * 
     * @param value
     *            the value extracted from the image related to the condition
     * @return the condition result
     */
    private static LtpValidationConditionResult getConditionIncorrectNumberOfValuesResult(String value) {
        return new LtpValidationConditionResult(false, LtpValidationConditionError.INCORRECT_NUMBER_OF_CONDITION_VALUES,
                value);
    }

    /**
     * Short-hand for generating a condition result stating that values could
     * not be parsed to numbers.
     * 
     * @param value
     *            the value extracted from the image related to the condition
     * @return the condition result
     */
    private static LtpValidationConditionResult getConditionNotANumberResult(String value) {
        return new LtpValidationConditionResult(false, LtpValidationConditionError.NOT_A_NUMBER, value);
    }

    /**
     * Evaluates an validation condition that is requiring an strict equality
     * between condition value and extracted value.
     * 
     * @param value
     *            the value extracted from the image
     * @param condition
     *            the condition
     * @return the condition result
     */
    private static LtpValidationConditionResult evaluateEqualCondition(String value,
            LtpValidationConditionInterface condition) {
        if (Objects.isNull(condition.getValues()) || condition.getValues().size() != 1) {
            return getConditionIncorrectNumberOfValuesResult(value);
        }

        if (value.toLowerCase().equals(condition.getValues().getFirst().toLowerCase())) {
            return new LtpValidationConditionResult(true, null, value);
        }

        return getConditionFalseResult(value);
    }

    /**
     * Evaluates an validation condition that is requiring the extracted value
     * to be any one of the condition values.
     * 
     * @param value
     *            the value extracted from the image
     * @param condition
     *            the condition
     * @return the condition result
     */
    private static LtpValidationConditionResult evaluateOneOfCondition(String value,
            LtpValidationConditionInterface condition) {
        if (Objects.isNull(condition.getValues()) || condition.getValues().size() == 0) {
            return getConditionIncorrectNumberOfValuesResult(value);
        }

        if (condition.getValues().stream().anyMatch(value::equalsIgnoreCase)) {
            return new LtpValidationConditionResult(true, null, value);
        }

        return getConditionFalseResult(value);
    }

    /**
     * Evaluates an validation condition that is requiring the extracted value
     * to be none of the condition values.
     * 
     * @param value
     *            the value extracted from the image
     * @param condition
     *            the condition
     * @return the condition result
     */
    private static LtpValidationConditionResult evaluateNoneOfCondition(String value,
            LtpValidationConditionInterface condition) {
        if (Objects.isNull(condition.getValues()) || condition.getValues().size() == 0) {
            return getConditionIncorrectNumberOfValuesResult(value);
        }

        if (condition.getValues().stream().noneMatch(value::equalsIgnoreCase)) {
            return new LtpValidationConditionResult(true, null, value);
        }

        return getConditionFalseResult(value);
    }

    /**
     * Evaluates an validation condition that is requiring the extracted value
     * to be larger than the condition value.
     * 
     * @param value
     *            the value extracted from the image
     * @param condition
     *            the condition
     * @return the condition result
     */
    private static LtpValidationConditionResult evaluateLargerThanCondition(String value,
            LtpValidationConditionInterface condition) {
        if (Objects.isNull(condition.getValues()) || condition.getValues().size() != 1) {
            return getConditionIncorrectNumberOfValuesResult(value);
        }

        try {
            float valueFloat = Float.parseFloat(value);
            float conditionFloat = Float.parseFloat(condition.getValues().getFirst());
            if (valueFloat >= conditionFloat) {
                return new LtpValidationConditionResult(true, null, value);
            }
        } catch (NumberFormatException e) {
            return getConditionNotANumberResult(value);
        }

        return getConditionFalseResult(value);
    }

    /**
     * Evaluates an validation condition that is requiring the extracted value
     * to be smaller than the condition value.
     * 
     * @param value
     *            the value extracted from the image
     * @param condition
     *            the condition
     * @return the condition result
     */
    private static LtpValidationConditionResult evaluateSmallerThanCondition(String value,
            LtpValidationConditionInterface condition) {
        if (Objects.isNull(condition.getValues()) || condition.getValues().size() != 1) {
            return getConditionIncorrectNumberOfValuesResult(value);
        }

        try {
            float valueFloat = Float.parseFloat(value);
            float conditionFloat = Float.parseFloat(condition.getValues().getFirst());
            if (valueFloat <= conditionFloat) {
                return new LtpValidationConditionResult(true, null, value);
            }
        } catch (NumberFormatException e) {
            return getConditionNotANumberResult(value);
        }

        return getConditionFalseResult(value);
    }

    /**
     * Evaluates an validation condition that is requiring the extracted value
     * to be in between the interval of the two condition values.
     * 
     * @param value
     *            the value extracted from the image
     * @param condition
     *            the condition
     * @return the condition result
     */
    private static LtpValidationConditionResult evaluateInBetweenThanCondition(String value,
            LtpValidationConditionInterface condition) {
        if (Objects.isNull(condition.getValues()) || condition.getValues().size() != 2) {
            return getConditionIncorrectNumberOfValuesResult(value);
        }

        try {
            float valueFloat = Float.parseFloat(value);
            float fromFloat = Float.parseFloat(condition.getValues().get(0));
            float toFloat = Float.parseFloat(condition.getValues().get(1));
            if (valueFloat >= fromFloat && valueFloat <= toFloat) {
                return new LtpValidationConditionResult(true, null, value);
            }
        } catch (NumberFormatException e) {
            return getConditionNotANumberResult(value);
        }

        return getConditionFalseResult(value);
    }

    /**
     * Evaluates an validation condition that where the extracted value needs to
     * match a regular expression.
     * 
     * @param value
     *            the value extracted from the image
     * @param condition
     *            the condition
     * @return the condition result
     */
    private static LtpValidationConditionResult evaluateMatchesCondition(String value,
            LtpValidationConditionInterface condition) {
        if (Objects.isNull(condition.getValues()) || condition.getValues().size() != 1) {
            return getConditionIncorrectNumberOfValuesResult(value);
        }

        try {
            Pattern pattern = Pattern.compile(condition.getValues().getFirst(), Pattern.CASE_INSENSITIVE);
            if (pattern.matcher(value).matches()) {
                return new LtpValidationConditionResult(true, null, value);
            }
            return getConditionFalseResult(value);
        } catch (PatternSyntaxException e) {
            return new LtpValidationConditionResult(false, LtpValidationConditionError.PATTERN_INVALID_SYNTAX, value);
        }
    }

    /**
     * Evaluate a validation condition that checks whether the value is at least
     * not empty.
     * 
     * @param value
     *            the value extracted from the image
     * @param condition
     *            the condition
     * @return the condition result
     */
    private static LtpValidationConditionResult evaluateNonEmptyCondition(String value,
            LtpValidationConditionInterface condition) {
        if (Objects.nonNull(condition.getValues()) && condition.getValues().size() > 0) {
            return getConditionIncorrectNumberOfValuesResult(value);
        }

        if (value.trim().isEmpty()) {
            return getConditionFalseResult(value);
        }
        return new LtpValidationConditionResult(true, null, value);
    }

    /**
     * Evaluates a single validation condition against the property values
     * extracted from an image.
     * 
     * @param condition
     *            the condition to be checked
     * @param properties
     *            the property values extracted from an image
     * @return the validation result
     */
    public static LtpValidationConditionResult evaluateValidationCondition(LtpValidationConditionInterface condition,
            Map<String, String> properties) {
        String property = condition.getProperty();

        if (!properties.containsKey(property)) {
            // property does not exist
            return new LtpValidationConditionResult(false, LtpValidationConditionError.PROPERTY_DOES_NOT_EXIST, null);
        }

        String value = properties.get(property);

        // check equal operation
        if (condition.getOperation().equals(LtpValidationConditionOperation.EQUAL)) {
            return evaluateEqualCondition(value, condition);
        } else if (condition.getOperation().equals(LtpValidationConditionOperation.ONE_OF)) {
            return evaluateOneOfCondition(value, condition);
        } else if (condition.getOperation().equals(LtpValidationConditionOperation.MATCHES)) {
            return evaluateMatchesCondition(value, condition);
        } else if (condition.getOperation().equals(LtpValidationConditionOperation.NONE_OF)) {
            return evaluateNoneOfCondition(value, condition);
        } else if (condition.getOperation().equals(LtpValidationConditionOperation.LARGER_THAN)) {
            return evaluateLargerThanCondition(value, condition);
        } else if (condition.getOperation().equals(LtpValidationConditionOperation.SMALLER_THAN)) {
            return evaluateSmallerThanCondition(value, condition);
        } else if (condition.getOperation().equals(LtpValidationConditionOperation.IN_BETWEEN)) {
            return evaluateInBetweenThanCondition(value, condition);
        } else if (condition.getOperation().equals(LtpValidationConditionOperation.NON_EMPTY)) {
            return evaluateNonEmptyCondition(value, condition);
        }

        // unknown operation
        return new LtpValidationConditionResult(false, LtpValidationConditionError.UNKNOWN_OPERATION, value);
    }

    /**
     * Calculates the overall validation success and failure state by checking
     * each condition result and its failure severity.
     * 
     * @param conditions
     *            the conditions that were checked
     * @param conditionResults
     *            the results for each condition
     * @return the overall validation sucesss or failure state
     */
    public static LtpValidationResultState summarizeValidationState(
            List<? extends LtpValidationConditionInterface> conditions,
            List<LtpValidationConditionResult> conditionResults) {
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
