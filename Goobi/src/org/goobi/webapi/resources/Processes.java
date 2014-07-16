/**
 * This file is part of the Goobi Application - a Workflow tool for the support of mass digitization.
 * 
 * Visit the websites for more information. 
 *     		- http://www.goobi.org
 *     		- http://launchpad.net/goobi-production
 * 		    - http://gdz.sub.uni-goettingen.de
 * 			- http://www.intranda.com
 * 			- http://digiverso.com 
 * 
 * This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free
 * Software Foundation; either version 2 of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
 * Linking this library statically or dynamically with other modules is making a combined work based on this library. Thus, the terms and conditions
 * of the GNU General Public License cover the whole combination. As a special exception, the copyright holders of this library give you permission to
 * link this library with independent modules to produce an executable, regardless of the license terms of these independent modules, and to copy and
 * distribute the resulting executable under terms of your choice, provided that you also meet, for each linked independent module, the terms and
 * conditions of the license of that module. An independent module is a module which is not derived from or based on this library. If you modify this
 * library, you may extend this exception to your version of the library, but you are not obliged to do so. If you do not wish to do so, delete this
 * exception statement from your version.
 */
package org.goobi.webapi.resources;

import com.sun.jersey.api.NotFoundException;
import org.goobi.webapi.beans.GoobiProcess;
import org.goobi.webapi.beans.GoobiProcessStep;
import org.goobi.webapi.beans.IdentifierPPN;
import org.goobi.webapi.dao.GoobiProcessDAO;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.ArrayList;
import java.util.List;

@Path("/processes")
public class Processes {

    @GET
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public List<GoobiProcess> getProcesses() {
        List<GoobiProcess> processes = new ArrayList<GoobiProcess>();

        processes.addAll(GoobiProcessDAO.getAllProcesses());

        return processes;
    }

    @GET
    @Path("{ppnIdentifier}")
    public GoobiProcess getProcess(@PathParam("ppnIdentifier") IdentifierPPN ippn) {

        GoobiProcess process = GoobiProcessDAO.getProcessByPPN(ippn);

        if (process == null) {
            throw new NotFoundException("No such process.");
        }

        return process;
    }

    @GET
    @Path("{ppnIdentifier}/steps")
    public List<GoobiProcessStep> getProcessSteps(@PathParam("ppnIdentifier") IdentifierPPN ippn) {

        List<GoobiProcessStep> resultList = GoobiProcessDAO.getAllProcessSteps(ippn);

        if (resultList.isEmpty()) {
            throw new NotFoundException("No such process.");
        }

        return resultList;
    }

}
