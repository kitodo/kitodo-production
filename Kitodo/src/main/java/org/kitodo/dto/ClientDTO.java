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

package org.kitodo.dto;

import java.util.List;

public class ClientDTO extends BaseDTO {
    private String name;
    private List<ProjectDTO> projects;
    private Integer projectsSize;

    /**
     * Gets title.
     *
     * @return The title.
     */
    public String getName() {
        return name;
    }

    /**
     * Sets title.
     *
     * @param name
     *            The title.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Gets projects.
     *
     * @return The projects.
     */
    public List<ProjectDTO> getProjects() {
        return projects;
    }

    /**
     * Sets projects.
     *
     * @param projects
     *            The projects.
     */
    public void setProjects(List<ProjectDTO> projects) {
        this.projects = projects;
    }

    /**
     * Gets projectsSize.
     *
     * @return The projectsSize.
     */
    public Integer getProjectsSize() {
        return projectsSize;
    }

    /**
     * Sets projectsSize.
     *
     * @param projectsSize
     *            The projectsSize.
     */
    public void setProjectsSize(Integer projectsSize) {
        this.projectsSize = projectsSize;
    }
}
