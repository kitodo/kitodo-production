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

package org.kitodo.production.services.validation;

import java.net.URI;
import java.util.List;
import java.util.Map;

import org.kitodo.api.validation.longtermpreservation.FileType;
import org.kitodo.api.validation.longtermpreservation.LongTermPreservationValidationInterface;
import org.kitodo.api.validation.longtermpreservation.LtpValidationConditionInterface;
import org.kitodo.api.validation.longtermpreservation.LtpValidationResult;
import org.kitodo.data.database.beans.LtpValidationConfiguration;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.data.database.persistence.LtpValidationConfigurationDAO;
import org.kitodo.data.exceptions.DataException;
import org.kitodo.production.services.data.base.SearchDatabaseService;
import org.kitodo.serviceloader.KitodoServiceLoader;
import org.primefaces.model.SortOrder;

/**
 * This class does nothing more than call the methods on the long term
 * preservation validation interface.
 */
public class LongTermPreservationValidationService extends SearchDatabaseService<LtpValidationConfiguration, LtpValidationConfigurationDAO> {

    private final LongTermPreservationValidationInterface longTermPreservationValidation;

    public LongTermPreservationValidationService() {
        super(new LtpValidationConfigurationDAO());
        longTermPreservationValidation = getValidationModule();
    }

    /**
     * Loads the module for long-term archival validation.
     *
     * @return the loaded module
     */
    private LongTermPreservationValidationInterface getValidationModule() {
        KitodoServiceLoader<LongTermPreservationValidationInterface> loader = new KitodoServiceLoader<>(
                LongTermPreservationValidationInterface.class);
        return loader.loadModule();
    }

    /**
     * Validates a file for longTimePreservation.
     *
     * @param fileUri
     *            The uri to the image, which should be validated.
     * @param fileType
     *            The fileType of the image at the given path.
     * @return A validation result.
     */
    public LtpValidationResult validate(URI fileUri, FileType fileType, List<? extends LtpValidationConditionInterface> conditions) {
        return longTermPreservationValidation.validate(fileUri, fileType, conditions);
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
    public List<LtpValidationConfiguration> loadData(int first, int pageSize, String sortField, SortOrder sortOrder, Map filters) {
        return dao.getByQuery("FROM LtpValidationConfiguration"  + getSort(sortField, sortOrder), filters, first, pageSize);
    }

    /**
     * Count all rows in database.
     *
     * @return amount of all rows
     */
    @Override
    public Long countDatabaseRows() throws DAOException {
        return countDatabaseRows("SELECT COUNT(*) FROM LtpValidationConfiguration");
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
}
