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

package org.kitodo.data.database.beans;

import java.util.Objects;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ForeignKey;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name = "projectFileGroup")
public class ProjectFileGroup extends BaseBean {
    private static final long serialVersionUID = -5506252462891480484L;

    @Column(name = "name")
    private String name;

    @Column(name = "path")
    private String path;

    @Column(name = "mimeType")
    private String mimeType; // optional

    @Column(name = "suffix")
    private String suffix; // optional

    @Column(name = "folder")
    private String folder;

    @Column(name = "previewImage")
    private boolean previewImage;

    @ManyToOne
    @JoinColumn(name = "project_id", foreignKey = @ForeignKey(name = "FK_projectFileGroup_project_id"))
    private Project project;

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Project getProject() {
        return this.project;
    }

    public void setProject(Project project) {
        this.project = project;
    }

    public String getPath() {
        return this.path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getMimeType() {
        return this.mimeType;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    public String getSuffix() {
        return this.suffix;
    }

    public void setSuffix(String suffix) {
        this.suffix = suffix;
    }

    public String getFolder() {
        return this.folder;
    }

    public void setFolder(String folder) {
        this.folder = folder;
    }

    public boolean isPreviewImage() {
        return previewImage;
    }

    public void setPreviewImage(boolean previewImage) {
        this.previewImage = previewImage;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ProjectFileGroup that = (ProjectFileGroup) o;
        return previewImage == that.previewImage
            && Objects.equals(name, that.name)
            && Objects.equals(path, that.path)
            && Objects.equals(mimeType, that.mimeType)
            && Objects.equals(suffix, that.suffix)
            && Objects.equals(folder, that.folder)
            && Objects.equals(project, that.project);
    }

    /**
     * Returns a hash code value for the object.
     */
    @Override
    public int hashCode() {
        return Objects.hash(name, path, mimeType, suffix, folder, previewImage);
    }
}
