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

package org.kitodo.data.database.persistence;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

import org.kitodo.data.database.beans.OcrProfile;
import org.kitodo.data.database.exceptions.DAOException;

public class OcrProfileDAO extends BaseDAO<OcrProfile> {

    @Override
    public OcrProfile getById(Integer id) throws DAOException {
        OcrProfile ocrProfile = retrieveObject(OcrProfile.class, id);
        if (Objects.isNull(ocrProfile)) {
            throw new DAOException("Object cannot be found in database");
        }
        return ocrProfile;
    }

    @Override
    public List<OcrProfile> getAll() throws DAOException {
        return retrieveAllObjects(OcrProfile.class);
    }

    @Override
    public List<OcrProfile> getAll(int offset, int size) throws DAOException {
        return retrieveObjects("FROM OcrProfile ORDER BY id ASC", offset, size);
    }

    @Override
    public List<OcrProfile> getAllNotIndexed(int offset, int size) throws DAOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void remove(Integer ocrProfileId) throws DAOException {
        removeObject(OcrProfile.class, ocrProfileId);
    }

    /**
     * Get available ocr profile - available means that ocr profile is assigned to client with given id.
     *
     * @param clientId
     *         id of client to which searched ocr profiles should be assigned
     * @return list of available ocr profiles objects
     */
    public List<OcrProfile> getAvailableOcrProfiles(int clientId) {
        return getByQuery("SELECT w FROM OcrProfile AS w INNER JOIN w.client AS c WITH c.id = :clientId",
                Collections.singletonMap("clientId", clientId));
    }

}
