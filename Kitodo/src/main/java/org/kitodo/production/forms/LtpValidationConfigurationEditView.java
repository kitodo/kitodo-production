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

package org.kitodo.production.forms;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Locale.LanguageRange;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import java.util.stream.Collectors;

import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;
import jakarta.xml.bind.JAXBException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kitodo.api.validation.longtermpreservation.LtpValidationConditionOperation;
import org.kitodo.api.validation.longtermpreservation.LtpValidationConditionSeverity;
import org.kitodo.config.xml.fileformats.FileFormat;
import org.kitodo.config.xml.fileformats.FileFormatsConfig;
import org.kitodo.data.database.beans.LtpValidationCondition;
import org.kitodo.data.database.beans.LtpValidationConfiguration;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.production.enums.ObjectType;
import org.kitodo.production.helper.Helper;
import org.kitodo.production.helper.validation.LtpValidationHelper;
import org.kitodo.production.services.ServiceManager;

@Named("LtpValidationConfigurationEditView")
@ViewScoped
public class LtpValidationConfigurationEditView extends BaseEditView {

    public static final String VIEW_PATH = MessageFormat.format(REDIRECT_PATH, "ltpValidationConfigurationEdit");

    private static final Logger logger = LogManager.getLogger(LtpValidationConfigurationEditView.class);
    private LtpValidationConfiguration configuration = new LtpValidationConfiguration();

    private static final String PROPERTY_WELLFORMED = "wellformed";
    private static final String PROPERTY_VALID = "valid";
    private static final String PROPERTY_FILENAME = "filename";
    private static final String VALUE_TRUE = "true";

    /**
     * Cache for the list of possible mime types.
     */
    private Map<String, String> mimeTypes = Collections.emptyMap();

    /**
     * The following variables (prefixed with "simple") are used for providing
     * simplified options to the user, such that a user does not need to learn
     * how validation conditions work and how they need to be specified.
     * 
     * <p>
     * If a non-null value is selected by a user, a corresponding validation
     * condition is added to the list of conditions. If a user removes the
     * option (sets it to null), the corresponding validation condition is
     * removed from the list of validation conditions.
     * </p>
     * 
     * <p>
     * Simplified options take precedence over manually specifying the same
     * condition in the list of validation conditions, meaning a condition on
     * the same property is modified to match the simplified option chosen by
     * the user.
     * </p>
     */
    private LtpValidationConditionSeverity simpleWellFormedSeverity;
    private LtpValidationConditionSeverity simpleValidSeverity;
    private LtpValidationConditionSeverity simpleFilenamePatternSeverity;
    private String simpleFilenamePattern;

    /**
     * Load mapping file by ID.
     *
     * @param id
     *            ID of mapping file to load
     */
    public void load(int id) {
        try {
            if (id > 0) {
                configuration = ServiceManager.getLtpValidationConfigurationService().getByIdWithFolders(id);
            } else {
                configuration = new LtpValidationConfiguration();
                configuration.setFolders(new ArrayList<>());
                configuration.setValidationConditions(new ArrayList<>());
            }
            setSaveDisabled(true);
        } catch (DAOException e) {
            Helper.setErrorMessage(ERROR_LOADING_ONE,
                new Object[] {ObjectType.LTP_VALIDATION_CONFIGURATION.getTranslationSingular(), id }, logger, e);
        }
    }

    /**
     * Save mapping file.
     *
     * @return projects page or empty String
     */
    public String save() {
        try {
            ServiceManager.getLtpValidationConfigurationService().save(configuration);
            return LtpValidationConfigurationListView.VIEW_PATH +  "&" + getReferrerListOptions();
        } catch (DAOException e) {
            Helper.setErrorMessage(ERROR_SAVING,
                new Object[] {ObjectType.LTP_VALIDATION_CONFIGURATION.getTranslationSingular() }, logger, e);
            return this.stayOnCurrentPage;
        }
    }

    /**
     * Get ltp validation configuration.
     *
     * @return value of ltp validation configuration
     */
    public LtpValidationConfiguration getConfiguration() {
        return configuration;
    }

    /**
     * Set ltp validation configuration.
     *
     * @param configuration
     *            as org.kitodo.data.database.beans.LtpValidationConfiguration
     */
    public void setConfiguration(LtpValidationConfiguration configuration) {
        this.configuration = configuration;
    }

    /**
     * Returns the list of possible mime types that are currently supported for
     * validation.
     *
     * @return possible mime types
     */
    public Map<String, String> getMimeTypes() {
        if (mimeTypes.isEmpty()) {
            try {
                Locale language = FacesContext.getCurrentInstance().getViewRoot().getLocale();
                List<LanguageRange> languages = Collections.singletonList(new LanguageRange(language.toLanguageTag()));
                mimeTypes = FileFormatsConfig.getFileFormats().parallelStream()
                        .filter((ff) -> ff.getFileType().isPresent())
                        .collect(Collectors.toMap(locale -> locale.getLabel(languages), FileFormat::getMimeType,
                            (prior, recent) -> recent, TreeMap::new));
            } catch (JAXBException | RuntimeException e) {
                Helper.setErrorMessage(ERROR_READING, new Object[] {e.getMessage() }, logger, e);
            }
        }
        return mimeTypes;
    }

    /**
     * Return Primefaces select map with all possible operations for a
     * validation condition.
     * 
     * @return the map of translated label to operation name of all possible
     *         operations
     */
    public Map<String, String> getPossibleOperationsSelectMap() {
        Map<String, String> map = new TreeMap<>();
        for (LtpValidationConditionOperation operation : LtpValidationConditionOperation.values()) {
            map.put(LtpValidationHelper.translateConditionOperation(operation), operation.name());
        }
        return map;
    }

    /**
     * Return Primefaces select map with all possible failure severities for a
     * validation condition.
     * 
     * @return the map of translated severity label to severity name
     */
    public Map<String, String> getPossibleSeveritiesSelectMap() {
        Map<String, String> map = new TreeMap<>();
        for (LtpValidationConditionSeverity severity : LtpValidationConditionSeverity.values()) {
            map.put(LtpValidationHelper.translateConditionSeverity(severity), severity.name());
        }
        return map;
    }

    /**
     * Adds an empty validation condition to the list of conditions if the user
     * clicks on the add validation condition buttton.
     */
    public void addValidationCondition() {
        LtpValidationCondition condition = new LtpValidationCondition();
        condition.setProperty("");
        condition.setOperation(LtpValidationConditionOperation.EQUAL);
        condition.setValues(Collections.emptyList());
        condition.setLtpValidationConfiguration(configuration);
        condition.setSeverity(LtpValidationConditionSeverity.WARNING);
        if (Objects.isNull(this.configuration.getValidationConditions())) {
            this.configuration.setValidationConditions(new ArrayList<>());
        }
        this.configuration.getValidationConditions().add(condition);
    }

    /**
     * Removes a validation condition from the list of conditions if the user
     * clicks on the tash icon for a validation condition.
     * 
     * @param index
     *            the index of the condition that is supposed to be removed
     */
    public void removeValidationCondition(int index) {
        if (Objects.nonNull(configuration.getValidationConditions()) && index >= 0
                && index < configuration.getValidationConditions().size()) {
            configuration.getValidationConditions().remove(index);
        }
    }

    /**
     * Adds a simple equals validation condition to the list of conditions.
     * 
     * @param property
     *            the property of the validation condition
     * @param operation
     *            the operation of the validation condition
     * @param value
     *            the value to be checked against
     * @param severity
     *            the severity of the validation condition
     */
    private void addSimpleCondition(String property, LtpValidationConditionOperation operation, String value,
            LtpValidationConditionSeverity severity) {
        LtpValidationCondition condition = new LtpValidationCondition();
        condition.setProperty(property);
        condition.setOperation(operation);
        condition.setValues(Collections.singletonList(value));
        condition.setLtpValidationConfiguration(configuration);
        condition.setSeverity(severity);
        if (Objects.isNull(this.configuration.getValidationConditions())) {
            this.configuration.setValidationConditions(new ArrayList<>());
        }
        this.configuration.getValidationConditions().add(condition);
    }

    /**
     * Returns true if the provided validation condition is a condition matching
     * the given property and operation, independent of its value.
     * 
     * @param condition
     *            the condition to be checked
     * @param property
     *            the expected property
     * @param operation
     *            the expected operation
     * @return true if the condition is a condition matching the given property
     *         and operation, indepedent of its value
     */
    private boolean isSimpleCondition(LtpValidationCondition condition, String property,
            LtpValidationConditionOperation operation) {
        if (Objects.nonNull(condition) && Objects.nonNull(condition.getProperty())
                && condition.getProperty().toLowerCase().equals(property.toLowerCase())
                && Objects.nonNull(condition.getOperation()) && condition.getOperation().equals(operation)) {
            return true;
        }
        return false;
    }

    /**
     * Finds and returns a validation condition matching a property and
     * operation, independent of its value.
     * 
     * @param property
     *            the property
     * @param operation
     *            the operation
     * @return the validation condition that matches the given property,
     *         operation and value, or null if not found
     */
    private LtpValidationCondition findSimpleCondition(String property, LtpValidationConditionOperation operation) {
        if (Objects.isNull(configuration.getValidationConditions())) {
            return null;
        }
        for (LtpValidationCondition condition : configuration.getValidationConditions()) {
            if (isSimpleCondition(condition, property, operation)) {
                return condition;
            }
        }
        return null;
    }

    /**
     * Finds and removes a simple validation condition matching the provided
     * condition.
     * 
     * @param condition
     *            the simple condition to be removed
     */
    private void findAndRemoveSimpleCondition(LtpValidationCondition condition) {
        if (Objects.nonNull(condition) && Objects.nonNull(configuration.getValidationConditions())
                && configuration.getValidationConditions().contains(condition)) {
            configuration.getValidationConditions().remove(condition);
            condition.setLtpValidationConfiguration(null);
        }
    }

    /**
     * Add, edits or removes a simple equals condition depending on the severity
     * level.
     * 
     * @param property
     *            the property of the condition
     * @param operation
     *            the operation of the condition
     * @param value
     *            the value of the condition
     * @param severity
     *            the severity of the condition (if null, condition will be
     *            removed)
     */
    private void addOrRemoveSimpleCondition(String property, LtpValidationConditionOperation operation, String value,
            LtpValidationConditionSeverity severity) {
        if (Objects.isNull(configuration.getValidationConditions())) {
            configuration.setValidationConditions(new ArrayList<>());
        }
        LtpValidationCondition condition = findSimpleCondition(property, operation);
        if (Objects.nonNull(severity)) {
            // user requires condition on file, add or edit it
            if (Objects.nonNull(condition) && Objects.nonNull(value)) {
                condition.setSeverity(severity);
                condition.setValues(Collections.singletonList(value));
            } else {
                addSimpleCondition(property, operation, value, severity);
            }
        } else {
            // remove condition
            findAndRemoveSimpleCondition(condition);
        }
    }

    /**
     * Returns the severity of a validation conditions that checks whether a
     * file is considered well-formed.
     * 
     * @return the severity of a validation condition that checks the
     *         well-formed property or null if not such condition exists
     */
    public LtpValidationConditionSeverity getSimpleWellFormedCondition() {
        LtpValidationCondition condition = findSimpleCondition(PROPERTY_WELLFORMED,
            LtpValidationConditionOperation.EQUAL);
        if (Objects.nonNull(condition)) {
            return condition.getSeverity();
        }
        return null;
    }

    /**
     * Sets the severity of a validation condition that checks whether a file is
     * considered well-formed.
     * 
     * @param severity
     *            the severity or null (if condition is not wanted and should be
     *            removed)
     */
    public void setSimpleWellFormedCondition(LtpValidationConditionSeverity severity) {
        simpleWellFormedSeverity = severity;
    }

    /**
     * Is called after the wellformed option was changed by the user. The new
     * value is already stored in <em>simpleWellFormedSeverity</em>.
     */
    public void onSimpleWellFormedConditionChange() {
        addOrRemoveSimpleCondition(PROPERTY_WELLFORMED, LtpValidationConditionOperation.EQUAL, VALUE_TRUE,
            simpleWellFormedSeverity);
    }

    /**
     * Returns the severity of a validation conditions that checks whether a
     * file is considered valid.
     * 
     * @return the severity of a validation condition that checks the valid
     *         property or null if not such condition exists
     */
    public LtpValidationConditionSeverity getSimpleValidCondition() {
        LtpValidationCondition condition = findSimpleCondition(PROPERTY_VALID, LtpValidationConditionOperation.EQUAL);
        if (Objects.nonNull(condition)) {
            return condition.getSeverity();
        }
        return null;
    }

    /**
     * Sets the severity of a validation condition that checks whether a file is
     * considered valid.
     * 
     * @param severity
     *            the severity or null (if condition is not wanted and should be
     *            removed)
     */
    public void setSimpleValidCondition(LtpValidationConditionSeverity severity) {
        simpleValidSeverity = severity;
    }

    /**
     * Is called after the user changed the severity of the valid option.
     */
    public void onSimpleValidConditionChange() {
        addOrRemoveSimpleCondition(PROPERTY_VALID, LtpValidationConditionOperation.EQUAL, VALUE_TRUE,
            simpleValidSeverity);
    }

    /**
     * Return the filename pattern that needs to match each file of a folder.
     * 
     * @return the filename pattern
     */
    public String getSimpleFilenamePattern() {
        LtpValidationCondition condition = findSimpleCondition(PROPERTY_FILENAME,
            LtpValidationConditionOperation.MATCHES);
        if (Objects.nonNull(condition) && Objects.nonNull(condition.getValues()) && condition.getValues().size() == 1) {
            return condition.getValues().getFirst();
        }
        return simpleFilenamePattern;
    }

    /**
     * Sets the filename pattern that needs to match each file of a folder.
     * 
     * @param simpleFilenamePattern
     *            the filname pattern
     */
    public void setSimpleFilenamePattern(String simpleFilenamePattern) {
        this.simpleFilenamePattern = simpleFilenamePattern;
    }

    /**
     * Is called when the user changes the provided filename pattern.
     */
    public void onSimpleFilenamePatternChange() {
        addOrRemoveSimpleCondition(PROPERTY_FILENAME, LtpValidationConditionOperation.MATCHES, simpleFilenamePattern,
            simpleFilenamePatternSeverity);
    }

    /**
     * Return the severity of a validation condition matching the filename to a
     * provided pattern.
     * 
     * @return the severity of a validation condition matching the filename to a
     *         provided pattern
     */
    public LtpValidationConditionSeverity getSimpleFilenamePatternSeverity() {
        LtpValidationCondition condition = findSimpleCondition(PROPERTY_FILENAME,
            LtpValidationConditionOperation.MATCHES);
        if (Objects.nonNull(condition)) {
            return condition.getSeverity();
        }
        return null;
    }

    /**
     * Sets the severity of the validation condition matching the filename to a
     * provided pattern.
     * 
     * @param severity
     *            the severity
     */
    public void setSimpleFilenamePatternSeverity(LtpValidationConditionSeverity severity) {
        simpleFilenamePatternSeverity = severity;
    }

    /**
     * Is called when the user changes the severity for the validation condition
     * matching the filename to a provided pattern.
     */
    public void onSimpleFilenamePatternSeverityChange() {
        addOrRemoveSimpleCondition(PROPERTY_FILENAME, LtpValidationConditionOperation.MATCHES, simpleFilenamePattern,
            simpleFilenamePatternSeverity);
    }
}
