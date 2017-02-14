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

package org.kitodo.services;

import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import org.kitodo.MockDatabase;
import org.kitodo.data.database.beans.Workpiece;
import org.kitodo.data.database.exceptions.DAOException;

import static org.junit.Assert.*;

/**
 * Tests for WorkpieceService class.
 */
public class WorkpieceServiceTestIT {

    @BeforeClass
    public static void prepareDatabase() throws DAOException {
        MockDatabase.insertProcessesFull();
        MockDatabase.insertWorkpieces();
        MockDatabase.insertWorkpieceProperties();
    }

    //weird update on database:
    //Hibernate: update workpieceProperty set choice=?, container=?, creationDate=?, dataType=?, obligatory=?,
    //title=?, value=?, workpiece_id=? where id=?

    @Ignore("problem with lazy fetching?")
    @Test
    public void shouldFindWorkpiece() throws Exception {
        WorkpieceService workpieceService = new WorkpieceService();

        Workpiece workpiece = workpieceService.find(1);
        boolean condition = workpiece.getProperties().size() == 2;
        assertTrue("Workpiece was not found in database!", condition);
    }

    @Ignore("problem with lazy fetching?")
    @Test
    public void shouldGetPropertiesSize() throws Exception {
        WorkpieceService workpieceService = new WorkpieceService();

        Workpiece workpiece = workpieceService.find(1);
        int actual = workpieceService.getPropertiesSize(workpiece);
        assertEquals("Workpiece's properties size is not equal to given value!", 2, actual);
    }
}
