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

package org.kitodo.data.interfaces;

import java.io.InputStream;
import java.util.List;

public interface TemplateInterface extends DataInterface {

    /**
     * Returns the process template name.
     *
     * @return the process template name
     */
    String getTitle();

    /**
     * Sets the process template name.
     *
     * @param title
     *            the process template name
     */
    void setTitle(String title);

    /**
     * Returns the day the process template was created.
     *
     * @return the day the process template was created
     */
    String getCreationDate();

    /**
     * Sets the day of process template creation.
     *
     * @param creationDate
     *            the day of process template creation
     */
    void setCreationDate(String creationDate);

    /**
     * Returns the docket generation statement to use when creating dockets for
     * processes derived from this process template.
     *
     * @return the docket generation statement
     */
    DocketInterface getDocket();

    /**
     * Sets the docket generation statement to use when creating dockets for
     * processes derived from this process template.
     *
     * @param docket
     *            the docket generation statement
     */
    void setDocket(DocketInterface docket);

    /**
     * Returns the business domain specification processes derived from this
     * process template template shall be using.
     *
     * @return the business domain specification
     */
    RulesetInterface getRuleset();

    /**
     * Sets the business domain specification processes derived from this
     * process template template shall be using.
     *
     * @param ruleset
     *            the business domain specification
     */
    void setRuleset(RulesetInterface ruleset);

    /**
     * Returns the task list of this process template.
     *
     * @return the task list
     */
    List<TaskInterface> getTasks();

    /**
     * Sets the task list of this process template.
     *
     * @param tasks
     *            the task list
     */
    void setTasks(List<TaskInterface> tasks);

    /**
     * Returns a read handle for the SVG image of this production template's
     * workflow. If the file cannot be read (due to an error), returns an empty
     * input stream.
     *
     * @return read file handle for the SVG
     */
    InputStream getDiagramImage();

    /**
     * Returns whether this production template is active. Production templates
     * that are no not to be used (anymore) can be deactivated. If processes
     * exist from them, they cannot be deleted.
     *
     * @return whether this production template is active
     */
    boolean isActive();

    /**
     * Sets whether this production template is active.
     *
     * @param active
     *            whether this production template is active
     */
    void setActive(boolean active);

    /**
     * Sets the workflow from which the production template was created.
     *
     * @param workflow
     *            workflow to set
     */
    void setWorkflow(WorkflowInterface workflow);

    /**
     * Returns the workflow from which the production template was created. The
     * tasks of the production template are not created directly in the
     * production template, but are edited using the workflow.
     *
     * @return the workflow
     */
    WorkflowInterface getWorkflow();

    /**
     * Returns whether the production template is valid. To do this, it must
     * contain at least one task and each task must have at least one role
     * assigned to it.
     *
     * @return whether the production template is valid
     */
    boolean isCanBeUsedForProcess();

    /**
     * Sets whether the production template is valid. The setter can be used
     * when representing data from a third-party source. Internally it depends
     * on whether the production template is valid. Setting this to true cannot
     * fix errors.
     *
     * @param canBeUsedForProcess
     *            as boolean
     * @throws UnsupportedOperationException
     *             when trying to set this on the database
     */
    default void setCanBeUsedForProcess(boolean canBeUsedForProcess) {
        throw new UnsupportedOperationException("not allowed");
    }

    /**
     * Returns the list of all projects that use this production template. A
     * production template can be used in multiple projects, even across
     * multiple clients. This list is not guaranteed to be in reliable order.
     *
     * @return the list of all projects that use this production template
     */
    List<ProjectInterface> getProjects();

    /**
     * Sets the list of all projects that use this production template. The list
     * should not contain duplicates, and must not contain {@code null}s.
     *
     * @param projects
     *            projects list to set
     */
    void setProjects(List<ProjectInterface> projects);
}
