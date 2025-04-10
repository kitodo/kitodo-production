package org.kitodo.api.validation.longtermpreservation;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

public class LtpValidationResult {

    private LtpValidationResultState state;
    private List<LtpValidationError> errors;
    private List<LtpValidationConditionResult> conditionResults;
    private List<String> additionalMessages;

    public LtpValidationResult(LtpValidationResultState generalState, List<LtpValidationError> generalErrors) {
        this(generalState, generalErrors, Collections.emptyList(), Collections.emptyList());
    }

    public LtpValidationResult(
        LtpValidationResultState generalState, 
        List<LtpValidationError> generalErrors, 
        List<LtpValidationConditionResult> conditionResults, 
        List<String> generalMessages
    ) {
        this.state = generalState;
        this.errors = generalErrors;
        this.conditionResults = conditionResults;
        this.additionalMessages = generalMessages;
    }
    
    public LtpValidationResultState getState() {
        return state;
    }

    public List<LtpValidationError> getErrors() {
        return errors;
    }

    /**
     * Return the validation state (valid, warning, error) of each validation condition that was checked.
     * 
     * @return the validation state of each validation condition
     */
    public List<LtpValidationConditionResult> getConditionResults() {
        return conditionResults;
    };

    /**
     * Return a list of general validation messages that might provide additional information about
     * why validation failed. Messages are not related to specific validation conditions.
     * 
     * @return
     */
    public List<String> getAdditionalMessages() {
        return additionalMessages;
    };

    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("LtpValidationResult " + super.toString() + "\n");
        builder.append("- generalState: " + state.name() + "\n");
        builder.append("- generalErrors: " + StringUtils.join(errors.stream().map((e) -> e.name()).collect(Collectors.toList()), ", ") + "\n");
        builder.append("- conditionResults: " + StringUtils.join(conditionResults.stream().map((r) -> r.toString()).collect(Collectors.toList()), ", ") + "\n");
        builder.append("- addtionalMessages: " + StringUtils.join(additionalMessages, ",") + "\n");
        return builder.toString();
    }

}
