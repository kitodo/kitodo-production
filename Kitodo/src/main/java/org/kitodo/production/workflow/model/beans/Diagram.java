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

package org.kitodo.production.workflow.model.beans;

import org.camunda.bpm.model.bpmn.instance.Process;

public class Diagram {

    private String id;
    private String title;

    /**
     * Constructor.
     *
     * @param process instance from model
     */
    public Diagram(Process process) {
        this.id = process.getId();
        this.title = process.getName();
    }

    /**
     * Get id of diagram.
     *
     * @return value of id
     */
    public String getId() {
        return id;
    }

    /**
     * Set id of diagram.
     *
     * @param id as String
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Get title.
     *
     * @return value of title
     */
    public String getTitle() {
        return title;
    }

    /**
     * Set title.
     *
     * @param title as java.lang.String
     */
    public void setTitle(String title) {
        this.title = title;
    }
}
