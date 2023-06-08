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

package org.kitodo.production.services.data;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.kitodo.api.externaldatamanagement.ImportConfigurationType;
import org.kitodo.data.database.beans.ImportConfiguration;
import org.kitodo.data.database.beans.Project;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.data.database.persistence.ImportConfigurationDAO;
import org.kitodo.data.exceptions.DataException;
import org.kitodo.exceptions.ImportConfigurationInUseException;
import org.kitodo.production.services.ServiceManager;
import org.kitodo.production.services.data.base.SearchDatabaseService;
import org.primefaces.model.SortOrder;

public class ImportConfigurationService extends SearchDatabaseService<ImportConfiguration, ImportConfigurationDAO> {

    private static volatile ImportConfigurationService instance = null;

    /**
     * Standard constructor.
     */
    public ImportConfigurationService() {
        super(new ImportConfigurationDAO());
    }

    /**
     * Return singleton variable of type OpacConfigurationService.
     *
     * @return unique instance of OpacConfigurationService
     */
    public static ImportConfigurationService getInstance() {
        ImportConfigurationService localReference = instance;
        if (Objects.isNull(localReference)) {
            synchronized (ImportConfigurationService.class) {
                localReference = instance;
                if (Objects.isNull(localReference)) {
                    localReference = new ImportConfigurationService();
                    instance = localReference;
                }
            }
        }
        return localReference;
    }

    /**
     * Load data for frontend lists. Data can be loaded from database or index.
     *
     * @param first     searched objects
     * @param pageSize  size of page
     * @param sortField field by which data should be sorted
     * @param sortOrder order ascending or descending
     * @param filters   for search query
     * @return loaded data
     */
    @Override
    @SuppressWarnings("unchecked")
    public List<ImportConfiguration> loadData(int first, int pageSize, String sortField, SortOrder sortOrder, Map filters) {
        return dao.getByQuery("FROM ImportConfiguration"  + getSort(sortField, sortOrder), filters, first, pageSize);
    }

    /**
     * Count all rows in database.
     *
     * @return amount of all rows
     */
    @Override
    public Long countDatabaseRows() throws DAOException {
        return countDatabaseRows("SELECT COUNT(*) FROM ImportConfiguration");
    }

    /**
     * This function is used for count amount of results for frontend lists.
     *
     * @param filters Map of parameters used for filtering
     * @return amount of results
     * @throws DAOException  that can be caused by Hibernate
     * @throws DataException that can be caused by ElasticSearch
     */
    @Override
    public Long countResults(Map filters) throws DAOException, DataException {
        return countDatabaseRows();
    }

    /**
     * Delete import configuration identified by ID. If the corresponding configuration is currently used as default
     * configuration for a project, an exception is thrown.
     * @param id of import configuration to delete
     * @throws DAOException if import configuration could not be deleted from database
     * @throws ImportConfigurationInUseException if import configuration is assigned as default configuration or
     *         default child configuration to at least one project
     */
    @Override
    public void removeFromDatabase(Integer id) throws DAOException, ImportConfigurationInUseException {
        for (Project project : ServiceManager.getProjectService().getAll()) {
            ImportConfiguration defaultConfiguration = project.getDefaultImportConfiguration();
            if (Objects.nonNull(defaultConfiguration) && Objects.equals(defaultConfiguration.getId(), id)) {
                throw new ImportConfigurationInUseException(defaultConfiguration.getTitle(), project.getTitle());
            }
            ImportConfiguration defaultChildConfiguration = project.getDefaultChildProcessImportConfiguration();
            if (Objects.nonNull(defaultChildConfiguration) && Objects.equals(defaultChildConfiguration.getId(), id)) {
                throw new ImportConfigurationInUseException(defaultChildConfiguration.getTitle(), project.getTitle());
            }
        }
        dao.remove(id);
    }


    /**
     * Load and return all ImportConfigurations of type OPAC_SEARCH.
     * @return list of OPAC_SEARCH type ImportConfigurations
     * @throws DAOException when ImportConfigurations could not be loaded
     */
    public List<ImportConfiguration> getAllOpacSearchConfigurations() throws DAOException {
        return getAllImportConfigurations(ImportConfigurationType.OPAC_SEARCH);
    }

    /**
     * Load and return all ImportConfigurations of type FILE_UPLOAD.
     * @return list of FILE_UPLOAD type ImportConfigurations
     * @throws DAOException when ImportConfigurations could not be loaded
     */
    public List<ImportConfiguration> getAllFileUploadConfigurations() throws DAOException {
        return getAllImportConfigurations(ImportConfigurationType.FILE_UPLOAD);
    }

    /**
     * Load and return all ImportConfigurations sorted by title.
     * @return list of all ImportConfigurations sorted by title
     * @throws DAOException when ImportConfigurations could not be loaded
     */
    public List<ImportConfiguration> getAll() throws DAOException {
        return super.getAll().stream().sorted(Comparator.comparing(ImportConfiguration::getTitle))
                .collect(Collectors.toList());
    }

    private List<ImportConfiguration> getAllImportConfigurations(ImportConfigurationType type) throws DAOException {
        return super.getAll().stream()
                .filter(importConfiguration -> type.name()
                        .equals(importConfiguration.getConfigurationType()))
                .sorted(Comparator.comparing(ImportConfiguration::getTitle))
                .collect(Collectors.toList());
    }
}
