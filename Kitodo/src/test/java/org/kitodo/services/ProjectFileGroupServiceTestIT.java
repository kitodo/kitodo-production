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
import org.junit.Test;

import org.kitodo.MockDatabase;
import org.kitodo.data.database.beans.ProjectFileGroup;
import org.kitodo.data.database.exceptions.DAOException;

import static org.junit.Assert.*;

/**
 * Tests for TaskService class.
 */
public class ProjectFileGroupServiceTestIT {

    @BeforeClass
    public static void prepareDatabase() throws DAOException {
        MockDatabase.insertProcessesFull();
    }

    @Test
    public void shouldFindHistory() throws Exception {
        ProjectFileGroupService projectFileGroupService = new ProjectFileGroupService();

        ProjectFileGroup projectFileGroup = projectFileGroupService.find(1);
        boolean condition = projectFileGroup.getName().equals("MAX") && projectFileGroup.getMimeType().equals("image/jpeg");
        assertTrue("Project file group was not found in database!", condition);
    }
}
