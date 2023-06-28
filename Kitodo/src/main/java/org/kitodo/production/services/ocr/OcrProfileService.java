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

package org.kitodo.production.services.ocr;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.kitodo.data.database.beans.OcrProfile;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.data.database.persistence.OcrProfileDAO;
import org.kitodo.data.exceptions.DataException;
import org.kitodo.production.services.ServiceManager;
import org.kitodo.production.services.data.base.SearchDatabaseService;
import org.primefaces.model.SortOrder;

public class OcrProfileService extends SearchDatabaseService<OcrProfile, OcrProfileDAO> {

    private static volatile OcrProfileService instance = null;

    /**
     * Constructor necessary to use searcher in child classes.
     */
    private OcrProfileService() {
        super(new OcrProfileDAO());
    }

    /**
     * Return singleton variable of type OcrProfileService.
     *
     * @return unique instance of OcrProfileService
     */
    public static OcrProfileService getInstance() {
        OcrProfileService localReference = instance;
        if (Objects.isNull(localReference)) {
            synchronized (OcrProfileService.class) {
                localReference = instance;
                if (Objects.isNull(localReference)) {
                    localReference = new OcrProfileService();
                    instance = localReference;
                }
            }
        }
        return localReference;
    }


    @Override
    public List loadData(int first, int pageSize, String sortField, SortOrder sortOrder, Map filters) throws DataException {
        return null;
    }

    @Override
    public Long countDatabaseRows() throws DAOException {
        return null;
    }

    @Override
    public Long countResults(Map filters) throws DAOException, DataException {
        return null;
    }

    /**
     * Get available OCR profiles - available means that OCR profile is assigned to selected session client.
     *
     * @return list of available OcrProfile objects
     */
    public List<OcrProfile> getAvailableOcrProfiles() {
        return dao.getAvailableOcrProfiles(ServiceManager.getUserService().getSessionClientId());
    }

}
