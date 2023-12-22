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

import org.apache.commons.configuration.ConfigurationException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kitodo.config.OPACConfig;
import org.kitodo.data.database.beans.ImportConfiguration;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.exceptions.ConfigException;
import org.kitodo.exceptions.ImportConfigurationInUseException;
import org.kitodo.production.enums.ObjectType;
import org.kitodo.production.helper.Helper;
import org.kitodo.production.model.LazyDTOModel;
import org.kitodo.production.services.ServiceManager;
import org.primefaces.PrimeFaces;

@Named("ImportConfigurationListView")
@ViewScoped
public class ImportConfigurationListView extends BaseForm {

    private static final Logger logger = LogManager.getLogger(ImportConfigurationListView.class);
    private final String importConfigurationEditPath = MessageFormat.format(REDIRECT_PATH, "importConfigurationEdit");

    /**
     * Empty default constructor that also sets the LazyDTOModel instance of this bean.
     */
    public ImportConfigurationListView() {
        super();
        super.setLazyDTOModel(new LazyDTOModel(ServiceManager.getImportConfigurationService()));
    }

    /**
     * Get import configurations.
     *
     * @return import configurations
     */
    public List<ImportConfiguration> getImportConfigurations() {
        try {
            return ServiceManager.getImportConfigurationService().getAll();
        } catch (DAOException e) {
            Helper.setErrorMessage(ERROR_LOADING_MANY,
                    new Object[] { ObjectType.IMPORT_CONFIGURATION.getTranslationPlural() }, logger, e);
            return new ArrayList<>();
        }
    }

    /**
     * Return path to 'importConfigurationEdit' view.
     *
     * @return path to 'importConfigurationEdit' view
     */
    public String newImportConfiguration() {
        return importConfigurationEditPath;
    }

    /**
     * Delete import configuration identified by ID.
     *
     * @param id ID of import configuration to delete
     */
    public void deleteById(int id) {
        try {
            ServiceManager.getImportConfigurationService().removeFromDatabase(id);
        } catch (DAOException e) {
            Helper.setErrorMessage(ERROR_DELETING, new Object[] {ObjectType.IMPORT_CONFIGURATION.getTranslationSingular() }, logger, e);
        } catch (ImportConfigurationInUseException e) {
            Helper.setErrorMessage(
                    Helper.getTranslation(ERROR_DELETING, ObjectType.IMPORT_CONFIGURATION.getTranslationSingular()),
                    e.getMessage());
        }
    }

    /**
     * Start import of catalog configurations from 'kitodo_opac.xml' file.
     */
    public void startCatalogConfigurationImport() {
        try {
            OPACConfig.getKitodoOpacConfiguration();
            PrimeFaces.current().executeScript("PF('importCatalogConfigurationsDialog').show();");
        } catch (ConfigurationException e) {
            Helper.setErrorMessage(e.getMessage() + ": " + e.getCause().getMessage());
        } catch (ConfigException e) {
            Helper.setErrorMessage(e.getMessage());
        }
    }
}
