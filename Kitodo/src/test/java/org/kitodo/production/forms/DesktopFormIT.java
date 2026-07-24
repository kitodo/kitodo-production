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

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.kitodo.MockDatabase;
import org.kitodo.SecurityTestUtils;
import org.kitodo.data.database.beans.Project;
import org.kitodo.data.database.beans.User;
import org.kitodo.production.services.ServiceManager;

public class DesktopFormIT {

    @BeforeAll
    public static void prepareDatabase() throws Exception {
        MockDatabase.startNode();
        MockDatabase.insertProcessesFull();
        User user = ServiceManager.getUserService().getByLogin("kowal");
        SecurityTestUtils.addUserDataToSecurityContext(user, 1);
    }

    @AfterAll
    public static void cleanDatabase() throws Exception {
        SecurityTestUtils.cleanSecurityContext();
        MockDatabase.stopNode();
        MockDatabase.cleanDatabase();
    }

    @Test
    public void shouldHideInactiveProjects() throws Exception {
        DesktopForm view = new DesktopForm();

        Project visibleProject = view.getProjects().stream()
                .findFirst()
                .orElseThrow(() -> new AssertionError("No project visible in desktop project widget"));

        Integer projectId = visibleProject.getId();

        assertTrue(view.getProjects().stream()
                        .anyMatch(project -> project.getId().equals(projectId)),
                "Active project should be shown in the desktop project widget");

        try {
            Project project = ServiceManager.getProjectService().getById(projectId);
            project.setActive(false);
            ServiceManager.getProjectService().save(project);

            view.emptyProjectCache();

            assertTrue(view.getProjects().stream()
                            .noneMatch(projectFromView -> projectFromView.getId().equals(projectId)),
                    "Inactive project should not be shown in the desktop project widget");
        } finally {
            Project project = ServiceManager.getProjectService().getById(projectId);
            project.setActive(true);
            ServiceManager.getProjectService().save(project);
        }
    }
}

