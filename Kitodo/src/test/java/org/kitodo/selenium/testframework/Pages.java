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

package org.kitodo.selenium.testframework;

import org.kitodo.selenium.testframework.pages.ClientEditPage;
import org.kitodo.selenium.testframework.pages.ClientsPage;
import org.kitodo.selenium.testframework.pages.DocketEditPage;
import org.kitodo.selenium.testframework.pages.HelpPage;
import org.kitodo.selenium.testframework.pages.IndexingPage;
import org.kitodo.selenium.testframework.pages.LdapGroupEditPage;
import org.kitodo.selenium.testframework.pages.LoginPage;
import org.kitodo.selenium.testframework.pages.ProcessesPage;
import org.kitodo.selenium.testframework.pages.ProjectsPage;
import org.kitodo.selenium.testframework.pages.RulesetEditPage;
import org.kitodo.selenium.testframework.pages.StartPage;
import org.kitodo.selenium.testframework.pages.TasksPage;
import org.kitodo.selenium.testframework.pages.TopNavigationPage;
import org.kitodo.selenium.testframework.pages.UserConfigurationPage;
import org.kitodo.selenium.testframework.pages.UserEditPage;
import org.kitodo.selenium.testframework.pages.UserGroupEditPage;
import org.kitodo.selenium.testframework.pages.UsersPage;;
import org.openqa.selenium.support.PageFactory;

public class Pages {

    private static <T> T getPage(Class<T> clazz) throws IllegalAccessException, InstantiationException {
        T page = clazz.newInstance();
        PageFactory.initElements(Browser.getDriver(), page);
        return page;
    }

    public static ClientsPage getClientsPage() throws InstantiationException, IllegalAccessException {
        return getPage(ClientsPage.class);
    }

    public static ClientEditPage getClientEditPage() throws InstantiationException, IllegalAccessException {
        return getPage(ClientEditPage.class);
    }

    public static HelpPage getHelpPage() throws InstantiationException, IllegalAccessException {
        return getPage(HelpPage.class);
    }

    public static IndexingPage getIndexingPage() throws InstantiationException, IllegalAccessException {
        return getPage(IndexingPage.class);
    }

    public static LoginPage getLoginPage() throws InstantiationException, IllegalAccessException {
        return getPage(LoginPage.class);
    }

    public static ProcessesPage getProcessesPage() throws InstantiationException, IllegalAccessException {
        return getPage(ProcessesPage.class);
    }

    public static ProjectsPage getProjectsPage() throws InstantiationException, IllegalAccessException {
        return getPage(ProjectsPage.class);
    }

    public static DocketEditPage getDocketEditPage() throws InstantiationException, IllegalAccessException {
        return getPage(DocketEditPage.class);
    }

    public static RulesetEditPage getRulesetEditPage() throws InstantiationException, IllegalAccessException {
        return getPage(RulesetEditPage.class);
    }

    public static StartPage getStartPage() throws InstantiationException, IllegalAccessException {
        return getPage(StartPage.class);
    }

    public static TasksPage getTasksPage() throws InstantiationException, IllegalAccessException {
        return getPage(TasksPage.class);
    }

    public static TopNavigationPage getTopNavigation() throws InstantiationException, IllegalAccessException {
        return getPage(TopNavigationPage.class);
    }

    public static UserConfigurationPage getUserConfigurationPage() throws InstantiationException, IllegalAccessException {
        return getPage(UserConfigurationPage.class);
    }

    public static UserEditPage getUserEditPage() throws InstantiationException, IllegalAccessException {
        return getPage(UserEditPage.class);
    }

    public static UserGroupEditPage getUserGroupEditPage() throws InstantiationException, IllegalAccessException {
        return getPage(UserGroupEditPage.class);
    }

    public static LdapGroupEditPage getLdapGroupEditPage() throws InstantiationException, IllegalAccessException {
        return getPage(LdapGroupEditPage.class);
    }

    public static UsersPage getUsersPage() throws InstantiationException, IllegalAccessException {
        return getPage(UsersPage.class);
    }

}
