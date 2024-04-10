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

import java.util.List;

/**
 * Interface for batches of processes. Processes can be assembled in batches to
 * take on or complete tasks for entire batches with a single user interaction.
 */
public interface BatchInterface extends DataInterface {

    /**
     * Returns the title of the batch. If no textual title was assigned, the
     * title returned is “Batch ‹i›” with its database ID.
     *
     * @return the title of the batch
     */
    String getTitle();

    /**
     * Gives the batch a text-based title.
     *
     * @param title
     *            title to use
     */
    void setTitle(String title);

    /**
     * Returns the processes belonging to the batch. This list is not guaranteed
     * to be in reliable order.
     *
     * @return the processes belonging to the batch
     */
    List<ProcessInterface> getProcesses();

    /**
     * Sets the list of processes belonging to the batch. The list should not
     * contain duplicates, and must not contain {@code null}s.
     *
     * @param processes
     *            contain the list of processes belonging to the batch to be
     *            determined
     */
    void setProcesses(List<ProcessInterface> processes);

}
