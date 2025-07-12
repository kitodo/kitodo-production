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

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

/**
 * Information about the success or failure of validating a single image or
 * file.
 */
public class LtpValidationResult {

    /**
     * The overall validation success state (valid, warning, error).
     */
    private LtpValidationResultState state;

    /**
     * Any general errors that might have occurred during validation.
     */
    private List<LtpValidationError> errors;

    /**
     * The success or failure state of checking each individual validation
     * condition.
     */
    private List<LtpValidationConditionResult> conditionResults;

    /**
     * Additional backend-specific information not covered by this LTP
     * Validation API, e.g. debug information about file-type specific errors.
     */
    private List<String> additionalMessages;

    /**
     * Initializes a validation result if the validation conditions could not be
     * checked.
     * 
     * @param generalState
     *            the failure state
     * @param generalErrors
     *            any errors that occurred during validation
     */
    public LtpValidationResult(LtpValidationResultState generalState, List<LtpValidationError> generalErrors) {
        this(generalState, generalErrors, Collections.emptyList(), Collections.emptyList());
    }

    /**
     * Initializes a validation result by providing all relevant information.
     * 
     * @param generalState
     *            the success or failure state of the validation for the file or
     *            image
     * @param generalErrors
     *            any general errors that occurred during validation
     * @param conditionResults
     *            the success or failure state of each validation condition
     * @param generalMessages
     *            any additional message provided by file-specific validation
     *            backend
     */
    public LtpValidationResult(LtpValidationResultState generalState, List<LtpValidationError> generalErrors,
            List<LtpValidationConditionResult> conditionResults, List<String> generalMessages) {
        this.state = generalState;
        this.errors = generalErrors;
        this.conditionResults = conditionResults;
        this.additionalMessages = generalMessages;
    }

    /**
     * Returns the overall validation state, success or failure.
     *
     * @return the overall state of the validation
     */
    public LtpValidationResultState getState() {
        return state;
    }

    /**
     * Returns general errors that occurred during validation.
     * 
     * @return a list of general validation errors
     */
    public List<LtpValidationError> getErrors() {
        return errors;
    }

    /**
     * Returns the validation state of each validation condition. Each
     * validation condition can have a status of either valid, warning, or
     * error.
     * 
     * @return the validation state of each validation condition
     */
    public List<LtpValidationConditionResult> getConditionResults() {
        return conditionResults;
    }

    /**
     * Returns a list of general validation messages. These may provide
     * additional information about the reason for the validation error. The
     * messages do not refer to specific validation conditions.
     * 
     * @return the list of messages
     */
    public List<String> getAdditionalMessages() {
        return additionalMessages;
    }

    /**
     * A simplified string representation of this validation result for
     * debugging purposes.
     * 
     * @return the string representation of this validation result
     */
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        String joinedErrors = StringUtils.join(errors.stream().map((e) -> e.name()).collect(Collectors.toList()), ", ");
        String joinedConditionResults = StringUtils
                .join(conditionResults.stream().map((r) -> r.toString()).collect(Collectors.toList()), ", ");
        String joinedMessages = StringUtils.join(additionalMessages, ", ");
        builder.append("LtpValidationResult " + super.toString() + "\n");
        builder.append("- state: " + state.name() + "\n");
        builder.append("- errors: " + joinedErrors + "\n");
        builder.append("- conditionResults: " + joinedConditionResults + "\n");
        builder.append("- addtionalMessages: " + joinedMessages + "\n");
        return builder.toString();
    }

}
