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

package org.kitodo.production.services.data.interfaces;

import java.util.List;

import org.kitodo.data.database.beans.Workflow;
import org.kitodo.data.database.enums.WorkflowStatus;

/**
 * Specifies the special database-related functions of the workflow service.
 */
public interface DatabaseWorkflowServiceInterface extends SearchDatabaseServiceInterface<Workflow> {

    /**
     * Returns all available workflows. These are all workflows that are in
     * {@link WorkflowStatus} {@code ACTIVE} for the client for which the
     * current user is working at the moment.
     * 
     * <p>
     * <b>Implementation Requirements:</b><br>
     * The function requires that the thread is assigned to a logged-in user.
     *
     * @return all available workflows
     */
    List<Workflow> getAvailableWorkflows();

    /**
     * Returns all active workflows. These are all workflows that are in
     * {@link WorkflowStatus} {@code ACTIVE}.
     *
     * <p>
     * <b>API Note:</b><br>
     * This method actually returns all objects of all clients and is therefore
     * more suitable for operational purposes, rather not for display purposes.
     * 
     * @return all active workflows
     */
    List<Workflow> getAllActiveWorkflows();
}
