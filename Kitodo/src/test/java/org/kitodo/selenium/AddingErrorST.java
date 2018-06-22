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
import org.kitodo.data.database.beans.Docket;
import org.kitodo.data.database.beans.Ruleset;
import org.kitodo.selenium.testframework.BaseTestSelenium;
import org.kitodo.selenium.testframework.Pages;

public class AddingErrorST extends BaseTestSelenium {

    @Test
    public void addDocketWithErrorTest() throws Exception {
        Docket docket = new Docket();
        docket.setTitle("MockDocket");
        docket.setFile("file.xml");
        String errorMessage = Pages.getProjectsPage().goTo().createNewDocket().insertDocketData(docket).saveWithError();
        boolean errorMessageExist = errorMessage.equals("Die angegebene Datei konnte nicht gefunden werden.");
        Assert.assertTrue("Error message was not displayed!", errorMessageExist);

    }

    @Test
    public void addRulesetWithErrorTest() throws Exception {
        Ruleset ruleset = new Ruleset();
        ruleset.setTitle("MockRuleset");
        ruleset.setFile("file.xml");
        String errorMessage = Pages.getProjectsPage().goTo().createNewRuleset().insertRulesetData(ruleset)
                .saveWithError();
        boolean errorMessageExist = errorMessage.equals("Die angegebene Datei konnte nicht gefunden werden.");
        Assert.assertTrue("Error message was not displayed!", errorMessageExist);
    }
}
