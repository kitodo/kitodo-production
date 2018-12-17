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

package org.kitodo.selenium.testframework.generators;

import org.kitodo.data.database.beans.Project;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.production.services.ServiceManager;

public class ProjectGenerator {

    public static Project generateProject() throws DAOException  {
        Project project = new Project();
        project.setTitle("MockProject");
        project.setNumberOfPages(300);
        project.setNumberOfVolumes(10);
        project.setClient(ServiceManager.getClientService().getById(1));
        project.setActive(true);

        return project;
    }
}
