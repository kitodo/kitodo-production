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

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.kitodo.data.database.beans.Docket;
import org.kitodo.data.database.enums.IndexAction;
import org.kitodo.data.database.exceptions.DAOException;

public class DocketDaoIT {

    @Rule
    public final ExpectedException exception = ExpectedException.none();

    @Test
    public void runPersistenceSuitTest() throws DAOException {
        List<Docket> dockets = getAuthorities();

        DocketDAO docketDAO = new DocketDAO();
        docketDAO.save(dockets.get(0));
        docketDAO.save(dockets.get(1));
        docketDAO.save(dockets.get(2));

        assertEquals("Objects were not saved or not found!", 3, docketDAO.getAll().size());
        assertEquals("Objects were not saved or not found!", 2, docketDAO.getAll(1,2).size());
        assertEquals("Object was not saved or not found!", "first_docket", docketDAO.getById(1).getTitle());

        docketDAO.remove(1);
        docketDAO.remove(dockets.get(1));
        assertEquals("Objects were not removed or not found!", 1, docketDAO.getAll().size());

        exception.expect(DAOException.class);
        exception.expectMessage("Object cannot be found in database");
        docketDAO.getById(1);
    }

    private List<Docket> getAuthorities() {
        Docket firstDocket = new Docket();
        firstDocket.setTitle("first_docket");
        firstDocket.setIndexAction(IndexAction.DONE);

        Docket secondDocket = new Docket();
        secondDocket.setTitle("second_docket");
        secondDocket.setIndexAction(IndexAction.INDEX);

        Docket thirdDocket = new Docket();
        thirdDocket.setTitle("third_docket");

        List<Docket> dockets = new ArrayList<>();
        dockets.add(firstDocket);
        dockets.add(secondDocket);
        dockets.add(thirdDocket);
        return dockets;
    }
}
