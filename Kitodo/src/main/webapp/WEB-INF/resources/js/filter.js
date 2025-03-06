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
/* globals removeFilter, updateSuggestions, submitFilters, filterKeydownEvents */

/* Define identifiers used to select elements */
const FILTER_INPUT_FORM = "#filterInputForm";
const FILTER_INPUT = "#filterInputForm\\:filterfield";
const FILTER_INPUT_PARSED_FILTERS_AND_OPTIONS_FORMS = "#filterInputForm, #parsedFiltersForm, #filterOptionsForm";
const FILTER_OPTIONS_FORM_WRAPPER = "#filterOptionsFormWrapper";
const SUGGESTIONS_ITEMS = "#filterOptionsForm\\:suggestions .suggestion";
const SELECTED_SUGGESTION_ITEM = "#filterOptionsForm\\:suggestions .suggestion.selected";
const PARSED_FILTERS = "#parsedFiltersForm\\:parsedFilters";


/**
 * The list of parsed filters overlaps the input, but should not block the actual content.
 * This function moves the content of the input to the right by setting the padding.
 */
function setFilterInputPadding() {
    let padding = $(PARSED_FILTERS).width() > 0 ? $(PARSED_FILTERS).width() + 'px' : '';
    $(FILTER_INPUT).css('padding-left', padding);
}

/**
 * The relevant part of the input is replaced with the given suggestion.
 * The result is applied to the input field.
 */
function replaceInput(relevantInputValue, suggestion) {
    let filterInput = $(FILTER_INPUT);
    let completeInputValue = filterInput.val();
    let index = completeInputValue.lastIndexOf(relevantInputValue);
    filterInput.val(completeInputValue.substring(0, index) + suggestion);
}

/**
 * Use the currently selected entry in the list or the one clicked by mouse.
 */
function selectConfirm(selection = $(SELECTED_SUGGESTION_ITEM)) {
    let relevantInput = selection.children().attr('data-input');
    let suggestion = selection.children().attr('data-suggestion');
    replaceInput(relevantInput, suggestion);
    $(SELECTED_SUGGESTION_ITEM).removeClass('selected');
    updateSuggestions([{name: "input", value: $(FILTER_INPUT).val()}]);
}

/**
 * Select next entry in the list.
 * If no entry is selected so far, the first one will be selected.
 */
function selectNext() {
    let suggestions = $(SUGGESTIONS_ITEMS);
    let selection = $(SELECTED_SUGGESTION_ITEM);
    if (selection.length === 1) {
        if (selection.next('.suggestion').length === 1) {
            selection.next('.suggestion').addClass('selected');
            selection.removeClass('selected');
        }
    } else {
        selection = suggestions.first();
        selection.addClass('selected');
    }
}

/**
 * Select previous entry in the list.
 * If no entry is selected so far, the last one will be selected.
 */
function selectPrevious() {
    let suggestions = $(SUGGESTIONS_ITEMS);
    let selection = $(SELECTED_SUGGESTION_ITEM);
    if (selection.length === 1) {
        if (selection.prev('.suggestion').length === 1) {
            selection.prev('.suggestion').addClass('selected');
            selection.removeClass('selected');
        }
    } else {
        selection = suggestions.last();
        selection.addClass('selected');
    }
}

/**
 * Submit the value of the filter input.
 */
function submitInput() {
    // remove event listener because JSF will update the field and register a new event handler
    $(FILTER_INPUT_FORM).off("keydown.filter");
    submitFilters([{name: "input", value: $(FILTER_INPUT).val()}]);
    $(FILTER_INPUT).trigger("change");
}

/**
 * Some keys are used to navigate inside the suggestion menu. Those keys' keydown events should not trigger an update.
 * @param event keydown event
 * @returns {boolean} indicating whether the event should trigger an update or not
 */
function filterKeydownEvents(event) {
    let keysToBeFilteredOut = [
        "ArrowDown",
        "ArrowUp",
        "Enter"
    ];
    return !keysToBeFilteredOut.includes(event.key);
}

/**
 * Call matching method to handle the occurred event.
 * @param event "keydown" event occurred on the input field
 */
function handleKeydown(event) {
    switch (event.originalEvent.code) {
        case "ArrowDown":
            selectNext();
            event.preventDefault();
            break;
        case "ArrowUp":
            selectPrevious();
            event.preventDefault();
            break;
        case "Backspace":
            if ($(FILTER_INPUT).val() === '') {
                removeFilter([{name: "plainFilter", value: $(PARSED_FILTERS).find('.ui-datalist-item .plainFilter').last().text()}]);
            }
            break;
        case "Enter":
        case "NumpadEnter":
            if ($(FILTER_OPTIONS_FORM_WRAPPER).is(':visible') && $(SELECTED_SUGGESTION_ITEM).length === 1) {
                selectConfirm();
            } else {
                submitInput();
            }
            event.preventDefault();
            event.stopImmediatePropagation();
            return false;
        default:
            // reset selection of suggestion when user is typing normal characters
            $(SELECTED_SUGGESTION_ITEM).removeClass("selected");
    }
}

/**
 * Display the filter options overlay.
 */
function openFilterOptionsMenu() {
    $(FILTER_OPTIONS_FORM_WRAPPER).show();
}

/**
 * Close the filter options overlay.
 */
function closeFilterOptionsMenu() {
    $(FILTER_OPTIONS_FORM_WRAPPER).hide();
}

$(document).ready(function () {
    $(FILTER_INPUT_FORM).on("focusin.filter", FILTER_INPUT, function (e) {
        // Add event listener for "keydown" when filter gets focus
        $(FILTER_INPUT_FORM).on("keydown.filter", [FILTER_INPUT], function (e) {
            return handleKeydown(e);
        });
        // Open filter options/suggestions menu when input gets focus
        openFilterOptionsMenu();
    });

    $(document).on("click.filter", function (e) {
        let target = $(e.target);
        if ($(FILTER_OPTIONS_FORM_WRAPPER).is(':visible')) {
            if (target.parents(FILTER_INPUT_PARSED_FILTERS_AND_OPTIONS_FORMS).length === 0) {
                // User clicked outside input or filter options/suggestions menu. Menu should be closed and filter should be submitted.
                closeFilterOptionsMenu();
                submitInput();
                return;
            } else if (target.hasClass("suggestion") || target.parents(".suggestion").length) {
                // User clicked on suggestion. Suggestion should be selected and copied to input.
                let selection = target.hasClass("suggestion") ? target : target.parents(".suggestion").first();
                selectConfirm(selection);
                $(FILTER_INPUT).focus();
                return;
            }
        }

        if (!target.hasClass("ui-button") && target.parents(".ui-button").length === 0 && target.parents(PARSED_FILTERS).length) {
            // User clicked on parsed filter. Filter should be moved to filter input.
            let filter = target.siblings(".plainFilter").length ? target.siblings(".plainFilter").first().text()
                : target.find(".plainFilter").first().text();
            $(FILTER_INPUT).val(filter);
            removeFilter([{name: "plainFilter", value: filter}]);
            $(FILTER_INPUT).focus();
        }
    });

    // Remove event listener for "keydown" when filter loses focus
    $(FILTER_INPUT_FORM).on("focusout.filter", [FILTER_INPUT], function (e) {
        $(FILTER_INPUT_FORM).off("keydown.filter");
    });

    // Set padding of filter input to match width of parsed filters
    setFilterInputPadding();
});
