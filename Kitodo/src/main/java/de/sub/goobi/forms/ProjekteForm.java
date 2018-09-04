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

package de.sub.goobi.forms;

import de.sub.goobi.helper.Helper;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Locale.LanguageRange;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.enterprise.context.SessionScoped;
import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;
import javax.inject.Named;
import javax.xml.bind.JAXBException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kitodo.config.xml.fileformats.FileFormat;
import org.kitodo.config.xml.fileformats.FileFormatsConfig;
import org.kitodo.data.database.beans.Folder;
import org.kitodo.data.database.beans.Project;
import org.kitodo.data.database.beans.Template;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.data.exceptions.DataException;
import org.kitodo.dto.ProjectDTO;
import org.kitodo.enums.ObjectType;
import org.kitodo.helper.SelectItemList;
import org.kitodo.model.LazyDTOModel;

@Named("ProjekteForm")
@SessionScoped
public class ProjekteForm extends BaseForm {
    private static final long serialVersionUID = 6735912903249358786L;
    private static final Logger logger = LogManager.getLogger(ProjekteForm.class);
    private Project myProjekt;

    /**
     * The folder currently under edit in the pop-up dialog.
     */
    /*
     * This is a hack. The clean solution would be to have an inner class bean
     * for the data table row an dialog, but this approach was introduced
     * decades ago and has been maintained until today.
     */
    private Folder myFolder;
    private Project baseProject;

    // lists accepting the preliminary actions of adding and delting folders
    // it needs the execution of commit folders to make these changes
    // permanent
    private List<Integer> newFolders = new ArrayList<>();
    private List<Integer> deletedFolders = new ArrayList<>();

    private boolean lockedDetail;
    private boolean lockedMets;
    private boolean lockedTechnical;
    private boolean copyTemplates;

    private String projectListPath = MessageFormat.format(REDIRECT_PATH, "projects");
    private String projectEditPath = MessageFormat.format(REDIRECT_PATH, "projectEdit");

    /**
     * Cash for the list of possible MIME types. So that the list does not have
     * to be read from file several times for one page load.
     */
    private Map<String, String> mimeTypes = Collections.emptyMap();

    /**
     * Empty default constructor that also sets the LazyDTOModel instance of
     * this bean.
     */
    public ProjekteForm() {
        super();
        super.setLazyDTOModel(new LazyDTOModel(serviceManager.getProjectService()));
    }

    /**
     * This method deletes folders by their IDs in the list.
     *
     * @param folderIds
     *            IDs of folders to delete
     */
    private void deleteFolders(List<Integer> folderIds) {
        if (Objects.nonNull(this.myProjekt)) {
            for (Integer id : folderIds) {
                for (Folder f : this.myProjekt.getFolders()) {
                    if (f.getId() == null ? id == null : f.getId().equals(id)) {
                        this.myProjekt.getFolders().remove(f);
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
    private void commitFolders() {
        // resetting the list of new folders
        this.newFolders = new ArrayList<>();
        // deleting the folders marked for deletion
        deleteFolders(this.deletedFolders);
        // resetting the list of folders marked for deletion
        this.deletedFolders = new ArrayList<>();
    }

    /**
     * This needs to be executed in order to rollback adding of folders.
     *
     * @return page address
     */
    public String cancel() {
        // flushing new folders
        deleteFolders(this.newFolders);
        // resetting the list of new folders
        this.newFolders = new ArrayList<>();
        // resetting the List of folders marked for deletion
        this.deletedFolders = new ArrayList<>();
        return projectListPath;
    }

    /**
     * Create new project.
     *
     * @return page address
     */
    public String newProject() {
        setLockedDetail(false);
        setLockedMets(false);
        setLockedTechnical(false);
        this.myProjekt = new Project();
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
    public String duplicateProject(Integer itemId) {
        setLockedDetail(false);
        setLockedTechnical(false);
        setLockedMets(false);
        setCopyTemplates(true);
        try {
            this.baseProject = serviceManager.getProjectService().getById(itemId);
            this.myProjekt = serviceManager.getProjectService().duplicateProject(baseProject);
            return projectEditPath;
        } catch (DAOException e) {
            Helper.setErrorMessage("unableToDuplicateProject", logger, e);
            return null;
        }
    }

    /**
     * Saves current project if title is not empty and redirects to projects
     * page.
     *
     * @return page or null
     */
    public String save() {
        serviceManager.getProjectService().evict(this.myProjekt);
        // call this to make saving and deleting permanent
        this.commitFolders();
        if (this.myProjekt.getTitle().equals("") || this.myProjekt.getTitle() == null) {
            Helper.setErrorMessage("errorProjectNoTitleGiven");
            return null;
        } else {
            try {
                if (this.copyTemplates) {
                    for (Template template : this.baseProject.getTemplates()) {
                        template.getProjects().add(this.myProjekt);
                        this.myProjekt.getTemplates().add(template);
                    }
                    setCopyTemplates(false);
                }
                serviceManager.getProjectService().save(this.myProjekt);
                return projectListPath;
            } catch (DataException e) {
                Helper.setErrorMessage(ERROR_SAVING, new Object[] {ObjectType.PROJECT.getTranslationSingular() },
                    logger, e);
                return null;
            }
        }
    }

    /**
     * Saves current project if title is not empty.
     *
     * @return String
     */
    public String apply() {
        // call this to make saving and deleting permanent
        this.commitFolders();
        if (this.myProjekt.getTitle().equals("") || this.myProjekt.getTitle() == null) {
            Helper.setErrorMessage("Can not save project with empty title!");
        } else {
            try {
                serviceManager.getProjectService().save(this.myProjekt);
                Helper.setMessage("Project saved!");
            } catch (DataException e) {
                Helper.setErrorMessage(ERROR_SAVING, new Object[] {ObjectType.PROJECT.getTranslationSingular() },
                    logger, e);
            }
        }
        return null;
    }

    /**
     * Remove.
     */
    public void delete() {
        if (!this.myProjekt.getUsers().isEmpty()) {
            Helper.setErrorMessage("userAssignedError");
        } else {
            try {
                serviceManager.getProjectService().remove(this.myProjekt);
            } catch (DataException e) {
                Helper.setErrorMessage(ERROR_DELETING, new Object[] {ObjectType.PROJECT.getTranslationSingular() },
                    logger, e);
            }
        }
    }

    /**
     * Add folder.
     *
     * @return String
     */
    public String addFolder() {
        this.myFolder = new Folder();
        this.myFolder.setProject(this.myProjekt);
        this.newFolders.add(this.myFolder.getId());
        return this.zurueck;
    }

    /**
     * Save folder.
     */
    public void saveFolder() {
        if (this.myProjekt.getFolders() == null) {
            this.myProjekt.setFolders(new ArrayList<>());
        }
        if (!this.myProjekt.getFolders().contains(this.myFolder)) {
            this.myProjekt.getFolders().add(this.myFolder);
        }
    }

    /**
     * Delete folder.
     *
     * @return page
     */
    public String deleteFolder() {
        // to be deleted folder IDs are listed
        // and deleted after a commit
        this.deletedFolders.add(this.myFolder.getId());
        return null;
    }

    /**
     * Get project.
     *
     * @return Project object
     */
    public Project getMyProjekt() {
        return this.myProjekt;
    }

    /**
     * Set my project.
     *
     * @param inProjekt
     *            Project object
     */
    public void setMyProjekt(Project inProjekt) {
        // has to be called if a page back move was done
        this.cancel();
        this.myProjekt = inProjekt;
    }

    /**
     * Set project by ID.
     *
     * @param projectID
     *            ID of project to set.
     */
    public void setProjectById(int projectID) {
        try {
            setMyProjekt(serviceManager.getProjectService().getById(projectID));
        } catch (DAOException e) {
            Helper.setErrorMessage(ERROR_LOADING_ONE,
                new Object[] {ObjectType.PROJECT.getTranslationSingular(), projectID }, logger, e);
        }
    }

    /**
     * Getter for lockedDetail.
     *
     * @return the lockedDetail
     */
    public boolean isLockedDetail() {
        return lockedDetail;
    }

    /**
     * Setter for lockedDetail.
     *
     * @param lockedDetail
     *            the lockedDetail to set
     */
    public void setLockedDetail(boolean lockedDetail) {
        this.lockedDetail = lockedDetail;
    }

    /**
     * Getter for lockedMets.
     *
     * @return the lockedMets
     */
    public boolean isLockedMets() {
        return lockedMets;
    }

    /**
     * Setter for lockedMets.
     *
     * @param lockedMets
     *            the lockedMets to set
     */
    public void setLockedMets(boolean lockedMets) {
        this.lockedMets = lockedMets;
    }

    /**
     * Getter for lockedTechnicaal.
     *
     * @return the lockedTechnical
     */
    public boolean isLockedTechnical() {
        return lockedTechnical;
    }

    /**
     * Setter for lockedTechnical.
     *
     * @param lockedTechnical
     *            the lockedTechnical to set
     */
    public void setLockedTechnical(boolean lockedTechnical) {
        this.lockedTechnical = lockedTechnical;
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
        List<Folder> filteredFolderList = new ArrayList<>(this.myProjekt.getFolders());

        for (Integer id : this.deletedFolders) {
            for (Folder f : this.myProjekt.getFolders()) {
                if (f.getId() == null ? id == null : f.getId().equals(id)) {
                    filteredFolderList.remove(f);
                    break;
                }
            }
        }
        return filteredFolderList;
    }

    private Map<String, Folder> getFolderMap() {
        return getFolderList().parallelStream().collect(Collectors.toMap(Folder::toString, Function.identity()));
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
                List<LanguageRange> languages = Arrays.asList(new LanguageRange(language.toLanguageTag()));
                mimeTypes = FileFormatsConfig.getFileFormats().parallelStream().collect(Collectors.toMap(
                    λ -> λ.getLabel(languages), FileFormat::getMimeType, (prior, recent) -> recent, TreeMap::new));
            } catch (IOException | JAXBException | RuntimeException e) {
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
        Folder source = myProjekt.getGeneratorSource();
        return source == null ? null : source.toString();
    }

    /**
     * Sets the folder to use as source for generation of derived resources of
     * this project.
     *
     * @param generatorSource
     *            source folder for generation to set
     */
    public void setGeneratorSource(String generatorSource) {
        myProjekt.setGeneratorSource(getFolderMap().get(generatorSource));
    }

    /**
     * Returns the folder to use for the media view.
     *
     * @return media view folder
     */
    public String getMediaView() {
        Folder mediaView = myProjekt.getMediaView();
        return mediaView == null ? null : mediaView.toString();
    }

    /**
     * Sets the folder to use for the media view.
     *
     * @param mediaView
     *            media view folder
     */
    public void setMediaView(String mediaView) {
        myProjekt.setMediaView(getFolderMap().get(mediaView));
    }

    /**
     * Returns the folder to use for preview.
     *
     * @return preview folder
     */
    public String getPreview() {
        Folder preview = myProjekt.getPreview();
        return preview == null ? null : preview.toString();
    }

    /**
     * Sets the folder to use for preview.
     *
     * @param preview
     *            preview folder
     */
    public void setPreview(String preview) {
        myProjekt.setPreview(getFolderMap().get(preview));
    }

    /**
     * Method being used as viewAction for project edit form.
     *
     * @param id
     *            ID of the ruleset to load
     */
    public void loadProject(int id) {
        try {
            if (!Objects.equals(id, 0)) {
                setMyProjekt(this.serviceManager.getProjectService().getById(id));
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
    public List<ProjectDTO> getProjects() {
        try {
            return serviceManager.getProjectService().findAll();
        } catch (DataException e) {
            Helper.setErrorMessage(ERROR_LOADING_MANY, new Object[] {ObjectType.PROJECT.getTranslationPlural() },
                logger, e);
            return new LinkedList<>();
        }
    }

    /**
     * Gets all available clients.
     *
     * @return The list of clients.
     */
    public List<SelectItem> getClients() {
        return SelectItemList.getClients();
    }
}
