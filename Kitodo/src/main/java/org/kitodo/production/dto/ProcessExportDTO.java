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

package org.kitodo.production.dto;

import java.io.Serializable;
import java.util.Date;

/**
 * Data Transfer Object for process export.
 */
public class ProcessExportDTO implements Serializable {

    private final Integer id;
    private final String title;
    private final Date creationDate;
    private final Integer sortHelperImages;
    private final Integer sortHelperDocstructs;
    private final Integer sortHelperMetadata;
    private final String projectTitle;
    private final String status;

    /**
     * Constructor.
     */
    public ProcessExportDTO(Integer id, String title, Date creationDate,
                            Integer sortHelperImages, Integer sortHelperDocstructs,
                            Integer sortHelperMetadata, String projectTitle, String status) {
        this.id = id;
        this.title = title;
        this.creationDate = creationDate;
        this.sortHelperImages = sortHelperImages;
        this.sortHelperDocstructs = sortHelperDocstructs;
        this.sortHelperMetadata = sortHelperMetadata;
        this.projectTitle = projectTitle;
        this.status = status;
    }

    /**
     * Returns the process identifier.
     *
     * @return process ID
     */
    public Integer getId() {
        return id != null ? id : 0;
    }

    /**
     * Returns the process title.
     *
     * @return title of the process
     */
    public String getTitle() {
        return title != null ? title : "";
    }

    /**
     * Returns the creation date of the process.
     *
     * @return creation date
     */
    public Date getCreationDate() {
        return creationDate;
    }

    /**
     * Returns the number of images.
     *
     * @return image count
     */
    public Integer getSortHelperImages() {
        return sortHelperImages != null ? sortHelperImages : 0;
    }

    /**
     * Returns the number of docstructs.
     *
     * @return docstruct count
     */
    public Integer getSortHelperDocstructs() {
        return sortHelperDocstructs != null ? sortHelperDocstructs : 0;
    }

    /**
     * Returns the number of metadata entries.
     *
     * @return metadata count
     */
    public Integer getSortHelperMetadata() {
        return sortHelperMetadata != null ? sortHelperMetadata : 0;
    }

    /**
     * Returns the title of the project.
     *
     * @return project title
     */
    public String getProjectTitle() {
        return projectTitle != null ? projectTitle : "";
    }

    /**
     * Returns the process status.
     *
     * @return current status
     */
    public String getStatus() {
        return status != null ? status : "";
    }
}

