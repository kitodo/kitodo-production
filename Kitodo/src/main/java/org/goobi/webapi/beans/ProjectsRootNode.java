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

package org.goobi.webapi.beans;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.kitodo.data.database.beans.Project;

/**
 * The ProjectsRootNode class is necessary to control the XML root element’s
 * name to be ‘projects’. Simply annotating the org.kitodo.data.database.beans.
 * Project class with @XmlRootElement(name = "project") results in a wrapping
 * element named &lt;projekts&gt; who’s name is still derived from the classes’
 * name, not from the ‘name’ property set in the annotation and cannot be
 * changed otherwise.
 * 
 * @author Matthias Ronge &lt;matthias.ronge@zeutschel.de&gt;
 */
@XmlRootElement(name = "projects")
public class ProjectsRootNode {
    @XmlElement(name = "project")
    private List<Project> projects;

    public ProjectsRootNode() {
        projects = new ArrayList<>();
    }

    public ProjectsRootNode(Collection<Project> data) {
        projects = new ArrayList<>(data);
    }

    /**
     * Copy Constructor to instantiate an already populated ProjectsRootNode.
     * Copying is done that way that a *new* list object is genererated, so
     * modifying the list (eg. removing, adding or resorting its elements) will
     * *not* influence the list the copy was derived from. However, no copies
     * are created of the list *entries*, so modifying a Project in the list
     * *will* modify the equal Project in the list the copy was derived from.
     *
     * @param toCopy
     *            ProjectsRootNode to create a copy from
     */
    public ProjectsRootNode(ProjectsRootNode toCopy) {
        this.projects = toCopy.projects != null ? new ArrayList<>(toCopy.projects) : null;
    }

    public void setProjects(List<Project> projects) {
        this.projects = projects;
    }

}
