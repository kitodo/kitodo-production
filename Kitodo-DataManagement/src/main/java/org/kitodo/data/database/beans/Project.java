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

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ForeignKey;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

import org.kitodo.data.database.helper.enums.MetadataFormat;

@XmlAccessorType(XmlAccessType.NONE)
// This annotation is to instruct the Jersey API not to generate arbitrary XML
// elements. Further XML elements can be
// added as needed by annotating with @XmlElement, but their respective names
// should be wisely chosen according to
// the Coding Guidelines (e.g. *english* names).
// TODO: get rid of this xml attributes
@XmlType(propOrder = {"template", "fieldConfig" })
// This annotation declares the desired order of XML elements generated and
// rather serves for better legibility of
// the generated XML. The list must be exhaustive and the properties have to be
// named according to their respective
// getter function, e.g. @XmlElement(name="field") getFieldConfig() must be
// referenced as "fieldConfig" here, not
// "field" as one might expect.
@Entity
@Table(name = "project")
public class Project extends BaseIndexedBean implements Comparable<Project> {
    private static final long serialVersionUID = -8543713331407761617L;

    /**
     * The constant ANCHOR_SEPARATOR holds the character U+00A6
     * (&ldquo;&brvbar;&rdquo;) which can be used to separate multiple anchors, if
     * several of them are needed in one project. The anchors must then be listed
     * the hierarchical order they have to be applied, that is the topmost anchor in
     * first place, followed by the second one and so on.
     */
    public static final String ANCHOR_SEPARATOR = "\u00A6";

    @Column(name = "title", nullable = false, unique = true)
    private String title;

    @Column(name = "useDmsImport")
    private boolean useDmsImport;

    @Column(name = "dmsImportTimeOut")
    private Integer dmsImportTimeOut = 20000;

    @Column(name = "dmsImportRootPath")
    private String dmsImportRootPath;

    @Column(name = "dmsImportImagesPath")
    private String dmsImportImagesPath;

    @Column(name = "dmsImportSuccessPath")
    private String dmsImportSuccessPath;

    @Column(name = "dmsImportErrorPath")
    private String dmsImportErrorPath;

    @Column(name = "dmsImportCreateProcessFolder")
    private Boolean dmsImportCreateProcessFolder;

    @Column(name = "fileFormatInternal")
    private String fileFormatInternal;

    @Column(name = "fileFormatDmsExport")
    private String fileFormatDmsExport;

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

    @Column(name = "metsDigiprovReferenceAnchor")
    private String metsDigiprovReferenceAnchor = "";

    @Column(name = "metsDigiprovPresentationAnchor")
    private String metsDigiprovPresentationAnchor = "";

    @Column(name = "metsPointerPath")
    private String metsPointerPath = "";

    @Column(name = "metsPointerPathAnchor")
    private String metsPointerPathAnchor = "";

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

    @Column(name = "projectIsArchived")
    private Boolean projectIsArchived = false;

    @ManyToMany(mappedBy = "projects", cascade = CascadeType.PERSIST)
    private List<User> users;

    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Process> processes;

    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProjectFileGroup> projectFileGroups;

    @ManyToOne
    @JoinColumn(name = "client_id", foreignKey = @ForeignKey(name = "FK_project_client_id"))
    private Client client;

    @Transient
    @XmlElement(name = "template")
    public List<Process> template; // The ‘template’ variable is populated from
                                   // org.goobi.webapi.resources.Projects
    // when calling ${SERVLET_CONTEXT}/rest/projects to output the templates
    // available within a project as XML child
    // nodes of the respective project.

    /**
     * Constructor.
     */
    public Project() {
        this.processes = new ArrayList<>();
        this.users = new ArrayList<>();
        this.useDmsImport = false;
        this.dmsImportTimeOut = 0;
        this.dmsImportImagesPath = "";
        this.dmsImportRootPath = "";
        this.dmsImportSuccessPath = "";
        this.dmsImportCreateProcessFolder = false;
        this.fileFormatInternal = MetadataFormat.getDefaultFileFormat().getName();
        this.fileFormatDmsExport = MetadataFormat.getDefaultFileFormat().getName();
    }

    @XmlAttribute(name = "key")
    public String getTitle() {
        return this.title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public List<User> getUsers() {
        if (this.users == null) {
            this.users = new ArrayList<>();
        }
        return this.users;
    }

    public void setUsers(List<User> users) {
        this.users = users;
    }

    public List<Process> getProcesses() {
        if (this.processes == null) {
            this.processes = new ArrayList<>();
        }
        return this.processes;
    }

    public void setProcesses(List<Process> processes) {
        this.processes = processes;
    }

    public String getDmsImportImagesPath() {
        return this.dmsImportImagesPath;
    }

    public void setDmsImportImagesPath(String dmsImportImagesPath) {
        this.dmsImportImagesPath = dmsImportImagesPath;
    }

    public String getDmsImportRootPath() {
        return this.dmsImportRootPath;
    }

    public void setDmsImportRootPath(String dmsImportRootPath) {
        this.dmsImportRootPath = dmsImportRootPath;
    }

    public String getDmsImportSuccessPath() {
        return this.dmsImportSuccessPath;
    }

    public void setDmsImportSuccessPath(String dmsImportSuccessPath) {
        this.dmsImportSuccessPath = dmsImportSuccessPath;
    }

    public Integer getDmsImportTimeOut() {
        return this.dmsImportTimeOut;
    }

    public void setDmsImportTimeOut(Integer dmsImportTimeOut) {
        this.dmsImportTimeOut = dmsImportTimeOut;
    }

    public boolean isUseDmsImport() {
        return this.useDmsImport;
    }

    public void setUseDmsImport(boolean useDmsImport) {
        this.useDmsImport = useDmsImport;
    }

    public String getDmsImportErrorPath() {
        return this.dmsImportErrorPath;
    }

    public void setDmsImportErrorPath(String dmsImportErrorPath) {
        this.dmsImportErrorPath = dmsImportErrorPath;
    }

    /**
     * Get DMS import create process folder.
     *
     * @return true or false
     */
    public boolean isDmsImportCreateProcessFolder() {
        if (this.dmsImportCreateProcessFolder == null) {
            this.dmsImportCreateProcessFolder = false;
        }
        return this.dmsImportCreateProcessFolder;
    }

    /**
     * Set DMS import create process folder.
     *
     * @param dmsImportCreateProcessFolder
     *            true or false
     */
    public void setDmsImportCreateProcessFolder(boolean dmsImportCreateProcessFolder) {
        this.dmsImportCreateProcessFolder = dmsImportCreateProcessFolder;
    }

    public List<ProjectFileGroup> getProjectFileGroups() {
        if (this.projectFileGroups == null) {
            this.projectFileGroups = new ArrayList<>();
        }
        return this.projectFileGroups;
    }

    public void setProjectFileGroups(List<ProjectFileGroup> projectFileGroups) {
        this.projectFileGroups = projectFileGroups;
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

    public String getMetsDigiprovReferenceAnchor() {
        return this.metsDigiprovReferenceAnchor;
    }

    public void setMetsDigiprovReferenceAnchor(String metsDigiprovReferenceAnchor) {
        this.metsDigiprovReferenceAnchor = metsDigiprovReferenceAnchor;
    }

    public String getMetsDigiprovPresentation() {
        return this.metsDigiprovPresentation;
    }

    public void setMetsDigiprovPresentation(String metsDigiprovPresentation) {
        this.metsDigiprovPresentation = metsDigiprovPresentation;
    }

    public String getMetsDigiprovPresentationAnchor() {
        return this.metsDigiprovPresentationAnchor;
    }

    public void setMetsDigiprovPresentationAnchor(String metsDigiprovPresentationAnchor) {
        this.metsDigiprovPresentationAnchor = metsDigiprovPresentationAnchor;
    }

    public String getMetsPointerPath() {
        return this.metsPointerPath;
    }

    public void setMetsPointerPath(String metsPointerPath) {
        this.metsPointerPath = metsPointerPath;
    }

    public void setMetsPointerPathAnchor(String metsPointerPathAnchor) {
        this.metsPointerPathAnchor = metsPointerPathAnchor;
    }

    public String getMetsPointerPathAnchor() {
        return this.metsPointerPathAnchor;
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

    public String getFileFormatInternal() {
        return this.fileFormatInternal;
    }

    public void setFileFormatInternal(String fileFormatInternal) {
        this.fileFormatInternal = fileFormatInternal;
    }

    public String getFileFormatDmsExport() {
        return this.fileFormatDmsExport;
    }

    public void setFileFormatDmsExport(String fileFormatDmsExport) {
        this.fileFormatDmsExport = fileFormatDmsExport;
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
     * Set if projects is archived.
     *
     * @param projectIsArchived
     *            true or false
     */
    public void setProjectIsArchived(Boolean projectIsArchived) {
        if (projectIsArchived == null) {
            projectIsArchived = false;
        }
        this.projectIsArchived = projectIsArchived;
    }

    public Boolean getProjectIsArchived() {
        return this.projectIsArchived;
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

    @Override
    public int compareTo(Project project) {
        return this.getTitle().compareTo(project.getTitle());
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof Project)) {
            return false;
        }
        Project other = (Project) object;
        return this.title == null ? other.title == null : this.title.equals(other.title);
    }

    @Override
    public int hashCode() {
        return this.title == null ? 0 : this.title.hashCode();
    }

    // Here will be methods which should be in ProjectService but are used by
    // jsp files

    public boolean isDeleteAble() {
        return this.processes.size() == 0;
    }
}
