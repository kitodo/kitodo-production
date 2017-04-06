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

package org.kitodo.webapi.resources;

import com.sun.jersey.api.NotFoundException;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.kitodo.webapi.beans.IdentifierPPN;
import org.kitodo.webapi.beans.KitodoProcess;
import org.kitodo.webapi.beans.KitodoProcessStep;
import org.kitodo.webapi.dao.KitodoProcessDAO;

@Path("/processes")
public class Processes {

    /**
     * Get Processes.
     *
     * @return list of KitodoProcess objects
     */
    @GET
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
    public List<KitodoProcess> getProcesses() {
        List<KitodoProcess> processes = new ArrayList<KitodoProcess>();

        processes.addAll(KitodoProcessDAO.getAllProcesses());

        return processes;
    }

    /**
     * Get Process.
     *
     * @param ippn
     *            IdentifierPPN object
     * @return KitodoProcess object
     */
    @GET
    @Path("{ppnIdentifier}")
    public KitodoProcess getProcess(@PathParam("ppnIdentifier") IdentifierPPN ippn) {

        KitodoProcess process = KitodoProcessDAO.getProcessByPPN(ippn);

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
     * @return KitodoProcessStep object
     */
    @GET
    @Path("{ppnIdentifier}/steps")
    public List<KitodoProcessStep> getProcessSteps(@PathParam("ppnIdentifier") IdentifierPPN ippn) {

        List<KitodoProcessStep> resultList = KitodoProcessDAO.getAllProcessSteps(ippn);

        if (resultList.isEmpty()) {
            throw new NotFoundException("No such process.");
        }
        return resultList;
    }

}
