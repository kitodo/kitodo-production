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

import java.io.Serializable;
import java.util.List;

import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;

import org.kitodo.data.database.beans.ImportConfiguration;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.production.helper.Helper;
import org.kitodo.production.services.ServiceManager;

@ViewScoped
@Named("SelectImportConfigurationDialogView")
public class SelectImportConfigurationDialogView implements Serializable {

    private int selectedImportConfigurationId = 0;
    private List<ImportConfiguration> importConfigurations;

    /**
     * Standard constructor.
     */
    public SelectImportConfigurationDialogView() {
        try {
            importConfigurations = ServiceManager.getImportConfigurationService().getAll();
        } catch (DAOException e) {
            Helper.setErrorMessage(e.getMessage());
        }
    }

    /**
     * Get ID of selected import configuration.
     *
     * @return ID of selected import configuration
     */
    public int getSelectedImportConfigurationId() {
        return selectedImportConfigurationId;
    }

    /**
     * Set ID of selected import configuration.
     *
     * @param selectedImportConfigurationId ID of selected import configuration
     */
    public void setSelectedImportConfigurationId(int selectedImportConfigurationId) {
        this.selectedImportConfigurationId = selectedImportConfigurationId;
    }

    /**
     * Get list of available import configurations.
     *
     * @return list of available import configurations
     */
    public List<ImportConfiguration> getImportConfigurations() {
        return importConfigurations;
    }

    /**
     * Set list of available import configurations.
     *
     * @param importConfigurations list of available import configurations
     */
    public void setImportConfigurations(List<ImportConfiguration> importConfigurations) {
        this.importConfigurations = importConfigurations;
    }
}
