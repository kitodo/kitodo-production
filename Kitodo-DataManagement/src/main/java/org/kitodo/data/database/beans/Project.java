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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ForeignKey;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.kitodo.data.database.persistence.ProjectDAO;

@Entity
@Table(name = "project")
public class Project extends BaseIndexedBean implements Comparable<Project> {

    @Column(name = "title", nullable = false, unique = true)
    private String title;

    @Column(name = "dmsImportRootPath")
    private String dmsImportRootPath;

    @Column(name = "metsRightsOwner")
    private String metsRightsOwner = "";

    @Column(name = "metsRightsOwnerLogo")
    private String metsRightsOwnerLogo = "";

    @Column(name = "metsRightsOwnerSite")
    private String metsRightsOwnerSite = "";

    @Column(name = "metsRightsOwnerMail")
    private String metsRightsOwnerMail = "";

    @Column(name = "metsDigiprovReference")
    private String metsDigiprovReference = "";

    @Column(name = "metsDigiprovPresentation")
    private String metsDigiprovPresentation = "";

    @Column(name = "metsPointerPath")
    private String metsPointerPath = "";

    @Column(name = "metsPurl")
    private String metsPurl = "";

    @Column(name = "metsContentId")
    private String metsContentIDs = "";

    @Column(name = "startDate")
    private Date startDate;

    @Column(name = "endDate")
    private Date endDate;

    @Column(name = "numberOfPages")
    private Integer numberOfPages;

    @Column(name = "numberOfVolumes")
    private Integer numberOfVolumes;

    @Column(name = "active")
    private Boolean active = true;

    @ManyToMany(mappedBy = "projects", cascade = CascadeType.PERSIST)
    private List<User> users;

    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Process> processes;

    @ManyToMany(mappedBy = "projects", cascade = CascadeType.PERSIST)
    private List<Template> templates;

    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Folder> folders;

    @ManyToOne
    @JoinColumn(name = "client_id", foreignKey = @ForeignKey(name = "FK_project_client_id"))
    private Client client;

    /**
     * Folder to use as source for generation of derived resources.
     */
    @ManyToOne
    @JoinColumn(name = "generatorSource_folder_id", foreignKey = @ForeignKey(name = "FK_project_generatorSource_folder_id"))
    private Folder generatorSource;

    /**
     * Folder with media to use for the viewer.
     */
    @ManyToOne
    @JoinColumn(name = "mediaView_folder_id", foreignKey = @ForeignKey(name = "FK_project_mediaView_folder_id"))
    private Folder mediaView;

    /**
     * Folder with media to use for the preview.
     */
    @ManyToOne
    @JoinColumn(name = "preview_folder_id", foreignKey = @ForeignKey(name = "FK_project_preview_folder_id"))
    private Folder preview;

    /**
     * Constructor.
     */
    public Project() {
        this.processes = new ArrayList<>();
        this.users = new ArrayList<>();
        this.folders = new ArrayList<>();
        this.dmsImportRootPath = "";
    }

    public String getTitle() {
        return this.title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * Returns the list of users of this project.
     *
     * @return the folders
     */
    public List<User> getUsers() {
        initialize(new ProjectDAO(), this.users);
        if (Objects.isNull(this.users)) {
            this.users = new ArrayList<>();
        }
        return this.users;
    }

    public void setUsers(List<User> users) {
        this.users = users;
    }

    /**
     * Returns the list of processes of this project.
     *
     * @return the folders
     */
    public List<Process> getProcesses() {
        initialize(new ProjectDAO(), this.processes);
        if (Objects.isNull(this.processes)) {
            this.processes = new ArrayList<>();
        }
        return this.processes;
    }

    public void setProcesses(List<Process> processes) {
        this.processes = processes;
    }

    /**
     * Get templates.
     *
     * @return value of templates
     */
    public List<Template> getTemplates() {
        initialize(new ProjectDAO(), this.templates);
        if (Objects.isNull(this.templates)) {
            this.templates = new ArrayList<>();
        }
        return this.templates;
    }

    /**
     * Set templates.
     *
     * @param templates
     *            as list of templates
     */
    public void setTemplates(List<Template> templates) {
        this.templates = templates;
    }

    public String getDmsImportRootPath() {
        return this.dmsImportRootPath;
    }

    public void setDmsImportRootPath(String dmsImportRootPath) {
        this.dmsImportRootPath = dmsImportRootPath;
    }

    /**
     * Returns the list of folders of this project.
     *
     * @return the folders
     */
    public List<Folder> getFolders() {
        initialize(new ProjectDAO(), this.folders);
        if (Objects.isNull(this.folders)) {
            this.folders = new ArrayList<>();
        }
        return this.folders;
    }

    /**
     * Sets the list of folders of this project.
     *
     * @param folders
     *            list of folders to set
     */
    public void setFolders(List<Folder> folders) {
        this.folders = folders;
    }

    public String getMetsRightsOwner() {
        return this.metsRightsOwner;
    }

    public void setMetsRightsOwner(String metsRightsOwner) {
        this.metsRightsOwner = metsRightsOwner;
    }

    public String getMetsRightsOwnerLogo() {
        return this.metsRightsOwnerLogo;
    }

    public void setMetsRightsOwnerLogo(String metsRightsOwnerLogo) {
        this.metsRightsOwnerLogo = metsRightsOwnerLogo;
    }

    public String getMetsRightsOwnerSite() {
        return this.metsRightsOwnerSite;
    }

    public void setMetsRightsOwnerSite(String metsRightsOwnerSite) {
        this.metsRightsOwnerSite = metsRightsOwnerSite;
    }

    public String getMetsRightsOwnerMail() {
        return this.metsRightsOwnerMail;
    }

    public void setMetsRightsOwnerMail(String metsRigthsOwnerMail) {
        this.metsRightsOwnerMail = metsRigthsOwnerMail;
    }

    public String getMetsDigiprovReference() {
        return this.metsDigiprovReference;
    }

    public void setMetsDigiprovReference(String metsDigiprovReference) {
        this.metsDigiprovReference = metsDigiprovReference;
    }

    public String getMetsDigiprovPresentation() {
        return this.metsDigiprovPresentation;
    }

    public void setMetsDigiprovPresentation(String metsDigiprovPresentation) {
        this.metsDigiprovPresentation = metsDigiprovPresentation;
    }

    public String getMetsPointerPath() {
        return this.metsPointerPath;
    }

    public void setMetsPointerPath(String metsPointerPath) {
        this.metsPointerPath = metsPointerPath;
    }

    public void setMetsPurl(String metsPurl) {
        this.metsPurl = metsPurl;
    }

    public String getMetsPurl() {
        return this.metsPurl;
    }

    public void setMetsContentIDs(String contentIDs) {
        this.metsContentIDs = contentIDs;
    }

    public String getMetsContentIDs() {
        return this.metsContentIDs;
    }

    /**
     * Get number of volumes.
     *
     * @return number of volumes for this project
     */

    public Integer getNumberOfVolumes() {
        if (this.numberOfVolumes == null) {
            this.numberOfVolumes = 0;
        }
        return this.numberOfVolumes;
    }

    /**
     * Set number of volumes.
     *
     * @param numberOfVolumes
     *            for this project
     */

    public void setNumberOfVolumes(Integer numberOfVolumes) {
        this.numberOfVolumes = numberOfVolumes;
    }

    /**
     * Get number of pages.
     *
     * @return number of pages
     */
    public Integer getNumberOfPages() {
        if (this.numberOfPages == null) {
            this.numberOfPages = 0;
        }
        return this.numberOfPages;
    }

    /**
     * Set number of pages.
     *
     * @param numberOfPages
     *            the number of pages to set
     */
    public void setNumberOfPages(Integer numberOfPages) {
        this.numberOfPages = numberOfPages;
    }

    /**
     * Get start date.
     *
     * @return the start date
     */
    public Date getStartDate() {
        if (this.startDate == null) {
            this.startDate = new Date();
        }
        return this.startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    /**
     * Get end date.
     *
     * @return the end date
     */
    public Date getEndDate() {
        if (this.endDate == null) {
            this.endDate = new Date();
        }
        return this.endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    /**
     * Set if project is active.
     *
     * @param active
     *            whether project is active
     */
    public void setActive(boolean active) {
        this.active = active;
    }

    /**
     * Get if project is active.
     *
     * @return whether project is active
     */
    public boolean isActive() {
        return this.active;
    }

    /**
     * Gets client.
     *
     * @return The client.
     */
    public Client getClient() {
        return client;
    }

    /**
     * Sets client.
     *
     * @param client
     *            The client.
     */
    public void setClient(Client client) {
        this.client = client;
    }

    /**
     * Returns the folder to use as source for generation of derived resources
     * of this project.
     *
     * @return the source folder for generation
     */
    public Folder getGeneratorSource() {
        return generatorSource;
    }

    /**
     * Sets the folder to use as source for generation of derived resources of
     * this project.
     *
     * @param generatorSource
     *            source folder for generation to set
     */
    public void setGeneratorSource(Folder generatorSource) {
        this.generatorSource = generatorSource;
    }

    /**
     * Returns the folder to use for the media view.
     *
     * @return media view folder
     */
    public Folder getMediaView() {
        return mediaView;
    }

    /**
     * Sets the folder to use for the media view.
     *
     * @param mediaView
     *            media view folder
     */
    public void setMediaView(Folder mediaView) {
        this.mediaView = mediaView;
    }

    /**
     * Returns the folder to use for preview.
     *
     * @return preview folder
     */
    public Folder getPreview() {
        return preview;
    }

    /**
     * Sets the folder to use for preview.
     *
     * @param preview
     *            preview folder
     */
    public void setPreview(Folder preview) {
        this.preview = preview;
    }

    @Override
    public int compareTo(Project project) {
        return this.getTitle().compareTo(project.getTitle());
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }

        if (object instanceof Project) {
            Project project = (Project) object;
            return Objects.equals(this.getId(), project.getId());
        }

        return false;
    }

    @Override
    public int hashCode() {
        return this.title == null ? 0 : this.title.hashCode();
    }
}
