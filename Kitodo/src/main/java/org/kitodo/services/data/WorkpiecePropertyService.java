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

package org.kitodo.services.data;

import java.util.List;

import org.kitodo.data.database.beans.WorkpieceProperty;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.data.database.persistence.WorkpiecePropertyDAO;

public class WorkpiecePropertyService {

    private WorkpiecePropertyDAO workpiecePropertyDao = new WorkpiecePropertyDAO();

    public void save(WorkpieceProperty workpieceProperty) throws DAOException {
        workpiecePropertyDao.save(workpieceProperty);
    }

    public WorkpieceProperty find(Integer id) throws DAOException {
        return workpiecePropertyDao.find(id);
    }

    public List<WorkpieceProperty> findAll() throws DAOException {
        return workpiecePropertyDao.findAll();
    }

    public void remove(WorkpieceProperty workpieceProperty) throws DAOException {
        workpiecePropertyDao.remove(workpieceProperty);
    }

    public String getNormalizedTitle(WorkpieceProperty workpieceProperty) {
        return workpieceProperty.getTitle().replace(" ", "_").trim();
    }

    public String getNormalizedValue(WorkpieceProperty workpieceProperty) {
        return workpieceProperty.getValue().replace(" ", "_").trim();
    }
}
