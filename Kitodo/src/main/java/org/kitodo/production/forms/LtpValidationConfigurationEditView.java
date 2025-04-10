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
                configuration = ServiceManager.getLongTermPreservationValidationService().getById(id);
            } else {
                configuration = new LtpValidationConfiguration();
            }
            setSaveDisabled(true);
        } catch (DAOException e) {
            Helper.setErrorMessage(ERROR_LOADING_ONE,
                    new Object[] { ObjectType.MAPPING_FILE.getTranslationSingular(), id }, logger, e);
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
            Helper.setErrorMessage(ERROR_SAVING, new Object[] {ObjectType.MAPPING_FILE.getTranslationSingular() }, logger, e);
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


    public Map<String, String> getPossibleOperations() {
        Map<String, String> map = new TreeMap<>();
        map.put("Equal to", LtpValidationConditionOperation.EQUAL.name());
        map.put("Not equal to", LtpValidationConditionOperation.NOT_EQUAL.name());
        map.put("Larger than", LtpValidationConditionOperation.LARGER_THAN.name());
        map.put("Smaller than", LtpValidationConditionOperation.SMALLER_THAN.name());
        map.put("contained in", LtpValidationConditionOperation.CONTAINED_IN.name());
        return map;
    }

    public Map<String, String> getPossibleSeverities() {
        Map<String, String> map = new TreeMap<>();
        map.put("Warning", LtpValidationConditionSeverity.WARNING.name());
        map.put("Error", LtpValidationConditionSeverity.ERROR.name());
        return map;
    }

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

    public void removeValidationCondition(LtpValidationCondition condition) {
        if (Objects.nonNull(condition) 
                && Objects.nonNull(configuration.getValidationConditions()) 
                && configuration.getValidationConditions().contains(condition)) {
            configuration.getValidationConditions().remove(condition);
        }
    }

}
