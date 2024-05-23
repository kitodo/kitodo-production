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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.kitodo.data.database.beans.Filter;
import org.kitodo.data.database.enums.IndexAction;
import org.kitodo.data.database.exceptions.DAOException;

public class FilterDaoIT {

    @Test
    public void runPersistenceSuitTest() throws DAOException {
        List<Filter> filters = getAuthorities();

        FilterDAO filterDAO = new FilterDAO();
        filterDAO.save(filters.get(0));
        filterDAO.save(filters.get(1));
        filterDAO.save(filters.get(2));

        assertEquals(3, filterDAO.getAll().size(), "Objects were not saved or not found!");
        assertEquals(2, filterDAO.getAll(1,2).size(), "Objects were not saved or not found!");
        assertEquals("first_filter", filterDAO.getById(1).getValue(), "Object was not saved or not found!");

        filterDAO.remove(1);
        filterDAO.remove(filters.get(1));
        assertEquals(1, filterDAO.getAll().size(), "Objects were not removed or not found!");

        Exception exception = assertThrows(DAOException.class,
                () -> filterDAO.getById(1));
        assertEquals("Object cannot be found in database", exception.getMessage());
    }

    private List<Filter> getAuthorities() {
        Filter firstFilter = new Filter();
        firstFilter.setValue("first_filter");
        firstFilter.setIndexAction(IndexAction.DONE);

        Filter secondFilter = new Filter();
        secondFilter.setValue("second_filter");
        secondFilter.setIndexAction(IndexAction.INDEX);

        Filter thirdFilter = new Filter();
        thirdFilter.setValue("third_filter");

        List<Filter> filters = new ArrayList<>();
        filters.add(firstFilter);
        filters.add(secondFilter);
        filters.add(thirdFilter);
        return filters;
    }
}
