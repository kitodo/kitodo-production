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

package org.kitodo.production.forms;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Locale.LanguageRange;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.enterprise.context.SessionScoped;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;
import javax.inject.Named;
import javax.xml.bind.JAXBException;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kitodo.config.xml.fileformats.FileFormat;
import org.kitodo.config.xml.fileformats.FileFormatsConfig;
import org.kitodo.data.database.beans.Folder;
import org.kitodo.data.database.beans.ImportConfiguration;
import org.kitodo.data.database.beans.LtpValidationConfiguration;
import org.kitodo.data.database.beans.Project;
import org.kitodo.data.database.beans.Template;
import org.kitodo.data.database.beans.User;
import org.kitodo.data.database.enums.PreviewHoverMode;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.exceptions.ProjectDeletionException;
import org.kitodo.forms.FolderGenerator;
import org.kitodo.production.controller.SecurityAccessController;
import org.kitodo.production.enums.ObjectType;
import org.kitodo.production.helper.Helper;
import org.kitodo.production.model.LazyBeanModel;
import org.kitodo.production.services.ServiceManager;
import org.kitodo.production.services.data.ProjectService;

@Named("ProjectForm")
@SessionScoped
public class ProjectForm extends BaseForm {
    public static final String MIMETYPE_PREFIX_AUDIO = "audio";
    private static final Logger logger = LogManager.getLogger(ProjectForm.class);
    public static final String MIMETYPE_PREFIX_VIDEO = "video";
    private Project project;
    private List<Template> deletedTemplates = new ArrayList<>();
    private boolean locked = true;
    private static final String TITLE_USED = "projectTitleAlreadyInUse";
    private Boolean hasProcesses;

    /**
     * An encapsulation of the content generator properties of the folder in a
     * way suitable to the JSF design.
     */
    private FolderGenerator generator = new FolderGenerator(this.myFolder);

    /**
     * Initialize the list of displayed list columns.
     */
    @PostConstruct
    public void init() {
        // Lists of available list columns
        columns = new ArrayList<>();
        try {
            columns.add(ServiceManager.getListColumnService().getListColumnsForListAsSelectItemGroup("project"));
            columns.add(ServiceManager.getListColumnService().getListColumnsForListAsSelectItemGroup("template"));
            columns.add(ServiceManager.getListColumnService().getListColumnsForListAsSelectItemGroup("workflow"));
            columns.add(ServiceManager.getListColumnService().getListColumnsForListAsSelectItemGroup("docket"));
            columns.add(ServiceManager.getListColumnService().getListColumnsForListAsSelectItemGroup("ruleset"));
        } catch (DAOException e) {
            Helper.setErrorMessage(e.getLocalizedMessage(), logger, e);
        }

        // Lists of selected list columns
        selectedColumns = new ArrayList<>();
        selectedColumns.addAll(ServiceManager.getListColumnService().getSelectedListColumnsForListAndClient("project"));
        selectedColumns
                .addAll(ServiceManager.getListColumnService().getSelectedListColumnsForListAndClient("template"));
        selectedColumns
                .addAll(ServiceManager.getListColumnService().getSelectedListColumnsForListAndClient("workflow"));
        selectedColumns.addAll(ServiceManager.getListColumnService().getSelectedListColumnsForListAndClient("docket"));
        selectedColumns.addAll(ServiceManager.getListColumnService().getSelectedListColumnsForListAndClient("ruleset"));
    }

    /**
     * The folder currently under edit in the pop-up dialog.
     */
    /*
     * This is a hack. The clean solution would be to have an inner class bean
     * for the data table row a dialog, but this approach was introduced
     * decades ago and has been maintained until today.
     */
    private Folder myFolder;
    private Project baseProject;

    // lists accepting the preliminary actions of adding and deleting folders
    // it needs the execution of commit folders to make these changes
    // permanent
    private List<Integer> newFolders = new ArrayList<>();
    private List<Integer> deletedFolders = new ArrayList<>();

    private boolean copyTemplates;

    private final String projectEditPath = MessageFormat.format(REDIRECT_PATH, "projectEdit");

    private String projectEditReferer = DEFAULT_LINK;

    /**
     * Cash for the list of possible MIME types. So that the list does not have
     * to be read from file several times for one page load.
     */
    private Map<String, String> mimeTypes = Collections.emptyMap();

    /**
     * Empty default constructor that also sets the LazyBeanModel instance of
     * this bean.
     */
    public ProjectForm() {
        super();
        super.setLazyBeanModel(new LazyBeanModel(ServiceManager.getProjectService()));
    }

    /**
     * This method deletes folders by their IDs in the list.
     *
     * @param folderIds
     *            IDs of folders to delete
     */
    private void removeFoldersFromProject(List<Integer> folderIds) {
        if (Objects.nonNull(project)) {
            for (Integer id : folderIds) {
                for (Folder f : project.getFolders()) {
                    if (Objects.isNull(f.getId()) ? Objects.isNull(id) : f.getId().equals(id)) {
                        project.getFolders().remove(f);
                        break;
                    }
                }
            }
        }
    }

    /**
     * this method flushes the newFolders list, thus makes them permanent and
     * deletes those marked for deleting, making the removal permanent.
     */
    private void commitFolders() throws DAOException {
        // resetting the list of new folders
        newFolders = new ArrayList<>();
        // deleting the folders marked for deletion
        removeFoldersFromProject(deletedFolders);
        // resetting the list of folders marked for deletion
        deletedFolders = new ArrayList<>();
    }

    /**
     * This needs to be executed in order to rollback adding of folders.
     */
    public void cancel() {
        // flushing new folders
        removeFoldersFromProject(newFolders);
        // resetting the list of new folders
        newFolders = new ArrayList<>();
        // resetting the List of folders marked for deletion
        deletedFolders = new ArrayList<>();
    }

    /**
     * Create new project.
     *
     * @return page address
     */
    public String newProject() {
        this.project = new Project();
        this.locked = false;
        this.project.setClient(ServiceManager.getUserService().getSessionClientOfAuthenticatedUser());
        return projectEditPath;
    }

    /**
     * Duplicate the selected project.
     *
     * @param itemId
     *            ID of the project to duplicate
     * @return page address; either redirect to the edit project page or return
     *         'null' if the project could not be retrieved, which will prompt
     *         JSF to remain on the same page and reuse the bean.
     */
    public String duplicate(Integer itemId) {
        setCopyTemplates(true);
        this.locked = false;
        try {
            this.baseProject = ServiceManager.getProjectService().getById(itemId);
            this.project = ServiceManager.getProjectService().duplicateProject(baseProject);
            this.setSaveDisabled(false);
            return projectEditPath + "&referer=projects";
        } catch (DAOException e) {
            Helper.setErrorMessage(ERROR_DUPLICATE, new Object[] {ObjectType.PROJECT.getTranslationSingular() }, logger,
                e);
            return this.stayOnCurrentPage;
        }
    }

    /**
     * Saves current project if title is not empty and redirects to projects
     * page.
     *
     * @return page or null
     */
    public String save() {
        ServiceManager.getProjectService().evict(project);
        if (isTitleValid()) {
            try {
                addFirstUserToNewProject();

                commitTemplates();
                commitFolders();

                ServiceManager.getProjectService().save(project);

                return projectsPage;
            } catch (DAOException e) {
                Helper.setErrorMessage(ERROR_SAVING, new Object[] {ObjectType.PROJECT.getTranslationSingular() },
                    logger, e);
                return this.stayOnCurrentPage;
            }
        } else {
            return this.stayOnCurrentPage;
        }
    }

    private void commitTemplates() throws DAOException {
        if (copyTemplates) {
            for (Template template : baseProject.getTemplates()) {
                template.getProjects().add(project);
                project.getTemplates().add(template);
            }
            setCopyTemplates(false);
        }

        for (Template template : project.getTemplates()) {
            ServiceManager.getTemplateService().save(template);
        }
        for (Template template : deletedTemplates) {
            ServiceManager.getTemplateService().save(template);
        }

        deletedTemplates = new ArrayList<>();
    }

    private boolean isTitleValid() {
        String projectTitle = this.project.getTitle();
        if (StringUtils.isNotBlank(projectTitle)) {
            List<Project> projects = ServiceManager.getProjectService().getProjectsWithTitleAndClient(projectTitle,
                    this.project.getClient().getId());
            int count = projects.size();
            if (count > 1) {
                Helper.setErrorMessage(ERROR_OCCURRED, TITLE_USED);
                return false;
            } else if (count == 1) {
                Integer projectId = this.project.getId();
                if (Objects.nonNull(projectId) && projects.get(0).getId().equals(projectId)) {
                    return true;
                }
                Helper.setErrorMessage(ERROR_OCCURRED, TITLE_USED);
                return false;
            }
            return true;
        }
        Helper.setErrorMessage(ERROR_INCOMPLETE_DATA, "errorProjectNoTitleGiven");
        return false;
    }

    private void addFirstUserToNewProject() throws DAOException {
        if (this.project.getUsers().isEmpty()) {
            User user = ServiceManager.getUserService().getCurrentUser();
            user.getProjects().add(this.project);
            this.project.getUsers().add(user);
            ServiceManager.getProjectService().save(this.project);
            ServiceManager.getUserService().save(user);
        }
    }

    /**
     * Remove.
     */
    public void delete(int projectId) {
        try {
            ProjectService.delete(projectId);
        } catch (DAOException e) {
            Helper.setErrorMessage(ERROR_DELETING, new Object[] {ObjectType.PROJECT.getTranslationSingular() }, logger,
                e);
        } catch (ProjectDeletionException e) {
            Helper.setErrorMessage(e.getMessage());
        }
    }

    /**
     * Add folder.
     *
     * @return String
     */
    public String addFolder() {
        this.myFolder = new Folder();
        this.myFolder.setProject(this.project);
        this.generator = new FolderGenerator(myFolder);
        this.newFolders.add(this.myFolder.getId());
        return this.stayOnCurrentPage;
    }

    /**
     * Save folder.
     */
    public void saveFolder() {
        if (!this.project.getFolders().contains(this.myFolder)) {
            this.project.getFolders().add(this.myFolder);
            try {
                ServiceManager.getProjectService().save(this.project);
            } catch (DAOException e) {
                Helper.setErrorMessage(ERROR_SAVING, new Object[] {ObjectType.PROJECT.getTranslationSingular() },
                        logger, e);
            }
        } else {
            List<Folder> folders = this.project.getFolders();
            for (Folder folder : folders) {
                if (this.myFolder.getFileGroup().equals(folder.getFileGroup()) && folder != myFolder) {
                    Helper.setErrorMessage("errorDuplicateFilegroup",
                        new Object[] {ObjectType.FOLDER.getTranslationPlural() });
                }
            }
        }
    }

    /**
     * Delete folder.
     *
     * @return page
     */
    public String deleteFolder() {
        if (Objects.isNull(myFolder.getId())) {
            project.getFolders().remove(myFolder);
        } else {
            deletedFolders.add(this.myFolder.getId());
        }
        return this.stayOnCurrentPage;
    }

    /**
     * Return list of templates assignable to this project. Templates are
     * assignable when they are not assigned already to this project and they
     * belong to the same client as the project and user which edits this
     * project.
     *
     * @return list of assignable templates
     */
    public List<Template> getTemplates() {
        try {
            return ServiceManager.getTemplateService().findAllAvailableForAssignToProject(this.project.getId());
        } catch (DAOException e) {
            Helper.setErrorMessage(ERROR_LOADING_MANY, new Object[] {ObjectType.TEMPLATE.getTranslationPlural() },
                logger, e);
            return new LinkedList<>();
        }
    }

    /**
     * Get import configurations.
     *
     * @return import configurations
     */
    public List<ImportConfiguration> getImportConfigurations() {
        try {
            return ServiceManager.getImportConfigurationService().getAll();
        } catch (DAOException e) {
            Helper.setErrorMessage(e);
            return Collections.emptyList();
        }
    }

    /**
     * Add template to project.
     *
     * @return stay on the same page
     */
    public String addTemplate() {
        int templateId = 0;
        String templateIdString = Helper.getRequestParameter(ID_PARAMETER);
        if (Objects.nonNull(templateIdString)) {
            try {
                templateId = Integer.parseInt(templateIdString);
                Template template = ServiceManager.getTemplateService().getById(templateId);
                if (!this.project.getTemplates().contains(template)) {
                    this.project.getTemplates().add(template);
                    template.getProjects().add(this.project);
                }
            } catch (DAOException e) {
                Helper.setErrorMessage(ERROR_DATABASE_READING,
                        new Object[] {ObjectType.TEMPLATE.getTranslationSingular(), templateId }, logger, e);
            } catch (NumberFormatException e) {
                Helper.setErrorMessage(e.getLocalizedMessage(), logger, e);
            }
        } else {
            Helper.setErrorMessage(ERROR_PARAMETER_MISSING, new Object[] {ID_PARAMETER});
        }
        return this.stayOnCurrentPage;
    }

    /**
     * Remove template from project.
     *
     * @return stay on the same page
     */
    public String deleteTemplate() {
        String templateIdString = Helper.getRequestParameter(ID_PARAMETER);
        if (Objects.nonNull(templateIdString)) {
            try {
                int templateId = Integer.parseInt(templateIdString);
                for (Template template : this.project.getTemplates()) {
                    if (template.getId().equals(templateId)) {
                        this.project.getTemplates().remove(template);
                        template.getProjects().remove(this.project);
                        this.deletedTemplates.add(template);
                        break;
                    }
                }
            } catch (NumberFormatException e) {
                Helper.setErrorMessage(e.getLocalizedMessage(), logger, e);
            }
        } else {
            Helper.setErrorMessage(ERROR_PARAMETER_MISSING, new Object[] {ID_PARAMETER});
        }
        return this.stayOnCurrentPage;
    }

    /**
     * Switch the lock status of the form.
     */
    public void switchLock() {
        locked = !locked;
    }

    /**
     * Gets the locked status of the form.
     *
     * @return te value of locked
     */
    public boolean isLocked() {
        return locked;
    }

    /**
     * Get project.
     *
     * @return Project object
     */
    public Project getProject() {
        return this.project;
    }

    /**
     * Set my project.
     *
     * @param project
     *            Project object
     */
    public void setProject(Project project) {
        // has to be called if a page back move was done
        cancel();
        this.project = project;
        hasProcesses = !project.getProcesses().isEmpty();
    }

    /**
     * Set copy templates.
     *
     * @param copyTemplates
     *            as boolean
     */
    public void setCopyTemplates(boolean copyTemplates) {
        this.copyTemplates = copyTemplates;
    }

    /**
     * Get copy templates.
     *
     * @return value of copy templates
     */
    public boolean isCopyTemplates() {
        return copyTemplates;
    }

    /**
     * The need to commit deleted folders only after the save action requires a
     * filter, so that those folders marked for delete are not shown anymore.
     *
     * @return modified ArrayList
     */
    public List<Folder> getFolderList() {
        List<Folder> filteredFolderList = new ArrayList<>(this.project.getFolders());

        for (Integer id : this.deletedFolders) {
            for (Folder f : this.project.getFolders()) {
                if (Objects.isNull(f.getId()) ? Objects.isNull(id) : f.getId().equals(id)) {
                    filteredFolderList.remove(f);
                    break;
                }
            }
        }
        return filteredFolderList;
    }

    /**
     * The need to commit deleted folders only after the save action requires a
     * filter, so that those folders marked for delete are not shown anymore.
     *
     * @return modified ArrayList
     */
    public List<SelectItem> getSelectableFolders() {
        return getFolderList().stream().map(folder -> new SelectItem(folder.getFileGroup(), folder.toString()))
                .collect(Collectors.toList());
    }

    /**
     * Checks if folder list contains audio folder.
     *
     * @return true if folder list contains audio folder
     */
    public boolean hasAudioFolder() {
        return getFolderList().stream().anyMatch(folder -> folder.getMimeType().startsWith(MIMETYPE_PREFIX_AUDIO));
    }

    /**
     * Checks if folder list contains video folder.
     *
     * @return true if folder list contains video folder
     */
    public boolean hasVideoFolder() {
        return getFolderList().stream().anyMatch(folder -> folder.getMimeType().startsWith(MIMETYPE_PREFIX_VIDEO));
    }

    private Map<String, Folder> getFolderMap() {
        return getFolderList().parallelStream().collect(Collectors.toMap(Folder::getFileGroup, Function.identity()));
    }

    /**
     * Returns the folder currently under edit in the pop-up dialog.
     *
     * @return the folder currently under edit
     */
    public Folder getMyFolder() {
        return this.myFolder;
    }

    /**
     * Sets the folder currently under edit in the pop-up dialog.
     *
     * @param myFolder
     *            folder to set to be under edit now
     */
    public void setMyFolder(Folder myFolder) {
        this.myFolder = myFolder;
        this.generator = new FolderGenerator(myFolder);
    }

    /**
     * Returns an encapsulation to access the generator properties of the folder
     * in a JSF-friendly way.
     *
     * @return the generator controller
     */
    public FolderGenerator getGenerator() {
        return generator;
    }

    /**
     * Returns the list of possible MIME types to display them in the drop-down
     * select.
     *
     * @return possible MIME types
     */
    public Map<String, String> getMimeTypes() {
        if (mimeTypes.isEmpty()) {
            try {
                Locale language = FacesContext.getCurrentInstance().getViewRoot().getLocale();
                List<LanguageRange> languages = Collections.singletonList(new LanguageRange(language.toLanguageTag()));
                mimeTypes = FileFormatsConfig.getFileFormats().parallelStream()
                        .collect(Collectors.toMap(locale -> locale.getLabel(languages), FileFormat::getMimeType,
                            (prior, recent) -> recent, TreeMap::new));
            } catch (JAXBException | RuntimeException e) {
                Helper.setErrorMessage(ERROR_READING, new Object[] {e.getMessage() }, logger, e);
            }
        }
        return mimeTypes;
    }

    /**
     * Returns the folder to use as source for generation of derived resources
     * of this project.
     *
     * @return the source folder for generation
     */
    public String getGeneratorSource() {
        Folder source = project.getGeneratorSource();
        return Objects.isNull(source) ? null : source.getFileGroup();
    }

    /**
     * Sets the folder to use as source for generation of derived resources of
     * this project.
     *
     * @param generatorSource
     *            source folder for generation to set
     */
    public void setGeneratorSource(String generatorSource) {
        project.setGeneratorSource(getFolderMap().get(generatorSource));
    }

    /**
     * Returns the folder to use for the media view.
     *
     * @return media view folder
     */
    public String getMediaView() {
        Folder mediaView = project.getMediaView();
        return Objects.isNull(mediaView) ? null : mediaView.getFileGroup();
    }

    /**
     * Sets the folder to use for the media view.
     *
     * @param mediaView
     *         media view folder
     */
    public void setMediaView(String mediaView) {
        project.setMediaView(getFolderMap().get(mediaView));
    }

    /**
     * Returns the folder to use for the audio media view.
     *
     * @return audio media view folder
     */
    public String getAudioMediaView() {
        Folder audioMediaView = project.getAudioMediaView();
        return Objects.isNull(audioMediaView) ? null : audioMediaView.getFileGroup();
    }

    /**
     * Sets the folder to use for the media view.
     *
     * @param audioMediaView
     *         audio media view folder
     */
    public void setAudioMediaView(String audioMediaView) {
        project.setAudioMediaView(getFolderMap().get(audioMediaView));
    }

    /**
     * Returns the state of the audio media view waveform.
     *
     * @return True if enabled
     */
    public boolean isAudioMediaViewWaveform() {
        return project.isAudioMediaViewWaveform();
    }

    /**
     * Sets the state of the audio media view waveform.
     *
     * @param audioMediaViewWaveform True if enabled
     *
     */
    public void setAudioMediaViewWaveform(boolean audioMediaViewWaveform) {
        project.setAudioMediaViewWaveform(audioMediaViewWaveform);
    }

    /**
     * Returns the folder to use for the video media view.
     *
     * @return video media view folder
     */
    public String getVideoMediaView() {
        Folder videoMediaView = project.getVideoMediaView();
        return Objects.isNull(videoMediaView) ? null : videoMediaView.getFileGroup();
    }

    /**
     * Sets the folder to use for the media view.
     *
     * @param videoMediaView
     *         video media view folder
     */
    public void setVideoMediaView(String videoMediaView) {
        project.setVideoMediaView(getFolderMap().get(videoMediaView));
    }

    /**
     * Returns the folder to use for preview.
     *
     * @return preview folder
     */
    public String getPreview() {
        Folder preview = project.getPreview();
        return Objects.isNull(preview) ? null : preview.getFileGroup();
    }

    /**
     * Sets the folder to use for preview.
     *
     * @param preview
     *         preview folder
     */
    public void setPreview(String preview) {
        project.setPreview(getFolderMap().get(preview));
    }

    /**
     * Returns the preview hover mode.
     *
     * @return The preview hover mode
     */
    public PreviewHoverMode getPreviewHoverMode() {
        return project.getPreviewHoverMode();
    }

    /**
     * Sets the preview hover mode.
     *
     * @param previewHoverMode preview hover mode
     */
    public void setPreviewHoverMode(PreviewHoverMode previewHoverMode) {
        project.setPreviewHoverMode(previewHoverMode);
    }

    /**
     * Returns the folder to use for audio preview.
     *
     * @return audio preview folder
     */
    public String getAudioPreview() {
        Folder audioPreview = project.getAudioPreview();
        return Objects.isNull(audioPreview) ? null : audioPreview.getFileGroup();
    }

    /**
     * Sets the folder to use for audio preview.
     *
     * @param audioPreview
     *         audio preview folder
     */
    public void setAudioPreview(String audioPreview) {
        project.setAudioPreview(getFolderMap().get(audioPreview));
    }

    /**
     * Returns the folder to use for video preview.
     *
     * @return video preview folder
     */
    public String getVideoPreview() {
        Folder videoPreview = project.getVideoPreview();
        return Objects.isNull(videoPreview) ? null : videoPreview.getFileGroup();
    }

    /**
     * Sets the folder to use for video preview.
     *
     * @param videoPreview
     *         video preview folder
     */
    public void setVideoPreview(String videoPreview) {
        project.setVideoPreview(getFolderMap().get(videoPreview));
    }

    /**
     * Method being used as viewAction for project edit form.
     *
     * @param id
     *         ID of the ruleset to load
     */
    public void loadProject(int id) {
        SecurityAccessController securityAccessController = new SecurityAccessController();
        try {
            if (!securityAccessController.hasAuthorityToEditProject(id)) {
                ExternalContext context = FacesContext.getCurrentInstance().getExternalContext();
                context.redirect(DEFAULT_LINK);
            }
        } catch (IOException e) {
            Helper.setErrorMessage(ERROR_LOADING_ONE, new Object[] {ObjectType.PROJECT.getTranslationSingular(), id },
                    logger, e);
        }
        try {
            if (!Objects.equals(id, 0)) {
                setProject(ServiceManager.getProjectService().getById(id));
                this.locked = true;
            }
            setSaveDisabled(true);
        } catch (DAOException e) {
            Helper.setErrorMessage(ERROR_LOADING_ONE, new Object[] {ObjectType.PROJECT.getTranslationSingular(), id },
                logger, e);
        }

    }

    /**
     * Return list of projects.
     *
     * @return list of projects
     */
    public List<Project> getProjects() {
        try {
            return ServiceManager.getProjectService().getAll();
        } catch (DAOException e) {
            Helper.setErrorMessage(ERROR_LOADING_MANY, new Object[] {ObjectType.PROJECT.getTranslationPlural() },
                logger, e);
            return new LinkedList<>();
        }
    }

    /**
     * Set referring view which will be returned when the user clicks "save" or
     * "cancel" on the project edit page.
     *
     * @param referer
     *            the referring view
     */
    public void setProjectEditReferer(String referer) {
        if (!referer.isEmpty()) {
            if ("projects".equals(referer)) {
                this.projectEditReferer = referer;
            } else {
                this.projectEditReferer = DEFAULT_LINK;
            }
        }
    }

    /**
     * Get project edit page referring view.
     *
     * @return project edit page referring view
     */
    public String getProjectEditReferer() {
        return this.projectEditReferer;
    }

    /**
     * Return whether project has processes or not.
     *
     * @return whether project has processes or not
     */
    public Boolean hasProcesses() {
        return hasProcesses;
    }

    /**
     * Return the list of validation configurations that can be assigned to a folder based
     * on the folders mimeType.
     * 
     * @return the list of possible validation configurations that can be assigned to a folder
     */
    public List<LtpValidationConfiguration> getPossibleLtpValidationConfigurations() {
        if (Objects.isNull(myFolder)) {
            return Collections.emptyList();
        }
        return ServiceManager.getLtpValidationConfigurationService().listByMimeType(myFolder.getMimeType());
    }
}
