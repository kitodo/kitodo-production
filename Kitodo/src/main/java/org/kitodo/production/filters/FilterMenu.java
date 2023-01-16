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

package org.kitodo.production.filters;

import org.kitodo.data.database.beans.User;
import org.kitodo.production.enums.FilterPart;
import org.kitodo.production.enums.FilterString;
import org.kitodo.production.forms.CurrentTaskForm;
import org.kitodo.production.forms.ProcessForm;
import org.kitodo.production.services.ServiceManager;
import org.kitodo.production.services.data.FilterService;

import javax.faces.context.FacesContext;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class FilterMenu {

    private static final int MAX_SUGGESTIONS = 15;

    private ProcessForm processForm = null;
    private CurrentTaskForm taskForm = null;
    private List<Suggestion> suggestions;
    private List<ParsedFilter> parsedFilters;
    private String filterInEditMode;

    public FilterMenu(ProcessForm processForm) {
        this.processForm = processForm;
        suggestions = createSuggestionsForProcessAndTaskCategory("");
        parsedFilters = new ArrayList<>();
    }

    /**
     * Default constructor.
     */
    public FilterMenu(CurrentTaskForm taskForm) {
        this.taskForm = taskForm;
        suggestions = createSuggestionsForProcessAndTaskCategory("");
        parsedFilters = new ArrayList<>();
    }

    public List<Suggestion> getSuggestions() {
        return suggestions;
    }

    /**
     * Create suggestions based on a String passed from a {@code <p:remoteCommand/>}.
     */
    public void updateSuggestions() {
        Map<String, String> params = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
        updateSuggestions(params.get("input"));
    }

    /**
     * Create suggestions based on the given input string.
     *
     * @param input String to get suggestions for
     */
    public void updateSuggestions(String input) {
        // remove quotation marks and minus
        String strippedInput = input.replaceAll("^[\"-]+|\"$", "");

        // determine the last part of the input
        int lastColonIndex = strippedInput.lastIndexOf(":");
        if (lastColonIndex == -1) {
            // category should be suggested
           suggestions = createSuggestionsForProcessAndTaskCategory(input);

        } else {
            // at least one colon in the input string
            String lastPart = input.substring(lastColonIndex + 1);
            Pattern patternNextCategory = Pattern.compile("(?<= \\| )\\w?$");
            Matcher matcherNextCategory = patternNextCategory.matcher(lastPart);
            if (matcherNextCategory.find()) {
                // strings ends with " | "
                // category should be suggested
                suggestions = createSuggestionsForProcessAndTaskCategory(matcherNextCategory.group());
            } else {
                // value should be suggested
                Pattern patternPreviousCategory = Pattern.compile("\\w+:(?!.*:)");
                Matcher matcherPreviousCategory = patternPreviousCategory.matcher(input);
                String category = matcherPreviousCategory.find() ? matcherPreviousCategory.group() : "";
                suggestions = createSuggestionsForProcessValue(normalizeFilterCategory(category), lastPart); // TODO only for processes? also tasks?
            }
        }

    }
    
    private List<Suggestion> createSuggestionsForProcessAndTaskCategory(String input) {
        List<Suggestion> suggestions = new ArrayList<>();
        for (FilterString filterString : FilterString.values()) {
            if (filterString.getFilterEnglish().startsWith(input.toLowerCase())
                    || filterString.getFilterGerman().startsWith(input.toLowerCase())) {
                suggestions.add(new Suggestion(input, filterString.getFilterEnglish(), FilterPart.CATEGORY));
            }
        }
        return suggestions;
    }

    private List<Suggestion> createSuggestionsForProcessValue(FilterString category, String input) {
        List<Suggestion> suggestions = new ArrayList<>();
        if (Objects.isNull(category)) {
            return suggestions;
        }
        FilterService filterService = ServiceManager.getFilterService();
        switch (category) {
            case TASK:
            case TASKINWORK:
            case TASKLOCKED:
            case TASKOPEN:
            case TASKDONE:
            case TASKDONETITLE:
                suggestions.addAll(createStringSuggestionsMatchingInput(input, filterService.initStepTitles(), FilterPart.VALUE));
                break;
            case PROJECT:
                suggestions.addAll(createStringSuggestionsMatchingInput(input, filterService.initProjects(), FilterPart.VALUE));
                break;
            case PROPERTY:
                suggestions.addAll(createStringSuggestionsMatchingInput(input, filterService.initProcessPropertyTitles(), FilterPart.VALUE));
                break;
            case TASKDONEUSER:
                suggestions.addAll(createUserSuggestionsMatchingInput(input, filterService.initUserList(), FilterPart.VALUE));
            case BATCH:
            case TASKAUTOMATIC:
            case PROCESS:
            case ID:
            case PARENTPROCESSID:
                break;
        }
        return suggestions;
    }

    private List<Suggestion> createStringSuggestionsMatchingInput(String input, List<String> suggestions, FilterPart filterPart) {
        return suggestions.stream()
                .filter(suggestion -> suggestion.startsWith(input))
                .limit(MAX_SUGGESTIONS)
                .map(suggestion -> new Suggestion(input, suggestion, filterPart))
                .collect(Collectors.toList());
    }

    private List<Suggestion> createUserSuggestionsMatchingInput(String input, List<User> suggestions, FilterPart filterPart) {
        return suggestions.stream()
                .filter(user -> user.getLogin().startsWith(input) || user.getFullName().startsWith(input))
                .limit(MAX_SUGGESTIONS)
                .map(user -> new Suggestion(input, user.getLogin(), filterPart))
                .collect(Collectors.toList());
    }

    private List<Suggestion> createSuggestionsForTaskValue(String input) {
        // TODO load available values from somewhere else – Forms?
        return new ArrayList<>();
    }

    private List<Suggestion> createSuggestionsForUserCategory(String input) {
        // TODO load available values from somewhere else – Forms?
        return new ArrayList<>();
    }

    private List<Suggestion> createSuggestionsForUserValue(String input) {
        // TODO load available values from somewhere else – Forms?
        return new ArrayList<>();
    }

    private FilterString normalizeFilterCategory(String category) {
        return Arrays.stream(FilterString.values())
                .filter(f -> f.getFilterGerman().equals(category.toLowerCase()) || f.getFilterEnglish().equals(category.toLowerCase()))
                .findFirst().orElse(null);
    }

    /**
     * Get parsedFilters.
     *
     * @return value of parsedFilters
     */
    public List<ParsedFilter> getParsedFilters() {
        return parsedFilters;
    }

    /**
     * Get filterInEditMode.
     *
     * @return value of filterInEditMode
     */
    public String getFilterInEditMode() {
        return filterInEditMode;
    }

    /**
     * Set filterInEditMode.
     *
     * @param filterInEditMode as java.lang.String
     */
    public void setFilterInEditMode(String filterInEditMode) {
        this.filterInEditMode = filterInEditMode;
    }

    public void addParsedFilter(String plainFilter) {
        parsedFilters.add(new ParsedFilter(plainFilter));
    }

    /**
     * Remove the given filter from the list of ParsedFilters.
     * This method does not take care of the actual plain filter used for filtering.
     *
     * @param filterToBeRemoved as ParsedFilter object
     */
    public void removeParsedFilter(ParsedFilter filterToBeRemoved) {
        parsedFilters.remove(filterToBeRemoved);
    }

    /**
     * Parse the given filter string.
     * The given string is split into separate filters before being transformed into ParsedFilter objects.
     *
     * @param filterString String containing one or multiple filters
     */
    public void parseFilters(String filterString) {
        parsedFilters.clear();
        List<String> plainFilters = List.of(filterString.split(" ?\"| (?=\\|)|(?<=\\|) "));
        for (String plainFilter : plainFilters) {
            if (plainFilter.replaceAll("\\s", "").length() > 0) {
                parsedFilters.add(new ParsedFilter(plainFilter));
            }
        }
    }

    public void submitFilters() {
        if (filterInEditMode.length() > 0) {
            addParsedFilter(filterInEditMode);
            filterInEditMode = "";
            updateSuggestions("");
        }
        updateFilters();
    }

    /**
     * Remove the given filter.
     *
     * @param filterToBeRemoved as ParsedFilter object
     */
    public void removeFilter(ParsedFilter filterToBeRemoved) {
        removeParsedFilter(filterToBeRemoved);
        updateFilters();
    }

    /**
     * Remove the filter passed in the request.
     * The filter can be passed as plain filter string from a {@code <p:remoteCommand/>} as request parameter.
     */
    public void removeFilter() {
        Map<String, String> params = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
        removeFilter(new ParsedFilter(params.get("plainFilter")));
    }

    public void updateFilters() {
        StringBuilder newFilter = new StringBuilder();
        for (ParsedFilter parsedFilter : parsedFilters) {
            if (newFilter.length() > 0) {
                newFilter.append(" ");
            }
            newFilter.append(parsedFilter.getPlainFilter());
        }
        if (Objects.nonNull(processForm)) {
            processForm.setFilter(newFilter.toString());
        } else if (Objects.nonNull(taskForm)) {
            taskForm.setFilter(newFilter.toString());
        } else {
            // TODO handle users
        }
    }
}
