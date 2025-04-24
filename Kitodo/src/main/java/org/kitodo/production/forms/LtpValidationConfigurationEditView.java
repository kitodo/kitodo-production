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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;

import javax.faces.view.ViewScoped;
import javax.inject.Named;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kitodo.api.validation.longtermpreservation.LtpValidationConditionOperation;
import org.kitodo.api.validation.longtermpreservation.LtpValidationConditionSeverity;
import org.kitodo.data.database.beans.LtpValidationCondition;
import org.kitodo.data.database.beans.LtpValidationConfiguration;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.production.enums.ObjectType;
import org.kitodo.production.helper.Helper;
import org.kitodo.production.helper.validation.LtpValidationHelper;
import org.kitodo.production.services.ServiceManager;

@Named("LtpValidationConfigurationEditView")
@ViewScoped
public class LtpValidationConfigurationEditView extends BaseForm {

    private static final Logger logger = LogManager.getLogger(LtpValidationConfigurationEditView.class);
    private LtpValidationConfiguration configuration = new LtpValidationConfiguration();

    /**
     * Load mapping file by ID.
     *
     * @param id
     *            ID of mapping file to load
     */
    public void load(int id) {
        try {
            if (id > 0) {
                configuration = ServiceManager.getLongTermPreservationValidationService().getByIdWithFolders(id);
            } else {
                configuration = new LtpValidationConfiguration();
            }
            setSaveDisabled(true);
        } catch (DAOException e) {
            Helper.setErrorMessage(ERROR_LOADING_ONE,
                    new Object[] { ObjectType.LTP_VALIDATION_CONFIGURATION.getTranslationSingular(), id }, logger, e);
        }
    }

    /**
     * Save mapping file.
     *
     * @return projects page or empty String
     */
    public String save() {
        try {
            ServiceManager.getLongTermPreservationValidationService().saveToDatabase(configuration);
            return projectsPage;
        } catch (DAOException e) {
            Helper.setErrorMessage(ERROR_SAVING, new Object[] {ObjectType.LTP_VALIDATION_CONFIGURATION.getTranslationSingular() }, logger, e);
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
     * @param configuration as org.kitodo.data.database.beans.LtpValidationConfiguration
     */
    public void setConfiguration(LtpValidationConfiguration configuration) {
        this.configuration = configuration;
    }

    /**
     * Return Primefaces select map with all possible operations for a validation condition.
     * 
     * @return the map of translated label to operation name of all possible operations
     */
    public Map<String, String> getPossibleOperationsSelectMap() {
        Map<String, String> map = new TreeMap<>();
        for (LtpValidationConditionOperation operation : LtpValidationConditionOperation.values()) {
            map.put(LtpValidationHelper.translateConditionOperation(operation), operation.name());
        }
        return map;
    }

    /**
     * Return Primefaces select map with all possible failure severities for a validation condition.
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
     * Adds an empty validation condition to the list of conditions if the user clicks on the 
     * add validation condition buttton.
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
     * Removes a validation condition from the list of conditions if the user clicks on the 
     * tash icon for a validation condition.
     * 
     * @param condition the validation condition to be removed
     */
    public void removeValidationCondition(LtpValidationCondition condition) {
        if (Objects.nonNull(condition) 
                && Objects.nonNull(configuration.getValidationConditions()) 
                && configuration.getValidationConditions().contains(condition)) {
            configuration.getValidationConditions().remove(condition);
        }
    }
}
