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

import de.sub.goobi.config.ConfigCore;

import java.io.File;

import org.apache.commons.lang.SystemUtils;
import org.junit.Assert;
import org.junit.Test;
import org.kitodo.ExecutionPermission;
import org.kitodo.selenium.testframework.BaseTestSelenium;
import org.kitodo.selenium.testframework.Pages;
import org.kitodo.selenium.testframework.pages.ProcessesPage;

public class RemovingST extends BaseTestSelenium {

    @Test
    public void removeProcessTest() throws Exception {
        if (SystemUtils.IS_OS_LINUX) {
            File scriptCreateDirMeta = new File(ConfigCore.getParameter("script_createDirMeta"));
            File scriptCreateDirUserHome = new File(ConfigCore.getParameter("script_createDirUserHome"));
            ExecutionPermission.setExecutePermission(scriptCreateDirMeta);
            ExecutionPermission.setExecutePermission(scriptCreateDirUserHome);
        }

        ProcessesPage processesPage = Pages.getProcessesPage();
        int numberOfProcessesDisplayed = processesPage.countListedProcesses();
        Assert.assertTrue("Process list is empty", processesPage.isResultSetNotEmpty(processesPage.getListOfProcessTitles()));
        Pages.getProcessesPage().deleteFirstProcess();
        Assert.assertTrue("Removal of first process was not successful!",
                processesPage.countListedProcesses() < numberOfProcessesDisplayed);

        if (SystemUtils.IS_OS_LINUX) {
            File scriptCreateDirMeta = new File(ConfigCore.getParameter("script_createDirMeta"));
            File scriptCreateDirUserHome = new File(ConfigCore.getParameter("script_createDirUserHome"));
            ExecutionPermission.setNoExecutePermission(scriptCreateDirMeta);
            ExecutionPermission.setNoExecutePermission(scriptCreateDirUserHome);
        }
    }
    
}
