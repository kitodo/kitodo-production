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

import java.io.IOException;
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
import org.hibernate.Criteria;
import org.hibernate.criterion.Restrictions;

import de.sub.goobi.beans.Projekt;
import de.sub.goobi.beans.Prozess;
import de.sub.goobi.helper.Helper;

/**
 * The CatalogueConfiguration class provides the Jersey API URL pattern
 * ${SERVLET_CONTEXT}/rest/projects which returns the major data from the
 * project configuration in XML or JSON format.
 *
 * @author Matthias Ronge <matthias.ronge@zeutschel.de>
 */
@Path("/projects")
public class Projects {

    @GET
    @Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
    public ProjectsRootNode getAllProjectsWithTheirRespectiveTemplates() throws IOException {
        Map<Projekt, Set<Prozess>> data = new HashMap<Projekt, Set<Prozess>>();

        Criteria query = Helper.getHibernateSession().createCriteria(Prozess.class);
        @SuppressWarnings("unchecked")
        List<Prozess> processTemplates = query.add(Restrictions.eq("istTemplate", Boolean.TRUE)).list();
        for (Prozess processTemplate : processTemplates) {
            Projekt project = processTemplate.getProjekt();
            Set<Prozess> templates = data.containsKey(project) ? data.get(project) : new HashSet<Prozess>();
            templates.add(processTemplate);
            data.put(project, templates);
        }
        List<Projekt> result = new ArrayList<Projekt>();
        for (Projekt project : data.keySet()) {
            project.template = new ArrayList<Prozess>(data.get(project));
            result.add(project);
        }
        return new ProjectsRootNode(result);
    }
}
