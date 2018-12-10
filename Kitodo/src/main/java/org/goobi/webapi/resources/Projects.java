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

package org.goobi.webapi.resources;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.goobi.webapi.beans.ProjectsRootNode;
import org.kitodo.data.database.beans.Project;
import org.kitodo.data.database.beans.Template;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.services.ServiceManager;

/**
 * The CatalogueConfiguration class provides the Jersey API URL pattern
 * ${SERVLET_CONTEXT}/rest/projects which returns the major data from the
 * project configuration in XML or JSON format.
 * 
 * @author Matthias Ronge &lt;matthias.ronge@zeutschel.de&gt;
 */
@Path("/projects")
public class Projects {
    /**
     * Get all projects with their respective templates.
     *
     * @return ProjectsRootNode object
     */
    @GET
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
    public ProjectsRootNode getAllProjectsWithTheirRespectiveTemplates() {
        Map<Project, Set<Template>> data = new HashMap<>();

        try {
            List<Template> processTemplates = ServiceManager.getTemplateService().getAll();
            for (Template processTemplate : processTemplates) {
                List<Project> projects = processTemplate.getProjects();
                for (Project project : projects) {
                    Set<Template> templates = data.containsKey(project) ? data.get(project) : new HashSet<>();
                    templates.add(processTemplate);
                    data.put(project, templates);
                }
            }
            List<Project> result = new ArrayList<>();
            for (Map.Entry<Project, Set<Template>> entry : data.entrySet()) {
                Project key = entry.getKey();
                key.template = new ArrayList<>(entry.getValue());
                result.add(key);
            }
            return new ProjectsRootNode(result);
        } catch (DAOException e) {
            return new ProjectsRootNode();
        }
    }
}
