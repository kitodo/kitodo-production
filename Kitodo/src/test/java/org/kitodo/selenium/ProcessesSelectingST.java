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

package org.kitodo.selenium;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.sql.Date;
import java.time.LocalDate;
import java.time.ZoneId;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.kitodo.data.database.beans.Process;
import org.kitodo.data.database.beans.Project;
import org.kitodo.data.database.beans.Template;
import org.kitodo.data.database.beans.User;
import org.kitodo.production.services.ServiceManager;
import org.kitodo.selenium.testframework.BaseTestSelenium;
import org.kitodo.selenium.testframework.Pages;
import org.kitodo.selenium.testframework.pages.ProcessesPage;

public class ProcessesSelectingST extends BaseTestSelenium {

    private static ProcessesPage processesPage;

    /**
     * Set up process selecting tests.
     * @throws Exception as exception
     */
    @BeforeClass
    public static void setup() throws Exception {

        User user = ServiceManager.getUserService().getById(1);
        user.setTableSize(2);
        ServiceManager.getUserService().saveToDatabase(user);
        addProcesses();
        processesPage = Pages.getProcessesPage();
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

    @Before
    public void login() throws Exception {
        Pages.getLoginPage().goTo().performLoginAsAdmin();
    }

    @After
    public void logout() throws Exception {
        Pages.getTopNavigation().logout();
    }

    /**
     * Tests selection of all processes on one page in the processes data table.
     * @throws Exception as exception
     */
    @Test
    public void selectAllProcessesOnPageTest() throws Exception {
        processesPage.goTo();

        processesPage.selectAllRowsOnPage();
        assertEquals(processesPage.countListedSelectedProcesses(), 2);

        processesPage.goToNextPage();
        assertEquals(processesPage.countListedSelectedProcesses(), 0);
    }

    /**
     *Tests selection of all processes in the processes data table.
     * @throws Exception as exception
     */
    @Test
    public void selectAllProcessesTest() throws Exception {
        processesPage.goTo();

        processesPage.selectAllRows();
        assertEquals(processesPage.countListedSelectedProcesses(), 2);

        processesPage.goToNextPage();
        assertEquals(processesPage.countListedSelectedProcesses(), 2);
    }
}
