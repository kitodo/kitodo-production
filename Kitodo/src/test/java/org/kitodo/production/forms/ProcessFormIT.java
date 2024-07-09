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

package org.kitodo.production.forms;

import java.sql.Date;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.kitodo.MockDatabase;
import org.kitodo.SecurityTestUtils;
import org.kitodo.data.database.beans.Process;
import org.kitodo.data.database.beans.Project;
import org.kitodo.data.database.beans.Template;
import org.kitodo.production.services.ServiceManager;

public class ProcessFormIT {

    private ProcessForm processForm = new ProcessForm();

    /**
     * Setup Database and start elasticsearch.
     *
     * @throws Exception If databaseConnection failed.
     */
    @BeforeClass
    public static void prepareDatabase() throws Exception {
        MockDatabase.startNode();
        MockDatabase.insertProcessesFull();
        addProcesses();
        SecurityTestUtils.addUserDataToSecurityContext(ServiceManager.getUserService().getById(1), 1);
    }

    private static void addProcesses() throws Exception {
        Project projectOne = ServiceManager.getProjectService().getById(1);
        Template template = ServiceManager.getTemplateService().getById(1);

        Process forthProcess = new Process();
        forthProcess.setTitle("Forth process");
        LocalDate localDate = LocalDate.of(2020, 3, 20);
        forthProcess.setCreationDate(Date.from(localDate.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant()));
        forthProcess.setWikiField("SelectionTest");
        forthProcess.setDocket(ServiceManager.getDocketService().getById(1));
        forthProcess.setProject(projectOne);
        forthProcess.setRuleset(ServiceManager.getRulesetService().getById(1));
        forthProcess.setTemplate(template);
        ServiceManager.getProcessService().save(forthProcess);

        Process fifthProcess = new Process();
        fifthProcess.setTitle("Fifth process");
        localDate = LocalDate.of(2020, 4, 20);
        fifthProcess.setCreationDate(Date.from(localDate.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant()));
        fifthProcess.setWikiField("SelectionTest");
        fifthProcess.setDocket(ServiceManager.getDocketService().getById(1));
        fifthProcess.setProject(projectOne);
        fifthProcess.setRuleset(ServiceManager.getRulesetService().getById(1));
        fifthProcess.setTemplate(template);
        ServiceManager.getProcessService().save(fifthProcess);
    }

    /**
     * Cleanup the database and stop elasticsearch.
     *
     * @throws Exception if elasticsearch could not been stopped.
     */
    @AfterClass
    public static void cleanDatabase() throws Exception {
        MockDatabase.stopNode();
        MockDatabase.cleanDatabase();
    }

    /**
     * Tests the selection in the process data table.
     */
    @Test
    public void testProcessesSelection() throws Exception {
        List<Integer> selectedIds;
        processForm.setAllSelected(true);

        selectedIds = processForm.getSelectedProcesses()
                .stream().map(Process::getId).sorted().collect(Collectors.toList());
        Assert.assertEquals(new ArrayList<>(Arrays.asList(1, 2, 4, 5)), selectedIds);

        processForm.getExcludedProcessIds().add(4);
        selectedIds = processForm.getSelectedProcesses()
                .stream().map(Process::getId).sorted().collect(Collectors.toList());

        Assert.assertEquals(new ArrayList<>(Arrays.asList(1, 2, 5)), selectedIds);

        processForm.setAllSelected(false);
        selectedIds = processForm.getSelectedProcesses()
                .stream().map(Process::getId).sorted().collect(Collectors.toList());
        Assert.assertEquals(new ArrayList<>(Arrays.asList(1, 2, 5)), selectedIds);
    }
}
