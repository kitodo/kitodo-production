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

package org.kitodo.production.process;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kitodo.config.ConfigCore;
import org.kitodo.config.enums.ParameterCore;
import org.kitodo.data.database.beans.Property;
import org.kitodo.data.exceptions.DataException;
import org.kitodo.production.enums.ObjectType;
import org.kitodo.production.helper.Helper;
import org.kitodo.production.process.field.AdditionalField;
import org.kitodo.production.services.ServiceManager;

public final class ProcessValidator {

    private static final Logger logger = LogManager.getLogger(ProcessValidator.class);

    private static final String ERROR_READ = "errorReading";
    private static final String INCOMPLETE_DATA = "errorDataIncomplete";

    private ProcessValidator() {
        // private constructor to hide implicit one
    }

    /**
     * Check if content of process is valid.
     * 
     * @param title
     *            of process for validation
     * @param additionalFields
     *            for process validation
     * @param digitalCollections
     *            as List of Strings
     * @param standardFields
     *            as Map Boolean to String
     * @param criticiseEmptyTitle
     *            true or false
     * @return true or false
     */
    public static boolean isContentValid(String title, List<AdditionalField> additionalFields,
            List<String> digitalCollections, Map<String, Boolean> standardFields, boolean criticiseEmptyTitle) {
        boolean valid = true;

        if (criticiseEmptyTitle) {
            valid = isProcessTitleCorrect(title);
        }

        // Check the standard entries that must be specified

        // no collection selected
        if (standardFields.get("collections") && digitalCollections.isEmpty()) {
            valid = false;
            Helper.setErrorMessage(INCOMPLETE_DATA, "processCreationErrorNoCollection");
        }

        // check the additional inputs that must be specified
        for (AdditionalField field : additionalFields) {
            String value = field.getValue();
            if (StringUtils.isBlank(value) && field.isRequired() && field.showDependingOnDoctype()) {
                valid = false;
                Helper.setErrorMessage(INCOMPLETE_DATA, "processCreationErrorFieldIsEmpty");
            }
        }
        return valid;
    }

    /**
     * Check if process title is correct.
     * 
     * @param title
     *            of the process for validation
     * @return true or false
     */
    public static boolean isProcessTitleCorrect(String title) {
        boolean valid = true;

        if (StringUtils.isBlank(title)) {
            valid = false;
            Helper.setErrorMessage(INCOMPLETE_DATA, "processTitleEmpty");
        }

        String validateRegEx = ConfigCore.getParameterOrDefaultValue(ParameterCore.VALIDATE_PROCESS_TITLE_REGEX);
        if (Objects.isNull(title) || !title.matches(validateRegEx)) {
            valid = false;
            Helper.setErrorMessage("processTitleInvalid", new Object[] {validateRegEx });
        }

        if (valid) {
            valid = isProcessTitleAvailable(title);
        }

        return valid;
    }

    /**
     * Check if property already exists, if yes set value for it.
     * 
     * @param properties
     *            existing for process
     * @param property
     *            for verification
     * @return true if property already exists, otherwise false
     */
    public static boolean existsProperty(List<Property> properties, Property property) {
        for (Property tempProperty : properties) {
            if (tempProperty.getTitle().equals(property.getTitle())) {
                tempProperty.setValue(property.getValue());
                return true;
            }
        }
        return false;
    }

    /**
     * Checks if process title is available. If yes, return true, if no, return
     * false.
     *
     * @param title
     *            of process for checking availability
     * @return true if process title is not used, false if otherwise or title is
     *         null
     */
    public static boolean isProcessTitleAvailable(String title) {
        if (Objects.nonNull(title)) {
            long amount;
            try {
                amount = ServiceManager.getProcessService().findNumberOfProcessesWithTitle(title);
            } catch (DataException e) {
                Helper.setErrorMessage(ERROR_READ, new Object[] {ObjectType.PROCESS.getTranslationSingular() }, logger,
                    e);
                return false;
            }
            if (amount > 0) {
                Helper.setErrorMessage(INCOMPLETE_DATA, "processTitleAlreadyInUse");
                return false;
            }
            return true;
        }
        return false;
    }
}
