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

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.kitodo.MockIndex;
import org.kitodo.data.database.beans.Docket;
import org.kitodo.data.database.exceptions.DAOException;

public class DocketDaoIT {

    @BeforeAll
    public static void setUp() throws Exception {
        MockIndex.startNode();
    }

    @AfterAll
    public static void tearDown() throws Exception {
        MockIndex.stopNode();
    }

    @Test
    public void runPersistenceSuitTest() throws DAOException {
        List<Docket> dockets = getAuthorities();

        DocketDAO docketDAO = new DocketDAO();
        docketDAO.save(dockets.get(0));
        docketDAO.save(dockets.get(1));
        docketDAO.save(dockets.get(2));

        assertEquals(3, docketDAO.getAll().size(), "Objects were not saved or not found!");
        assertEquals(2, docketDAO.getAll(1,2).size(), "Objects were not saved or not found!");
        assertEquals("first_docket", docketDAO.getById(1).getTitle(), "Object was not saved or not found!");

        docketDAO.remove(1);
        docketDAO.remove(dockets.get(1));
        assertEquals(1, docketDAO.getAll().size(), "Objects were not removed or not found!");

        Exception exception = assertThrows(DAOException.class,
            () -> docketDAO.getById(1));
        assertEquals("Object cannot be found in database", exception.getMessage());
    }

    private List<Docket> getAuthorities() {
        Docket firstDocket = new Docket();
        firstDocket.setTitle("first_docket");

        Docket secondDocket = new Docket();
        secondDocket.setTitle("second_docket");

        Docket thirdDocket = new Docket();
        thirdDocket.setTitle("third_docket");

        List<Docket> dockets = new ArrayList<>();
        dockets.add(firstDocket);
        dockets.add(secondDocket);
        dockets.add(thirdDocket);
        return dockets;
    }
}
