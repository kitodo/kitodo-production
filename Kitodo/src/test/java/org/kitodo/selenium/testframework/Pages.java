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

import org.kitodo.selenium.testframework.helper.RepeatingFieldDecorator;
import org.kitodo.selenium.testframework.pages.CalendarPage;
import org.kitodo.selenium.testframework.pages.ClientEditPage;
import org.kitodo.selenium.testframework.pages.CurrentTasksEditPage;
import org.kitodo.selenium.testframework.pages.DesktopPage;
import org.kitodo.selenium.testframework.pages.DocketEditPage;
import org.kitodo.selenium.testframework.pages.ExtendedSearchPage;
import org.kitodo.selenium.testframework.pages.HelpPage;
import org.kitodo.selenium.testframework.pages.ImportConfigurationEditPage;
import org.kitodo.selenium.testframework.pages.LdapGroupEditPage;
import org.kitodo.selenium.testframework.pages.LoginPage;
import org.kitodo.selenium.testframework.pages.LtpValidationConfigurationEditPage;
import org.kitodo.selenium.testframework.pages.MassImportPage;
import org.kitodo.selenium.testframework.pages.MetadataEditorPage;
import org.kitodo.selenium.testframework.pages.PostLoginChecksPage;
import org.kitodo.selenium.testframework.pages.ProcessEditPage;
import org.kitodo.selenium.testframework.pages.ProcessFromTemplatePage;
import org.kitodo.selenium.testframework.pages.ProcessesPage;
import org.kitodo.selenium.testframework.pages.ProjectEditPage;
import org.kitodo.selenium.testframework.pages.ProjectsPage;
import org.kitodo.selenium.testframework.pages.RoleEditPage;
import org.kitodo.selenium.testframework.pages.RulesetEditPage;
import org.kitodo.selenium.testframework.pages.StartPage;
import org.kitodo.selenium.testframework.pages.SystemPage;
import org.kitodo.selenium.testframework.pages.TasksPage;
import org.kitodo.selenium.testframework.pages.TemplateEditPage;
import org.kitodo.selenium.testframework.pages.TopNavigationPage;
import org.kitodo.selenium.testframework.pages.UserEditPage;
import org.kitodo.selenium.testframework.pages.UsersPage;
import org.kitodo.selenium.testframework.pages.WorkflowEditPage;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.pagefactory.DefaultElementLocatorFactory;

public class Pages {

    private static <T> T getPage(Class<T> clazz) throws ReflectiveOperationException {
        T page = clazz.getDeclaredConstructor().newInstance();
        PageFactory.initElements(new RepeatingFieldDecorator(new DefaultElementLocatorFactory(Browser.getDriver())), page);
        return page;
    }

    public static ClientEditPage getClientEditPage() throws ReflectiveOperationException {
        return getPage(ClientEditPage.class);
    }

    public static CurrentTasksEditPage getCurrentTasksEditPage() throws ReflectiveOperationException {
        return getPage(CurrentTasksEditPage.class);
    }

    public static DesktopPage getDesktopPage() throws ReflectiveOperationException {
        return getPage(DesktopPage.class);
    }

    public static ExtendedSearchPage getExtendedSearchPage() throws ReflectiveOperationException {
        return getPage(ExtendedSearchPage.class);
    }

    public static HelpPage getHelpPage() throws ReflectiveOperationException {
        return getPage(HelpPage.class);
    }

    public static SystemPage getSystemPage() throws ReflectiveOperationException {
        return getPage(SystemPage.class);
    }

    public static LoginPage getLoginPage() throws ReflectiveOperationException {
        return getPage(LoginPage.class);
    }

    public static PostLoginChecksPage getPostLoginChecksPage() throws ReflectiveOperationException {
        return getPage(PostLoginChecksPage.class);
    }

    public static MetadataEditorPage getMetadataEditorPage() throws ReflectiveOperationException {
        return getPage(MetadataEditorPage.class);
    }

    public static ProcessesPage getProcessesPage() throws ReflectiveOperationException {
        return getPage(ProcessesPage.class);
    }

    public static ProcessEditPage getProcessEditPage() throws ReflectiveOperationException {
        return getPage(ProcessEditPage.class);
    }

    public static ProcessFromTemplatePage getProcessFromTemplatePage() throws ReflectiveOperationException {
        return getPage(ProcessFromTemplatePage.class);
    }

    public static ProjectsPage getProjectsPage() throws ReflectiveOperationException {
        return getPage(ProjectsPage.class);
    }

    public static ProjectEditPage getProjectEditPage() throws ReflectiveOperationException {
        return getPage(ProjectEditPage.class);
    }

    public static TemplateEditPage getTemplateEditPage() throws ReflectiveOperationException {
        return getPage(TemplateEditPage.class);
    }

    public static WorkflowEditPage getWorkflowEditPage() throws ReflectiveOperationException {
        return getPage(WorkflowEditPage.class);
    }

    public static DocketEditPage getDocketEditPage() throws ReflectiveOperationException {
        return getPage(DocketEditPage.class);
    }

    public static RulesetEditPage getRulesetEditPage() throws ReflectiveOperationException {
        return getPage(RulesetEditPage.class);
    }

    public static ImportConfigurationEditPage getImportConfigurationEditPage() throws ReflectiveOperationException {
        return getPage(ImportConfigurationEditPage.class);
    }

    public static StartPage getStartPage() throws ReflectiveOperationException {
        return getPage(StartPage.class);
    }

    public static TasksPage getTasksPage() throws ReflectiveOperationException {
        return getPage(TasksPage.class);
    }

    public static TopNavigationPage getTopNavigation() throws ReflectiveOperationException {
        return getPage(TopNavigationPage.class);
    }

    public static UserEditPage getUserEditPage() throws ReflectiveOperationException {
        return getPage(UserEditPage.class);
    }

    public static RoleEditPage getRoleEditPage() throws ReflectiveOperationException {
        return getPage(RoleEditPage.class);
    }

    public static LdapGroupEditPage getLdapGroupEditPage() throws ReflectiveOperationException {
        return getPage(LdapGroupEditPage.class);
    }

    public static UsersPage getUsersPage() throws ReflectiveOperationException {
        return getPage(UsersPage.class);
    }

    public static CalendarPage getCalendarPage() throws ReflectiveOperationException {
        return getPage(CalendarPage.class);
    }

    public static MassImportPage getMassImportPage() throws ReflectiveOperationException {
        return getPage(MassImportPage.class);
    }

    public static LtpValidationConfigurationEditPage getLtpValidationConfigurationEditPage()
            throws ReflectiveOperationException {
        return getPage(LtpValidationConfigurationEditPage.class);
    }
}
