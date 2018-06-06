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

import org.junit.Assert;
import org.junit.Test;
import org.kitodo.selenium.testframework.BaseTestSelenium;
import org.kitodo.selenium.testframework.Pages;

public class RemovingST extends BaseTestSelenium {

    @Test
    public void removeProcessTest() throws Exception {
        int numberOfProcessesDisplayed = Pages.getProcessesPage().countListedProcesses();
        Assert.assertTrue("Process list is empty", numberOfProcessesDisplayed > 0);
        Pages.getProcessesPage().deleteFirstProcess();
        Assert.assertTrue("Removal of first process was not successful!",
                Pages.getProcessesPage().countListedProcesses() < numberOfProcessesDisplayed);
    }
    
}
