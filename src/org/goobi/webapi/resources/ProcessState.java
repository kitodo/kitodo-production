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
import org.goobi.webapi.models.GoobiProcessState;
import org.goobi.webapi.beans.GoobiProcessStateInformation;

import javax.ws.rs.GET;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.UriInfo;
import java.util.List;

public class ProcessState {

	@Context UriInfo uriInfo;
	@Context Request request;
	String ppnIdentifier;

	public ProcessState(UriInfo uriInfo, Request request, String ppnIdentifier) {
		this.uriInfo = uriInfo;
		this.request = request;
		this.ppnIdentifier = ppnIdentifier;
	}

	@GET
	@Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
	public List<GoobiProcessStateInformation> getProzessState() {
		List<GoobiProcessStateInformation> listStates;

		listStates = GoobiProcessState.getProcessState(ppnIdentifier);

		if ((listStates == null) || (listStates.size() == 0)) {
			throw new NotFoundException("No such Goobi process.");
		}

		return listStates;
	}

}
