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

package org.kitodo.production.services.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Collection;
import java.util.Objects;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.kitodo.MockDatabase;
import org.kitodo.SecurityTestUtils;
import org.kitodo.data.database.beans.User;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.production.services.ServiceManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

public class SecurityAccessServiceIT {

    @BeforeAll
    public static void setUp() throws Exception {
        MockDatabase.startNode();
        MockDatabase.insertForAuthenticationTesting();
        MockDatabase.insertDockets();
        MockDatabase.insertRulesets();
        MockDatabase.insertTemplates();
        MockDatabase.insertWorkflows();
        MockDatabase.insertMappingFiles();
        MockDatabase.insertImportConfigurations();
        MockDatabase.insertClientSpecificAdministrator();
    }

    @AfterAll
    public static void tearDown() throws Exception {
        MockDatabase.stopNode();
        MockDatabase.cleanDatabase();
    }

    @AfterEach
    public void cleanContext() {
        SecurityTestUtils.cleanSecurityContext();
    }

    @Test
    public void shouldGetAuthorities() throws DAOException {
        User user = ServiceManager.getUserService().getByLogin("kowal");
        SecurityTestUtils.addUserDataToSecurityContext(user, 1);
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (Objects.nonNull(authentication)) {
            Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
            assertEquals(189, authorities.size(),
                    "Security context holder does not hold the corresponding authorities");
        }
    }

    @Test
    public void hasAuthorityTest() throws DAOException {
        User user = ServiceManager.getUserService().getByLogin("kowal");
        SecurityTestUtils.addUserDataToSecurityContext(user, 1);
        assertTrue(ServiceManager.getSecurityAccessService().hasAuthorityGlobal("editClient"),
                "The authority \"editClient\" was not found for authenticated user");
    }

    @Test
    public void hasAuthorityForClientTest() throws DAOException {
        User user = ServiceManager.getUserService().getByLogin("kowal");
        SecurityTestUtils.addUserDataToSecurityContext(user,1);
        assertTrue(ServiceManager.getSecurityAccessService().hasAuthorityForClient("editProject"),
                "Checking if user has edit project authority for first client returned wrong value");
    }

    @Test
    public void hasAuthorityToViewSpecificTemplateTest() throws DAOException {
        User user = ServiceManager.getUserService().getByLogin("nowak");

        // Session client 2, user "nowak" has the authority "viewTemplate"
        SecurityTestUtils.addUserDataToSecurityContext(user, 2);
        assertTrue(ServiceManager.getSecurityAccessService().hasAuthorityToViewTemplate(2),
                "User 'nowak' should have the permission to view templates of current session client with ID 2");
        assertFalse(ServiceManager.getSecurityAccessService().hasAuthorityToViewTemplate(1),
                "User 'nowak' should NOT have permission to view templates of client 1 because the current session client has the ID 2");
        assertTrue(ServiceManager.getSecurityAccessService().hasAuthorityToViewTemplate(0),
                "User 'nowak' should have the permission to open the 'view template' page with ID 0");
        assertThrows(DAOException.class, () -> ServiceManager.getSecurityAccessService().hasAuthorityToViewTemplate(999),
                "Viewing a non-existing template should trigger a DAOException when the user has the permission to view templates");

        // Session client 1, user "nowak" does _not_ have the authority "viewTemplate"
        SecurityTestUtils.addUserDataToSecurityContext(user, 1);
        assertFalse(ServiceManager.getSecurityAccessService().hasAuthorityToViewTemplate(1),
                "User 'nowak' should NOT have the permission to view templates while logged in with session client 1");
    }

    @Test
    public void hasAuthorityToViewSpecificWorkflowTest() throws DAOException {
        User user = ServiceManager.getUserService().getByLogin("nowak");

        // Session client 2, user "nowak" has the authority "viewWorkflow"
        SecurityTestUtils.addUserDataToSecurityContext(user, 2);
        assertTrue(ServiceManager.getSecurityAccessService().hasAuthorityToViewWorkflow(3),
                "User 'nowak' should have the permission to view workflow 3 of current session client 2");
        assertFalse(ServiceManager.getSecurityAccessService().hasAuthorityToViewWorkflow(1),
                "User 'nowak' should NOT have permission to view workflow 1 of client 1 while session client is 2");
        assertTrue(ServiceManager.getSecurityAccessService().hasAuthorityToViewWorkflow(0),
                "User 'nowak' should have the permission to open the 'view workflow' page with ID 0");
        assertThrows(DAOException.class, () -> ServiceManager.getSecurityAccessService().hasAuthorityToViewWorkflow(999),
                "Viewing a non-existing workflow should trigger a DAOException when the user may view workflows");

        // Session client 1, user "nowak" does _not_ have the authority "viewWorkflow"
        SecurityTestUtils.addUserDataToSecurityContext(user, 1);
        assertFalse(ServiceManager.getSecurityAccessService().hasAuthorityToViewWorkflow(1),
                "User 'nowak' should NOT have the permission to view workflows while logged in with session client 1");
    }

    @Test
    public void hasAuthorityToViewSpecificDocketTest() throws DAOException {
        User user = ServiceManager.getUserService().getByLogin("nowak");

        // Session client 2, user "nowak" has the authority "viewDocket"
        SecurityTestUtils.addUserDataToSecurityContext(user, 2);
        assertTrue(ServiceManager.getSecurityAccessService().hasAuthorityToViewDocket(3),
                "User 'nowak' should have the permission to view docket 3 of current session client 2");
        assertFalse(ServiceManager.getSecurityAccessService().hasAuthorityToViewDocket(1),
                "User 'nowak' should NOT have permission to view docket 1 of client 1 while session client is 2");
        assertTrue(ServiceManager.getSecurityAccessService().hasAuthorityToViewDocket(0),
                "User 'nowak' should have the permission to open the 'view docket' page with ID 0");
        assertThrows(DAOException.class, () -> ServiceManager.getSecurityAccessService().hasAuthorityToViewDocket(999),
                "Viewing a non-existing docket should trigger a DAOException when the user may view dockets");

        // Session client 1, user "nowak" does _not_ have the authority "viewDocket"
        SecurityTestUtils.addUserDataToSecurityContext(user, 1);
        assertFalse(ServiceManager.getSecurityAccessService().hasAuthorityToViewDocket(1),
                "User 'nowak' should NOT have the permission to view dockets while logged in with session client 1");
    }

    @Test
    public void hasAuthorityToViewSpecificRulesetTest() throws DAOException {
        // Session client 1, user "kowal" has the authority "viewRuleset"
        User kowal = ServiceManager.getUserService().getByLogin("kowal");
        SecurityTestUtils.addUserDataToSecurityContext(kowal, 1);
        assertTrue(ServiceManager.getSecurityAccessService().hasAuthorityToViewRuleset(1),
                "User 'kowal' should have the permission to view ruleset 1 of current session client 1");
        assertFalse(ServiceManager.getSecurityAccessService().hasAuthorityToViewRuleset(3),
                "User 'kowal' should NOT have permission to view ruleset 3 of client 2 while session client is 1");
        assertTrue(ServiceManager.getSecurityAccessService().hasAuthorityToViewRuleset(0),
                "User 'kowal' should have the permission to open the 'view ruleset' page with ID 0");
        assertThrows(DAOException.class, () -> ServiceManager.getSecurityAccessService().hasAuthorityToViewRuleset(999),
                "Viewing a non-existing ruleset should trigger a DAOException when the user may view rulesets");

        // Session client 2, user "nowak" does _not_ have the authority "viewRuleset"
        User nowak = ServiceManager.getUserService().getByLogin("nowak");
        SecurityTestUtils.addUserDataToSecurityContext(nowak, 2);
        assertFalse(ServiceManager.getSecurityAccessService().hasAuthorityToViewRuleset(3),
                "User 'nowak' should NOT have the permission to view rulesets while logged in with session client 2");
    }

    @Test
    public void hasAuthorityToViewSpecificImportConfigurationTest() throws DAOException {
        // Session client 1, user "kowal" has the authority "viewImportConfiguration"
        User kowal = ServiceManager.getUserService().getByLogin("kowal");
        SecurityTestUtils.addUserDataToSecurityContext(kowal, 1);
        assertTrue(ServiceManager.getSecurityAccessService().hasAuthorityToViewImportConfiguration(2),
                "User 'kowal' should have the permission to view import configuration 2 assigned to client 1 only");
        assertTrue(ServiceManager.getSecurityAccessService().hasAuthorityToViewImportConfiguration(1),
                "User 'kowal' should have the permission to view import configuration 1 assigned to clients 1 and 2");
        assertTrue(ServiceManager.getSecurityAccessService().hasAuthorityToViewImportConfiguration(0),
                "User 'kowal' should have the permission to open the 'view import configuration' page with ID 0");
        assertThrows(DAOException.class,
                () -> ServiceManager.getSecurityAccessService().hasAuthorityToViewImportConfiguration(999),
                "Viewing a non-existing import configuration should trigger a DAOException when the user may view them");

        // Session client 2, user "clientadmin" has the authority "viewImportConfiguration" for client 2 only
        User clientAdmin = ServiceManager.getUserService().getByLogin("clientadmin");
        SecurityTestUtils.addUserDataToSecurityContext(clientAdmin, 2);
        assertTrue(ServiceManager.getSecurityAccessService().hasAuthorityToViewImportConfiguration(1),
                "User 'clientadmin' should have the permission to view import configuration 1 assigned to client 2");
        assertFalse(ServiceManager.getSecurityAccessService().hasAuthorityToViewImportConfiguration(2),
                "User 'clientadmin' should NOT have permission to view import configuration 2 not assigned to client 2");

        // Session client 2, user "nowak" does _not_ have the authority "viewImportConfiguration"
        User nowak = ServiceManager.getUserService().getByLogin("nowak");
        SecurityTestUtils.addUserDataToSecurityContext(nowak, 2);
        assertFalse(ServiceManager.getSecurityAccessService().hasAuthorityToViewImportConfiguration(1),
                "User 'nowak' should NOT have the permission to view import configurations in session client 2");
    }

    @Test
    public void hasAuthorityToViewSpecificRoleTest() throws DAOException {
        // Session client 1, user "kowal" has the global authority "viewRole", so the client of the role is irrelevant
        User kowal = ServiceManager.getUserService().getByLogin("kowal");
        SecurityTestUtils.addUserDataToSecurityContext(kowal, 1);
        assertTrue(ServiceManager.getSecurityAccessService().hasAuthorityToViewRole(4),
                "User 'kowal' should have the permission to view role 4 of client 2 due to his global authority");
        assertTrue(ServiceManager.getSecurityAccessService().hasAuthorityToViewRole(1),
                "User 'kowal' should have the permission to view role 1 of client 1 due to his global authority");
        assertTrue(ServiceManager.getSecurityAccessService().hasAuthorityToViewRole(999),
                "With the global authority, the role is not loaded, so a non-existing ID should not cause an exception");

        // Session client 2, user "clientadmin" has the authority "viewRole" for client 2 only
        User clientAdmin = ServiceManager.getUserService().getByLogin("clientadmin");
        SecurityTestUtils.addUserDataToSecurityContext(clientAdmin, 2);
        assertTrue(ServiceManager.getSecurityAccessService().hasAuthorityToViewRole(4),
                "User 'clientadmin' should have the permission to view role 4 of current session client 2");
        assertFalse(ServiceManager.getSecurityAccessService().hasAuthorityToViewRole(1),
                "User 'clientadmin' should NOT have permission to view role 1 of client 1 while session client is 2");
        assertTrue(ServiceManager.getSecurityAccessService().hasAuthorityToViewRole(0),
                "User 'clientadmin' should have the permission to open the 'view role' page with ID 0");
        assertThrows(DAOException.class, () -> ServiceManager.getSecurityAccessService().hasAuthorityToViewRole(999),
                "Viewing a non-existing role should trigger a DAOException when the user may view roles of the client");

        // Session client 1, user "nowak" has the authority "viewAllRoles", but not "viewRole"
        User nowak = ServiceManager.getUserService().getByLogin("nowak");
        SecurityTestUtils.addUserDataToSecurityContext(nowak, 1);
        assertFalse(ServiceManager.getSecurityAccessService().hasAuthorityToViewRole(1),
                "User 'nowak' should NOT have the permission to view roles while logged in with session client 1");
    }

    @Test
    public void hasAuthorityToViewSpecificClientTest() throws DAOException {
        // Session client 1, user "kowal" has the global authority "viewClient", so the requested client is irrelevant
        User kowal = ServiceManager.getUserService().getByLogin("kowal");
        SecurityTestUtils.addUserDataToSecurityContext(kowal, 1);
        assertTrue(ServiceManager.getSecurityAccessService().hasAuthorityToViewClient(2),
                "User 'kowal' should have the permission to view client 2 due to his global authority");

        // Session client 2, user "clientadmin" has the authority "viewClient" for client 2 only
        User clientAdmin = ServiceManager.getUserService().getByLogin("clientadmin");
        SecurityTestUtils.addUserDataToSecurityContext(clientAdmin, 2);
        assertTrue(ServiceManager.getSecurityAccessService().hasAuthorityToViewClient(2),
                "User 'clientadmin' should have the permission to view the current session client 2");
        assertFalse(ServiceManager.getSecurityAccessService().hasAuthorityToViewClient(1),
                "User 'clientadmin' should NOT have permission to view client 1 while session client is 2");
        assertTrue(ServiceManager.getSecurityAccessService().hasAuthorityToViewClient(0),
                "User 'clientadmin' should have the permission to open the 'view client' page with ID 0");

        // Session client 1, user "nowak" does _not_ have the authority "viewClient"
        User nowak = ServiceManager.getUserService().getByLogin("nowak");
        SecurityTestUtils.addUserDataToSecurityContext(nowak, 1);
        assertFalse(ServiceManager.getSecurityAccessService().hasAuthorityToViewClient(1),
                "User 'nowak' should NOT have the permission to view clients while logged in with session client 1");
    }
}
