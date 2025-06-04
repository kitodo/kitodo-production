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
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.kitodo.api.validation.longtermpreservation.FileType;
import org.kitodo.api.validation.longtermpreservation.LongTermPreservationValidationInterface;
import org.kitodo.api.validation.longtermpreservation.LtpValidationConditionInterface;
import org.kitodo.api.validation.longtermpreservation.LtpValidationResult;
import org.kitodo.data.database.beans.LtpValidationConfiguration;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.data.database.persistence.LtpValidationConfigurationDAO;
import org.kitodo.production.services.data.BaseBeanService;
import org.kitodo.serviceloader.KitodoServiceLoader;
import org.primefaces.model.SortOrder;

/**
 * This class does nothing more than call the methods on the long term
 * preservation validation interface.
 */
public class LongTermPreservationValidationService 
        extends BaseBeanService<LtpValidationConfiguration, LtpValidationConfigurationDAO> {

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
     * Return a ltp validation configuration for a specific id but also load the list of attched folders,
     * which is not loaded by default due to the lazy fetch strategy.
     * 
     * @param id the id of the validation configuration that is supposed to be loaded
     * @return the validation configuration including the list of attached folders
     * @throws DAOException in case something goes wrong
     */
    public LtpValidationConfiguration getByIdWithFolders(int id) throws DAOException {
        List<LtpValidationConfiguration> results = dao.getByQuery(
            "SELECT c FROM LtpValidationConfiguration c LEFT JOIN FETCH c.folders WHERE c.id = :id", 
            Collections.singletonMap("id", id)
        );
        if (results.size() != 1) {
            throw new DAOException("Unable to find ltp validation configuration object with ID " + id + "!");
        }
        return results.get(0);
    }

    /**
     * Count all rows in database.
     *
     * @return amount of all rows
     */
    @Override
    public Long count() throws DAOException {
        return count("SELECT COUNT(*) FROM LtpValidationConfiguration");
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
    public Long countResults(Map filters) throws DAOException {
        return count();
    }
}
