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

import com.sun.jersey.api.NotFoundException;
import org.goobi.webapi.models.GoobiProcess;
import org.goobi.webapi.beans.GoobiProcessInformation;
import org.goobi.webapi.validators.IdentifierPpn;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.UriInfo;
import java.util.ArrayList;
import java.util.List;

@Path("/processes")
public class Processes {

	@Context UriInfo uriInfo;
	@Context Request request;

	@GET
	@Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
	public List<GoobiProcessInformation> getProcesses(){
		List<GoobiProcessInformation> processes = new ArrayList<GoobiProcessInformation>();

		processes.addAll(GoobiProcess.getAllProcesses());

		return processes;
	}

	@Path("{ppnIdentifier}")
	public Process getProcess(@PathParam("ppnIdentifier") String ppnIdentifier) {
		if (IdentifierPpn.isValid((ppnIdentifier))) {
			return new Process(uriInfo, request, ppnIdentifier);
		} else {
			throw new NotFoundException("Given PPN is invalid.");
		}
	}
}
