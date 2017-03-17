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

import org.kitodo.data.database.beans.ProcessProperty;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.data.database.persistence.ProcessPropertyDAO;

import java.util.List;

public class ProcessPropertyService {

    private ProcessPropertyDAO processPropertyDao = new ProcessPropertyDAO();

    public void save(ProcessProperty processProperty) throws DAOException {
        processPropertyDao.save(processProperty);
    }

    public ProcessProperty find(Integer id) throws DAOException {
        return processPropertyDao.find(id);
    }

    public List<ProcessProperty> findAll() throws DAOException {
        return processPropertyDao.findAll();
    }

    public void remove(ProcessProperty processProperty) throws DAOException {
        processPropertyDao.remove(processProperty);
    }

    public String getNormalizedTitle(ProcessProperty processProperty) {
        //why trim after replace spaces to _, it should have reversed order
        return processProperty.getTitle().replace(" ", "_").trim();
    }

    public String getNormalizedValue(ProcessProperty processProperty) {
        return processProperty.getValue().replace(" ", "_").trim();
    }
}
