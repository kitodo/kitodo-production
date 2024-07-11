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
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.ForeignKey;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.apache.commons.collections.CollectionUtils;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;
import org.kitodo.data.database.enums.PreviewHoverMode;
import org.kitodo.data.database.persistence.ProjectDAO;
import org.kitodo.data.interfaces.ClientInterface;
import org.kitodo.data.interfaces.ProjectInterface;
import org.kitodo.data.interfaces.TemplateInterface;
import org.kitodo.data.interfaces.UserInterface;

@Entity
@Table(name = "project")
public class Project extends BaseIndexedBean implements ProjectInterface, Comparable<Project> {

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

    @LazyCollection(LazyCollectionOption.FALSE)
    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Process> processes;

    @LazyCollection(LazyCollectionOption.FALSE)
    @ManyToMany(mappedBy = "projects", cascade = CascadeType.PERSIST)
    private List<Template> templates;

    @ManyToOne
    @JoinColumn(name = "default_importconfiguration_id",
            foreignKey = @ForeignKey(name = "FK_project_default_importconfiguration_id"))
    private ImportConfiguration defaultImportConfiguration;

    @ManyToOne
    @JoinColumn(name = "default_child_process_importconfiguration_id",
            foreignKey = @ForeignKey(name = "FK_project_default_child_process_importconfiguration_id"))
    private ImportConfiguration defaultChildProcessImportConfiguration;

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
     * Field to define mode of hover in preview.
     */
    @Column(name = "preview_hover_mode")
    @Enumerated(EnumType.STRING)
    private PreviewHoverMode previewHoverMode = PreviewHoverMode.OVERLAY;

    /**
     * Folder with media to use for the audio preview.
     */
    @ManyToOne
    @JoinColumn(name = "preview_audio_folder_id", foreignKey = @ForeignKey(name = "FK_project_preview_audio_folder_id"))
    private Folder audioPreview;

    /**
     * Folder with media to use for the audio viewer.
     */
    @ManyToOne
    @JoinColumn(name = "mediaView_audio_folder_id", foreignKey = @ForeignKey(name = "FK_project_mediaView_audio_folder_id"))
    private Folder audioMediaView;

    /**
     * Field to define the status of the audio media view waveform.
     */
    @Column(name = "mediaView_audio_waveform")
    private Boolean audioMediaViewWaveform = false;

    /**
     * Folder with media to use for the video preview.
     */
    @ManyToOne
    @JoinColumn(name = "preview_video_folder_id", foreignKey = @ForeignKey(name = "FK_project_preview_video_folder_id"))
    private Folder videoPreview;

    /**
     * Folder with media to use for the video viewer.
     */
    @ManyToOne
    @JoinColumn(name = "mediaView_video_folder_id", foreignKey = @ForeignKey(name = "FK_project_mediaView_video_folder_id"))
    private Folder videoMediaView;

    /**
     * Filename length for renaming media files of processes in this project.
     */
    @Column(name = "filename_length")
    private Integer filenameLength;

    /**
     * Constructor.
     */
    public Project() {
        this.processes = new ArrayList<>();
        this.users = new ArrayList<>();
        this.folders = new ArrayList<>();
        this.dmsImportRootPath = "";
    }

    @Override
    public String getTitle() {
        return this.title;
    }

    @Override
    public void setTitle(String title) {
        this.title = title;
    }

    @Override
    public List<User> getUsers() {
        initialize(new ProjectDAO(), this.users);
        if (Objects.isNull(this.users)) {
            this.users = new ArrayList<>();
        }
        return this.users;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void setUsers(List<? extends UserInterface> users) {
        this.users = (List<User>) users;
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

    @Override
    public String getMetsRightsOwner() {
        return this.metsRightsOwner;
    }

    @Override
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

    @Override
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

    @Override
    public void setNumberOfVolumes(Integer numberOfVolumes) {
        this.numberOfVolumes = numberOfVolumes;
    }

    @Override
    public Integer getNumberOfPages() {
        if (this.numberOfPages == null) {
            this.numberOfPages = 0;
        }
        return this.numberOfPages;
    }

    @Override
    public void setNumberOfPages(Integer numberOfPages) {
        this.numberOfPages = numberOfPages;
    }

    @Override
    public Date getStartDate() {
        if (this.startDate == null) {
            this.startDate = new Date();
        }
        return this.startDate;
    }

    @Override
    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    @Override
    public Date getEndDate() {
        if (this.endDate == null) {
            this.endDate = new Date();
        }
        return this.endDate;
    }

    @Override
    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    @Override
    public void setActive(boolean active) {
        this.active = active;
    }

    @Override
    public boolean isActive() {
        return this.active;
    }

    @Override
    public Client getClient() {
        return client;
    }

    @Override
    public void setClient(ClientInterface client) {
        this.client = (Client) client;
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
     *         preview folder
     */
    public void setPreview(Folder preview) {
        this.preview = preview;
    }

    /**
     * Sets the preview hover mode.
     *
     * @param previewHoverMode preview hover mode
     */
    public void setPreviewHoverMode(PreviewHoverMode previewHoverMode) {
        this.previewHoverMode = previewHoverMode;
    }

    /**
     * Returns the preview hover mode.
     *
     * @return The preview hover mode
     */
    public PreviewHoverMode getPreviewHoverMode() {
        return previewHoverMode;
    }

    /**
     * Returns the folder to use for audio preview.
     *
     * @return audio preview folder
     */
    public Folder getAudioPreview() {
        return audioPreview;
    }

    /**
     * Sets the folder to use for audio preview.
     *
     * @param audioPreview
     *         audio preview folder
     */
    public void setAudioPreview(Folder audioPreview) {
        this.audioPreview = audioPreview;
    }

    /**
     * Returns the folder to use for the audio media view.
     *
     * @return the audio media view folder
     */
    public Folder getAudioMediaView() {
        return audioMediaView;
    }

    /**
     * Sets the folder to use for the audio media view.
     *
     * @param audioMediaView
     *         audio media view folder
     */
    public void setAudioMediaView(Folder audioMediaView) {
        this.audioMediaView = audioMediaView;
    }

    /**
     * Get the status of the audio media view waveform.
     *
     * @return True if is active
     */
    public boolean isAudioMediaViewWaveform() {
        return audioMediaViewWaveform;
    }

    /**
     * Set the status of the audio media view waveform.
     *
     * @param audioMediaViewWaveform True if is active
     */
    public void setAudioMediaViewWaveform(boolean audioMediaViewWaveform) {
        this.audioMediaViewWaveform = audioMediaViewWaveform;
    }

    /**
     * Returns the folder to use for video preview.
     *
     * @return video preview folder
     */
    public Folder getVideoPreview() {
        return videoPreview;
    }

    /**
     * Sets the folder to use for video preview.
     *
     * @param videoPreview
     *         video preview folder
     */
    public void setVideoPreview(Folder videoPreview) {
        this.videoPreview = videoPreview;
    }

    /**
     * Returns the folder to use for the video media view.
     *
     * @return the video media view folder
     */
    public Folder getVideoMediaView() {
        return videoMediaView;
    }

    /**
     * Sets the folder to use for the video media view.
     *
     * @param videoMediaView
     *         video media view folder
     */
    public void setVideoMediaView(Folder videoMediaView) {
        this.videoMediaView = videoMediaView;
    }

    /**
     * Get defaultImportConfiguration.
     *
     * @return value of defaultImportConfiguration
     */
    public ImportConfiguration getDefaultImportConfiguration() {
        return defaultImportConfiguration;
    }

    /**
     * Set defaultImportConfiguration.
     *
     * @param defaultImportConfiguration as org.kitodo.data.database.beans.ImportConfiguration
     */
    public void setDefaultImportConfiguration(ImportConfiguration defaultImportConfiguration) {
        this.defaultImportConfiguration = defaultImportConfiguration;
    }

    /**
     * Get defaultChildProcessImportConfiguration.
     *
     * @return value of defaultChildProcessImportConfiguration
     */
    public ImportConfiguration getDefaultChildProcessImportConfiguration() {
        return defaultChildProcessImportConfiguration;
    }

    /**
     * Set defaultChildProcessImportConfiguration.
     *
     * @param defaultChildProcessImportConfiguration as org.kitodo.data.database.beans.ImportConfiguration
     */
    public void setDefaultChildProcessImportConfiguration(ImportConfiguration defaultChildProcessImportConfiguration) {
        this.defaultChildProcessImportConfiguration = defaultChildProcessImportConfiguration;
    }

    /**
     * Get filename length.
     * @return filename length
     */
    public Integer getFilenameLength() {
        if (Objects.isNull(filenameLength)) {
            filenameLength = 8;
        }
        return filenameLength;
    }

    /**
     * Set filename length.
     * @param filenameLength as Integer
     */
    public void setFilenameLength(Integer filenameLength) {
        this.filenameLength = filenameLength;
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

    @Override
    public List<? extends TemplateInterface> getActiveTemplates() {
        if (Objects.isNull(templates)) {
            return null;
        }
        return templates.stream().filter(template -> template.isActive()).collect(Collectors.toList());
    }

    @SuppressWarnings("unchecked")
    @Override
    public void setActiveTemplates(List<? extends TemplateInterface> activeTemplates) {
        if (Objects.isNull(activeTemplates)) {
            activeTemplates = Collections.emptyList();
        }
        Map<Integer, ? extends TemplateInterface> activeTemplatesMap = activeTemplates.stream()
                .collect(Collectors.toMap(TemplateInterface::getId, Function.identity()));

        if (Objects.isNull(this.templates) && CollectionUtils.isNotEmpty(activeTemplates)) {
            this.templates = new ArrayList<>();
        }
        for (Template assignedTemplate : this.templates) {
            assignedTemplate.setActive(Objects.nonNull(activeTemplatesMap.remove(assignedTemplate.getId())));
        }
        for (Template unassignedTemplate : ((Map<Integer, Template>) activeTemplatesMap).values()) {
            unassignedTemplate.setActive(true);
            this.templates.add(unassignedTemplate);
        }
    }

    @Override
    public boolean hasProcesses() {
        return CollectionUtils.isNotEmpty(processes);
    }

    @Override
    public void setHasProcesses(boolean hasProcesses) {
        if (!hasProcesses && CollectionUtils.isNotEmpty(processes)) {
            processes.clear();
        } else if (hasProcesses && CollectionUtils.isEmpty(processes)) {
            throw new UnsupportedOperationException("cannot add processes");
        }
    }
}
