/*
 * This file is part of the Goobi Application - a Workflow tool for the support of
 * mass digitization.
 *
 * Visit the websites for more information.
 *     - http://gdz.sub.uni-goettingen.de
 *     - http://www.goobi.org
 *     - https://github.com/goobi/goobi-production
 *
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
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
