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
import org.goobi.webapi.beans.GoobiProcess;
import org.goobi.webapi.beans.GoobiProcessStep;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.ArrayList;
import java.util.List;

import static javax.ws.rs.core.Response.Status;
import static javax.ws.rs.core.Response.status;

@Path("/processes")
public class Processes {

    @GET
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public List<GoobiProcess> getProcesses() {
        List<GoobiProcess> processes = new ArrayList<GoobiProcess>();

        processes.addAll(org.goobi.webapi.models.GoobiProcess.getAllProcesses());

        return processes;
    }

    @GET
    @Path("{ppnIdentifier}")
    public GoobiProcess getProcess(@PathParam("ppnIdentifier") String PPN) {

        if (!org.goobi.webapi.validators.IdentifierPpn.isValid(PPN)) {
            throw new WebApplicationException(
                    status(Status.BAD_REQUEST)
                            .entity("The given Identifier is no valid PPN.")
                            .build());
        }

        GoobiProcess process = org.goobi.webapi.models.GoobiProcess.getProcessByPPN(PPN);

        if (process == null) {
            throw new NotFoundException("No such process.");
        }

        return process;
    }

    @GET
    @Path("{ppnIdentifier}/steps")
    public List<GoobiProcessStep> getProcessSteps(@PathParam("ppnIdentifier") String PPN) {

        if (!org.goobi.webapi.validators.IdentifierPpn.isValid(PPN)) {
            throw new WebApplicationException(
                    status(Status.BAD_REQUEST)
                            .entity("The given Identifier is no valid PPN.")
                            .build());
        }

        List<GoobiProcessStep> resultList = org.goobi.webapi.models.GoobiProcess.getAllProcessSteps(PPN);

        if (resultList == null) {
            throw new NotFoundException("No such process.");
        }

        return resultList;
    }

}
