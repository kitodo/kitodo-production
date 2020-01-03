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

package org.kitodo.selenium.testframework.pages;

import static org.awaitility.Awaitility.await;
import static org.kitodo.selenium.testframework.Browser.getRowsOfTable;
import static org.kitodo.selenium.testframework.Browser.getTableDataByColumn;

import java.util.List;
import java.util.concurrent.TimeUnit;

import org.kitodo.MockDatabase;
import org.kitodo.production.enums.ObjectType;
import org.kitodo.selenium.testframework.Browser;
import org.kitodo.selenium.testframework.Pages;
import org.kitodo.selenium.testframework.enums.TabIndex;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

public class UsersPage extends Page<UsersPage> {

    private static final String USERS_TAB_VIEW = "usersTabView";
    private static final String USERS_TABLE = USERS_TAB_VIEW + ":usersTable";
    private static final String ROLES_TABLE = USERS_TAB_VIEW + ":rolesTable";
    private static final String CLIENTS_TABLE = USERS_TAB_VIEW + ":clientsTable";
    private static final String LDAP_GROUPS_TABLE = USERS_TAB_VIEW + ":ldapGroupsTable";

    @SuppressWarnings("unused")
    @FindBy(id = USERS_TAB_VIEW)
    private WebElement usersTabView;

    @SuppressWarnings("unused")
    @FindBy(id = USERS_TABLE + DATA)
    private WebElement usersTable;

    @SuppressWarnings("unused")
    @FindBy(id = ROLES_TABLE + DATA)
    private WebElement rolesTable;

    @SuppressWarnings("unused")
    @FindBy(id = CLIENTS_TABLE + DATA)
    private WebElement clientsTable;

    @SuppressWarnings("unused")
    @FindBy(id = LDAP_GROUPS_TABLE + DATA)
    private WebElement ldapGroupsTable;

    @SuppressWarnings("unused")
    @FindBy(id = "newElementForm:newElementButton_button")
    private WebElement newElementButton;

    @SuppressWarnings("unused")
    @FindBy(id = "newElementForm:newUserButton")
    private WebElement newUserButton;

    @SuppressWarnings("unused")
    @FindBy(id = "newElementForm:newRoleButton")
    private WebElement newRoleButton;

    @SuppressWarnings("unused")
    @FindBy(id = "newElementForm:newClientButton")
    private WebElement newClientButton;

    @SuppressWarnings("unused")
    @FindBy(id = "newElementForm:newLdapGroupButton")
    private WebElement newLdapGroupButton;

    @SuppressWarnings("unused")
    @FindBy(id = "usersTabView:usersTable:0:actionForm:editUser")
    private WebElement editUserLink;

    @SuppressWarnings("unused")
    @FindBy(xpath = "//a[@href='/kitodo/pages/roleEdit.jsf?id=1']")
    private WebElement editRoleLink;

    @SuppressWarnings("unused")
    @FindBy(xpath = "//a[@href='/kitodo/pages/clientEdit.jsf?id=1']")
    private WebElement editClientLink;

    @SuppressWarnings("unused")
    @FindBy(xpath = "//a[@href='/kitodo/pages/ldapgroupEdit.jsf?id=1']")
    private WebElement editLdapGroupLink;

    @SuppressWarnings("unused")
    @FindBy(id = USERS_TABLE + ":0:actionForm:deleteUser")
    private WebElement deleteFirstUserButton;

    @SuppressWarnings("unused")
    @FindBy(id = ROLES_TABLE + ":0:actionForm:deleteRole")
    private WebElement deleteFirstRoleButton;

    @SuppressWarnings("unused")
    @FindBy(id = CLIENTS_TABLE + ":0:actionForm:deleteClient")
    private WebElement deleteFirstClientButton;

    @SuppressWarnings("unused")
    @FindBy(id = LDAP_GROUPS_TABLE + ":0:actionForm:deleteLdapgroup")
    private WebElement deleteFirstLDAPGroupButton;

    public UsersPage() {
        super("pages/users.jsf");
    }

    /**
     * Goes to users page.
     * 
     * @return The users page.
     */
    public UsersPage goTo() throws Exception {
        Pages.getTopNavigation().gotoUsers();
        await("Wait for execution of link click").pollDelay(Browser.getDelayMinAfterLinkClick(), TimeUnit.MILLISECONDS)
                .atMost(Browser.getDelayMaxAfterLinkClick(), TimeUnit.MILLISECONDS).ignoreExceptions()
                .until(this::isAt);
        return this;
    }

    /**
     * Counts rows of users table.
     * 
     * @return The number of rows of users table.
     */
    public int countListedUsers() throws Exception {
        if (isNotAt()) {
            goTo();
        }
        return getRowsOfTable(usersTable).size();
    }

    /**
     * Counts rows of roles table.
     *
     * @return The number of rows of roles table.
     */
    public int countListedRoles() throws Exception {
        switchToTabByIndex(TabIndex.ROLES.getIndex());
        return getRowsOfTable(rolesTable).size();
    }

    /**
     * Count rows of clients table.
     *
     * @return The number of rows of the clients table.
     */
    public int countListedClients() throws Exception {
        switchToTabByIndex(TabIndex.CLIENTS.getIndex());
        return getRowsOfTable(clientsTable).size();
    }


    /**
     * Counts rows of ldap groups table.
     *
     * @return The number of rows of ldap groups table.
     */
    public int countListedLdapGroups() throws Exception {
        switchToTabByIndex(TabIndex.LDAP_GROUPS.getIndex());
        return getRowsOfTable(ldapGroupsTable).size();
    }

    /**
     * Goes to edit page for creating a new user.
     * 
     * @return The user edit page.
     */
    public UserEditPage createNewUser() throws Exception {
        if (isNotAt()) {
            goTo();
        }
        newElementButton.click();
        clickButtonAndWaitForRedirect(newUserButton, Pages.getUserEditPage().getUrl());
        return Pages.getUserEditPage();
    }

    /**
     * Goes to edit page for creating a new ldap group.
     *
     * @return The user edit page.
     */
    public LdapGroupEditPage createNewLdapGroup() throws Exception {
        if (isNotAt()) {
            goTo();
        }
        newElementButton.click();
        clickButtonAndWaitForRedirect(newLdapGroupButton, Pages.getLdapGroupEditPage().getUrl());
        return Pages.getLdapGroupEditPage();
    }

    /**
     * Returns a list of all ldap groups titles which were displayed on ldap group
     * page.
     *
     * @return The list of ldap group titles
     */
    public List<String> getLdapGroupNames() throws Exception {
        switchToTabByIndex(TabIndex.LDAP_GROUPS.getIndex());
        return getTableDataByColumn(ldapGroupsTable, 0);
    }

    /**
     * Clicks on the tab indicated by given index (starting with 0 for the first
     * tab).
     * 
     * @return The users page.
     */
    public UsersPage switchToTabByIndex(int index) throws Exception {
        return switchToTabByIndex(index, usersTabView);
    }

    /**
     * Goes to edit page for creating a new user group.
     *
     * @return The user group edit page.
     */
    public RoleEditPage createNewRole() throws Exception {
        if (isNotAt()) {
            goTo();
        }
        newElementButton.click();
        clickButtonAndWaitForRedirect(newRoleButton, Pages.getRoleEditPage().getUrl());
        return Pages.getRoleEditPage();
    }

    /**
     * Go to edit page for editing a user.
     *
     * @return the user edit page
     */
    public UserEditPage editUser() throws Exception {
        switchToTabByIndex(TabIndex.USERS.getIndex());

        clickButtonAndWaitForRedirect(editUserLink, Pages.getUserEditPage().getUrl());
        return Pages.getUserEditPage();
    }

    /**
     * Go to edit page for editing a user group.
     *
     * @return the user group edit page
     */
    public RoleEditPage editRole() throws Exception {
        switchToTabByIndex(TabIndex.ROLES.getIndex());

        clickButtonAndWaitForRedirect(editRoleLink, Pages.getRoleEditPage().getUrl());
        return Pages.getRoleEditPage();
    }

    /**
     * Goes to edit page for editing a given role, specified by title.
     *
     * @param roleTitle
     *            the role title
     * @return the role edit page
     */
    public RoleEditPage editRole(String roleTitle) throws Exception {
        switchToTabByIndex(TabIndex.ROLES.getIndex());

        List<WebElement> tableRows = getRowsOfTable(rolesTable);

        for (WebElement tableRow : tableRows) {
            if (Browser.getCellDataByRow(tableRow, 0).equals(roleTitle)) {
                clickEditLinkOfTableRow(tableRow);
                return Pages.getRoleEditPage();
            }
        }
        throw new NoSuchElementException("No user group with given title was not found: " + roleTitle);
    }

    /**
     * Go to edit page for editing a LDAP group.
     *
     * @return the LDAP group edit page
     */
    public LdapGroupEditPage editLdapGroup() throws Exception {
        switchToTabByIndex(TabIndex.LDAP_GROUPS.getIndex());

        clickButtonAndWaitForRedirect(editLdapGroupLink, Pages.getLdapGroupEditPage().getUrl());
        return Pages.getLdapGroupEditPage();
    }

    /**
     * Go to edit page for editing a client.
     *
     * @return the client edit page
     */
    public ClientEditPage editClient() throws Exception {
        switchToTabByIndex(TabIndex.CLIENTS.getIndex());

        clickButtonAndWaitForRedirect(editClientLink, Pages.getClientEditPage().getUrl());
        return Pages.getClientEditPage();
    }

    /**
     * Goes to edit page for editing a given ldap group, specified by title.
     *
     * @param ldapGroupTitle
     *            The ldap group title.
     * @return The ldap group edit page.
     */
    public LdapGroupEditPage editLdapGroup(String ldapGroupTitle) throws Exception {
        switchToTabByIndex(TabIndex.LDAP_GROUPS.getIndex());

        List<WebElement> tableRows = getRowsOfTable(ldapGroupsTable);

        for (WebElement tableRow : tableRows) {
            if (Browser.getCellDataByRow(tableRow, 0).equals(ldapGroupTitle)) {
                clickEditLinkOfTableRow(tableRow);
                return Pages.getLdapGroupEditPage();
            }
        }
        throw new NoSuchElementException("No ldap group with given title was found: " + ldapGroupTitle);
    }

    private void clickEditLinkOfTableRow(WebElement tableRow) throws Exception {
        WebElement ldapGroupEditLink = tableRow.findElement(By.tagName("a"));
        ldapGroupEditLink.click();
        Thread.sleep(Browser.getDelayMinAfterLinkClick());
    }

    /**
     * Returns a list of all role titles which were displayed on roles
     * page.
     * 
     * @return the list of role titles
     */
    public List<String> getRoleTitles() throws Exception {
        switchToTabByIndex(TabIndex.ROLES.getIndex());
        return getTableDataByColumn(rolesTable, 0);
    }

    /**
     * Returns a list of all client titles which were displayed on clients tab.
     *
     * @return The list of client titles
     */
    public List<String> getClientNames() throws Exception {
        switchToTabByIndex(TabIndex.CLIENTS.getIndex());
        return getTableDataByColumn(clientsTable, 0);
    }

    /**
     * Goes to edit page for creating a new client.
     *
     * @return The client edit page.
     */
    public ClientEditPage createNewClient() throws Exception {
        if (isNotAt()) {
            goTo();
        }
        newElementButton.click();
        clickButtonAndWaitForRedirect(newClientButton, Pages.getClientEditPage().getUrl());
        return Pages.getClientEditPage();
    }

    /**
     * Remove user from corresponding list on user page.
     */
    public void deleteRemovableUser() throws Exception {
        deleteElement("User",
                MockDatabase.getRemovableObjectIDs().get(ObjectType.USER.name()),
                TabIndex.USERS.getIndex(),
                usersTabView);
    }

    /**
     * Remove role from corresponding list on user page.
     */
    public void deleteRemovableRole() throws Exception {
        deleteElement("Role",
                MockDatabase.getRemovableObjectIDs().get(ObjectType.ROLE.name()),
                TabIndex.ROLES.getIndex(),
                usersTabView);
    }

    /**
     * Remove client from corresponding list on user page.
     */
    public void deleteRemovableClient() throws Exception {
        deleteElement("Client",
                MockDatabase.getRemovableObjectIDs().get(ObjectType.CLIENT.name()),
                TabIndex.CLIENTS.getIndex(),
                usersTabView);
    }
}
