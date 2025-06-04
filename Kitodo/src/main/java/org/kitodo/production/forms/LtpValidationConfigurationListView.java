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
import java.util.List;

import javax.faces.view.ViewScoped;
import javax.inject.Named;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kitodo.data.database.beans.LtpValidationConfiguration;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.production.enums.ObjectType;
import org.kitodo.production.helper.Helper;
import org.kitodo.production.model.LazyBeanModel;
import org.kitodo.production.services.ServiceManager;

@Named("LtpValidationConfigurationListView")
@ViewScoped
public class LtpValidationConfigurationListView extends BaseForm {

    private static final Logger logger = LogManager.getLogger(LtpValidationConfigurationListView.class);
    private final String ltpValidationConfigurationEditPath = MessageFormat.format(REDIRECT_PATH, "ltpValidationConfigurationEdit");

    /**
     * Empty default constructor that also sets the LazyDTOModel instance of this bean.
     */
    public LtpValidationConfigurationListView() {
        super();
        super.setLazyBeanModel(new LazyBeanModel(ServiceManager.getLongTermPreservationValidationService()));
    }

    /**
     * Get mapping files.
     *
     * @return mapping files
     */
    public List<LtpValidationConfiguration> getLtpValidationConfigurations() {
        try {
            return ServiceManager.getLongTermPreservationValidationService().getAll();
        } catch (DAOException e) {
            Helper.setErrorMessage(ERROR_LOADING_MANY,
                    new Object[] {ObjectType.LTP_VALIDATION_CONFIGURATION.getTranslationPlural() }, logger, e);
            return new ArrayList<>();
        }
    }

    /**
     * Return path to 'ltpValidationConfigurationEdit' view.
     *
     * @return path to 'ltpValidationConfigurationEdit' view
     */
    public String newLtpValidationConfiguration() {
        return ltpValidationConfigurationEditPath;
    }

    /**
     * Delete ltp validation configuration identified by ID.
     *
     * @param id ID of ltp validation configuration to delete
     */
    public void deleteById(int id) {
        try {
            ServiceManager.getLongTermPreservationValidationService().remove(id);
        } catch (DAOException e) {
            Helper.setErrorMessage(
                ERROR_DELETING, 
                new Object[] {ObjectType.LTP_VALIDATION_CONFIGURATION.getTranslationSingular() }, 
                logger, e
            );
        }
    }

}
