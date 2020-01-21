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

package org.kitodo.production.webapi.resources;

import com.sun.jersey.api.NotFoundException;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.kitodo.production.webapi.beans.IdentifierPPN;
import org.kitodo.production.webapi.beans.WebApiProcess;
import org.kitodo.production.webapi.beans.WebApiStep;
import org.kitodo.production.webapi.dao.WebApiProcessDAO;

@Path("/processes")
public class Processes {

    /**
     * Get Processes.
     *
     * @return list of WebApiProcess objects
     */
    @GET
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
    public List<WebApiProcess> getProcesses() {
        return new ArrayList<>(WebApiProcessDAO.getAllProcesses());
    }

    /**
     * Get Process.
     *
     * @param ippn
     *            IdentifierPPN object
     * @return WebApiProcess object
     */
    @GET
    @Path("{ppnIdentifier}")
    public WebApiProcess getProcess(@PathParam("ppnIdentifier") IdentifierPPN ippn) {
        WebApiProcess process = WebApiProcessDAO.getProcessByPPN(ippn);

        if (Objects.isNull(process)) {
            throw new NotFoundException("No such process.");
        }

        return process;
    }

    /**
     * Get process tasks.
     *
     * @param ippn
     *            IdentifierPPN object
     * @return WebApiStep object
     */
    @GET
    @Path("{ppnIdentifier}/steps")
    public List<WebApiStep> getProcessSteps(@PathParam("ppnIdentifier") IdentifierPPN ippn) {
        List<WebApiStep> processSteps = WebApiProcessDAO.getAllProcessSteps(ippn);

        if (processSteps.isEmpty()) {
            throw new NotFoundException("No such process.");
        }
        return processSteps;
    }

}
