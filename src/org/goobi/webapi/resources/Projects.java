/*
 * This file is part of the Goobi Application - a Workflow tool for the support of
 * mass digitization.
 *
 * Visit the websites for more information.
 *     - http://gdz.sub.uni-goettingen.de
 *     - http://www.goobi.org
 *     - http://launchpad.net/goobi-production
 *
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU General Public License for more details. You
 * should have received a copy of the GNU General Public License along with this
 * program; if not, write to the Free Software Foundation, Inc., 59 Temple Place,
 * Suite 330, Boston, MA 02111-1307 USA
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

		@SuppressWarnings("unchecked")
		List<Prozess> processes = (List<Prozess>) Helper.getHibernateSession().createCriteria(Prozess.class).list();
		for (Prozess process : processes) {
			if (process.isIstTemplate()) {
				Projekt project = process.getProjekt();
				Set<Prozess> templates = data.containsKey(project) ? data.get(project) : new HashSet<Prozess>();
				templates.add(process);
				data.put(project, templates);
			}
		}
		List<Projekt> result = new ArrayList<Projekt>();
		for(Projekt project : data.keySet()){
			project.template = new ArrayList<Prozess>(data.get(project));
			result.add(project);
		}
		return new ProjectsRootNode(result);
	}
}
