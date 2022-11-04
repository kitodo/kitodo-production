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
import org.kitodo.selenium.testframework.pages.CurrentTasksEditPage;
import org.kitodo.selenium.testframework.pages.DesktopPage;
import org.kitodo.selenium.testframework.pages.DocketEditPage;
import org.kitodo.selenium.testframework.pages.ExtendedSearchPage;
import org.kitodo.selenium.testframework.pages.HelpPage;
import org.kitodo.selenium.testframework.pages.ImportConfigurationEditPage;
import org.kitodo.selenium.testframework.pages.LdapGroupEditPage;
import org.kitodo.selenium.testframework.pages.LoginPage;
import org.kitodo.selenium.testframework.pages.MetadataEditorPage;
import org.kitodo.selenium.testframework.pages.PostLoginChecksPage;
import org.kitodo.selenium.testframework.pages.ProcessEditPage;
import org.kitodo.selenium.testframework.pages.ProcessFromTemplatePage;
import org.kitodo.selenium.testframework.pages.ProcessesPage;
import org.kitodo.selenium.testframework.pages.ProjectEditPage;
import org.kitodo.selenium.testframework.pages.ProjectsPage;
import org.kitodo.selenium.testframework.pages.RoleEditPage;
import org.kitodo.selenium.testframework.pages.RulesetEditPage;
import org.kitodo.selenium.testframework.pages.SearchResultPage;
import org.kitodo.selenium.testframework.pages.StartPage;
import org.kitodo.selenium.testframework.pages.SystemPage;
import org.kitodo.selenium.testframework.pages.TasksPage;
import org.kitodo.selenium.testframework.pages.TemplateEditPage;
import org.kitodo.selenium.testframework.pages.TopNavigationPage;
import org.kitodo.selenium.testframework.pages.UserEditPage;
import org.kitodo.selenium.testframework.pages.UsersPage;
import org.kitodo.selenium.testframework.pages.WorkflowEditPage;
import org.openqa.selenium.support.PageFactory;

public class Pages {

    private static <T> T getPage(Class<T> clazz) throws IllegalAccessException, InstantiationException {
        T page = clazz.newInstance();
        PageFactory.initElements(Browser.getDriver(), page);
        return page;
    }

    public static ClientEditPage getClientEditPage() throws InstantiationException, IllegalAccessException {
        return getPage(ClientEditPage.class);
    }

    public static CurrentTasksEditPage getCurrentTasksEditPage() throws InstantiationException, IllegalAccessException {
        return getPage(CurrentTasksEditPage.class);
    }

    public static DesktopPage getDesktopPage() throws InstantiationException, IllegalAccessException {
        return getPage(DesktopPage.class);
    }

    public static SearchResultPage getSearchResultPage() throws InstantiationException, IllegalAccessException {
        return getPage(SearchResultPage.class);
    }

    public static ExtendedSearchPage getExtendedSearchPage() throws InstantiationException, IllegalAccessException {
        return getPage(ExtendedSearchPage.class);
    }

    public static HelpPage getHelpPage() throws InstantiationException, IllegalAccessException {
        return getPage(HelpPage.class);
    }

    public static SystemPage getSystemPage() throws InstantiationException, IllegalAccessException {
        return getPage(SystemPage.class);
    }

    public static LoginPage getLoginPage() throws InstantiationException, IllegalAccessException {
        return getPage(LoginPage.class);
    }

    public static PostLoginChecksPage getPostLoginChecksPage() throws InstantiationException, IllegalAccessException {
        return getPage(PostLoginChecksPage.class);
    }

    public static MetadataEditorPage getMetadataEditorPage() throws InstantiationException, IllegalAccessException {
        return getPage(MetadataEditorPage.class);
    }

    public static ProcessesPage getProcessesPage() throws InstantiationException, IllegalAccessException {
        return getPage(ProcessesPage.class);
    }

    public static ProcessEditPage getProcessEditPage() throws InstantiationException, IllegalAccessException {
        return getPage(ProcessEditPage.class);
    }

    public static ProcessFromTemplatePage getProcessFromTemplatePage() throws InstantiationException, IllegalAccessException {
        return getPage(ProcessFromTemplatePage.class);
    }

    public static ProjectsPage getProjectsPage() throws InstantiationException, IllegalAccessException {
        return getPage(ProjectsPage.class);
    }

    public static ProjectEditPage getProjectEditPage() throws InstantiationException, IllegalAccessException {
        return getPage(ProjectEditPage.class);
    }

    public static TemplateEditPage getTemplateEditPage() throws InstantiationException, IllegalAccessException {
        return getPage(TemplateEditPage.class);
    }

    public static WorkflowEditPage getWorkflowEditPage() throws InstantiationException, IllegalAccessException {
        return getPage(WorkflowEditPage.class);
    }

    public static DocketEditPage getDocketEditPage() throws InstantiationException, IllegalAccessException {
        return getPage(DocketEditPage.class);
    }

    public static RulesetEditPage getRulesetEditPage() throws InstantiationException, IllegalAccessException {
        return getPage(RulesetEditPage.class);
    }

    public static ImportConfigurationEditPage getImportConfigurationEditPage() throws IllegalAccessException,
            InstantiationException {
        return getPage(ImportConfigurationEditPage.class);
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

    public static UserEditPage getUserEditPage() throws InstantiationException, IllegalAccessException {
        return getPage(UserEditPage.class);
    }

    public static RoleEditPage getRoleEditPage() throws InstantiationException, IllegalAccessException {
        return getPage(RoleEditPage.class);
    }

    public static LdapGroupEditPage getLdapGroupEditPage() throws InstantiationException, IllegalAccessException {
        return getPage(LdapGroupEditPage.class);
    }

    public static UsersPage getUsersPage() throws InstantiationException, IllegalAccessException {
        return getPage(UsersPage.class);
    }

}
