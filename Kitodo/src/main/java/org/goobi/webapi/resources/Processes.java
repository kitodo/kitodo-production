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

import com.sun.jersey.api.NotFoundException;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.goobi.webapi.beans.GoobiProcess;
import org.goobi.webapi.beans.GoobiProcessStep;
import org.goobi.webapi.beans.IdentifierPPN;
import org.goobi.webapi.dao.GoobiProcessDAO;

@Path("/processes")
public class Processes {

    /**
     * Get Processes.
     *
     * @return list of GoobiProcess objects
     */
    @GET
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
    public List<GoobiProcess> getProcesses() {
        return new ArrayList<>(GoobiProcessDAO.getAllProcesses());
    }

    /**
     * Get Process.
     *
     * @param ippn
     *            IdentifierPPN object
     * @return GoobiProcess object
     */
    @GET
    @Path("{ppnIdentifier}")
    public GoobiProcess getProcess(@PathParam("ppnIdentifier") IdentifierPPN ippn) {

        GoobiProcess process = GoobiProcessDAO.getProcessByPPN(ippn);

        if (process == null) {
            throw new NotFoundException("No such process.");
        }

        return process;
    }

    /**
     * Get process tasks.
     *
     * @param ippn
     *            IdentifierPPN object
     * @return GoobiProcessStep object
     */
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
