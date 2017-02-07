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
import org.kitodo.data.database.beans.Process;
import org.kitodo.data.database.exceptions.DAOException;

import static org.junit.Assert.*;
import static org.kitodo.data.database.beans.Batch.Type.LOGISTIC;

/**
 * Tests for ProcessService class.
 */
public class ProcessServiceTest {

    @BeforeClass
    public static void prepareDatabase() throws DAOException {
        MockDatabase.insertBatches();
        MockDatabase.insertDockets();
        MockDatabase.insertUsers();
        MockDatabase.insertUserGroups();
        MockDatabase.insertProjects();
        MockDatabase.insertRulesets();
        MockDatabase.insertProcesses();
        MockDatabase.insertTasks();
    }

    @Test
    public void shouldGetBatchesByType() throws Exception {
        ProcessService processService = new ProcessService();

        Process process = processService.find(1);
        boolean condition = processService.getBatchesByType(process, LOGISTIC).size() == 1;
        assertTrue("Table size is incorrect!", condition);
    }

    @Test
    public void shouldGetBlockedUsers() throws Exception {
        ProcessService processService = new ProcessService();
        UserService userService = new UserService();

        Process process = processService.find(1);
        System.out.println(process.getTitle() + " " + process.getDocket().getName());
        boolean condition = processService.getBlockedUsers(process) == userService.find(3);
        assertTrue("Blocked user doesn't match to given user!", condition);
    }

    @Test
    public void shouldGetImagesTifDirectory() throws Exception {
        ProcessService processService = new ProcessService();

        Process process = processService.find(1);
        System.out.println(processService.getImagesTifDirectory(false, process));
        boolean condition = processService.getImagesTifDirectory(false, process).equals("");
        assertTrue("Images tif directory doesn't match to given directory!", condition);
    }

    @Test
    public void shouldGetProgress() throws Exception {
        ProcessService processService = new ProcessService();

        Process process = processService.find(1);
        System.out.println(processService.getProgress(process));
        boolean condition = processService.getProgress(process).equals("");
        assertTrue("Images tif directory doesn't match to given directory!", condition);
    }
}
