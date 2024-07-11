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

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.kitodo.data.database.beans.Template;
import org.kitodo.data.exceptions.DataException;
import org.kitodo.data.interfaces.TemplateInterface;
import org.primefaces.model.SortOrder;

/**
 * Specifies the special database-related functions of the template service.
 */
public interface DatabaseTemplateServiceInterface extends SearchDatabaseServiceInterface<Template> {

    /**
     * Determines all process templates with a specific docket.
     *
     * @param docketId
     *            record number of the docket
     * @return list that is not empty if something was found, otherwise empty
     *         list
     */
    public Collection<?> findByDocket(int docketId) throws DataException;

    /**
     * Returns all process templates that can still be assigned to a project.
     * Returns the process templates that
     * <ul>
     * <li>are active</li>
     * <li>and that are not already assigned to the project to be
     * edited<sup>✻</sup>,</li>
     * <li>and that belong to the client for which the logged-in user is
     * currently working.</li>
     * </ul>
     * These are displayed in the templateAddPopup.
     * 
     * <p>
     * {T} := currentUser.currentClient.processTemplates[active] -
     * projectEdited.processTemplates
     *
     * <p>
     * <b>Implementation Requirements:</b><br>
     * The function requires that the thread is assigned to a logged-in user.
     * 
     * <p>
     * <b>Implementation Note:</b><br>
     * ✻) If the project has already been saved. If not, if {@code projectID} is
     * {@code null}, returns all active process templates for the current
     * client.
     * 
     * @param projectId
     *            ID of project which is going to be edited. May be
     *            {@code null}.
     * @return process templates that can be assigned
     */
    List<? extends TemplateInterface> findAllAvailableForAssignToProject(Integer projectId) throws DataException;

    /**
     * Determines all process templates with a specific ruleset.
     *
     * @param rulesetId
     *            record number of the ruleset
     * @return list that is not empty if something was found, otherwise empty
     *         list
     */
    /*
     * Used in RulesetForm to find out whether a ruleset is used in a process
     * template. (Then it may not be deleted.) Is only checked for isEmpty().
     */
    Collection<?> findByRuleset(int rulesetId) throws DataException;

    /**
     * Sets whether to display non-active production templates in the list.
     *
     * <p>
     * <b>API Note:</b><br>
     * Affects the results of functions {@link #countDatabaseRows()} and
     * {@link #loadData(int, int, String, SortOrder, Map)} in
     * {@link SearchDatabaseServiceInterface}.
     * 
     * @param showInactiveTemplates
     *            as boolean
     */
    /*
     * Here, a value is set that affects the generally specified functions
     * countResults() and loadData() in SearchDatabaseServiceInterface. However,
     * in DatabaseProjectServiceInterface and DatabaseTaskServiceInterface, an
     * additional functions countResults() and loadData() are specified with
     * additional parameters, and the generally specified functions
     * countResults() and loadData() from SearchDatabaseServiceInterface are not
     * used. This could be equalized at some point in the future.
     */
    void setShowInactiveTemplates(boolean showInactiveTemplates);

    /**
     * Returns all process templates of the specified client and name.
     *
     * @param title
     *            naming of the projects
     * @param clientId
     *            record number of the client whose projects are queried
     * @return all process templates of the specified client and name
     */
    List<Template> getTemplatesWithTitleAndClient(String title, Integer clientId);
}
