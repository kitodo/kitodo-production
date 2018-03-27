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

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

import javax.enterprise.context.SessionScoped;
import javax.faces.context.FacesContext;
import javax.inject.Named;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Session;
import org.kitodo.data.database.beans.Client;
import org.kitodo.data.database.beans.Project;
import org.kitodo.data.database.beans.ProjectFileGroup;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.data.exceptions.DataException;
import org.kitodo.dto.ProjectDTO;
import org.kitodo.model.LazyDTOModel;
import org.kitodo.services.ServiceManager;

@Named("ProjekteForm")
@SessionScoped
public class ProjekteForm extends BasisForm {
    private static final long serialVersionUID = 6735912903249358786L;
    private static final Logger logger = LogManager.getLogger(ProjekteForm.class);
    private Project myProjekt = new Project();
    private ProjectFileGroup myFilegroup;
    private transient ServiceManager serviceManager = new ServiceManager();

    // lists accepting the preliminary actions of adding and delting filegroups
    // it needs the execution of commit fileGroups to make these changes
    // permanent
    private List<Integer> newFileGroups = new ArrayList<>();
    private List<Integer> deletedFileGroups = new ArrayList<>();

    private boolean lockedDetail;
    private boolean lockedMets;
    private boolean lockedTechnical;

    private String projectListPath = MessageFormat.format(REDIRECT_PATH, "projects");
    private String projectEditPath = MessageFormat.format(REDIRECT_PATH, "projectEdit");

    /**
     * Empty default constructor that also sets the LazyDTOModel instance of this
     * bean.
     */
    public ProjekteForm() {
        super();
        super.setLazyDTOModel(new LazyDTOModel(serviceManager.getProjectService()));
    }

    // making sure its cleaned up
    @Override
    protected void finalize() {
        this.cancel();
    }

    /**
     * this method deletes filegroups by their id's in the list.
     *
     * @param fileGroups
     *            List
     */
    private void deleteFileGroups(List<Integer> fileGroups) {
        for (Integer id : fileGroups) {
            for (ProjectFileGroup f : this.myProjekt.getProjectFileGroups()) {
                if (f.getId() == null ? id == null : f.getId().equals(id)) {
                    this.myProjekt.getProjectFileGroups().remove(f);
                    break;
                }
            }
        }
    }

    /**
     * this method flushes the newFileGroups List, thus makes them permanent and
     * deletes those marked for deleting, making the removal permanent.
     */
    private void commitFileGroups() {
        // resetting the List of new fileGroups
        this.newFileGroups = new ArrayList<>();
        // deleting the fileGroups marked for deletion
        deleteFileGroups(this.deletedFileGroups);
        // resetting the List of fileGroups marked for deletion
        this.deletedFileGroups = new ArrayList<>();
    }

    /**
     * this needs to be executed in order to rollback adding of filegroups.
     */
    public String cancel() {
        // flushing new fileGroups
        deleteFileGroups(this.newFileGroups);
        // resetting the List of new fileGroups
        this.newFileGroups = new ArrayList<>();
        // resetting the List of fileGroups marked for deletion
        this.deletedFileGroups = new ArrayList<>();
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
     *         'null' if the project could not be retrieved, which will prompt JSF
     *         to remain on the same page and reuse the bean.
     */
    public String duplicateProject(Integer itemId) {
        setLockedDetail(false);
        setLockedTechnical(false);
        setLockedMets(false);
        try {
            this.myProjekt = serviceManager.getProjectService().duplicateProject(itemId);
            return projectEditPath;
        } catch (DAOException e) {
            Helper.setErrorMessage("unableToDuplicateProject", logger, e);
            return null;
        }
    }

    /**
     * Saves current project if title is not empty and redirects to projects page.
     *
     * @return page or empty String
     */
    public String save() {
        Session session = Helper.getHibernateSession();
        session.evict(this.myProjekt);
        // call this to make saving and deleting permanent
        this.commitFileGroups();
        if (this.myProjekt.getTitle().equals("") || this.myProjekt.getTitle() == null) {
            Helper.setFehlerMeldung("errorProjectNoTitleGiven");
            return null;
        } else {
            try {
                serviceManager.getProjectService().save(this.myProjekt);
                return projectListPath;
            } catch (DataException e) {
                Helper.setErrorMessage("errorSaving", new Object[] {Helper.getTranslation("projekt") }, logger, e);
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
        this.commitFileGroups();
        if (this.myProjekt.getTitle().equals("") || this.myProjekt.getTitle() == null) {
            Helper.setFehlerMeldung("Can not save project with empty title!");
            return null;
        } else {
            try {
                serviceManager.getProjectService().save(this.myProjekt);
                Helper.setMeldung("Project saved!");
                return null;
            } catch (DataException e) {
                Helper.setErrorMessage("errorSaving", new Object[] {Helper.getTranslation("projekt") }, logger, e);
                return null;
            }
        }
    }

    /**
     * Remove.
     *
     * @return String
     */
    public String delete() {
        if (this.myProjekt.getUsers().size() > 0) {
            Helper.setFehlerMeldung("userAssignedError");
            return null;
        } else {
            try {
                serviceManager.getProjectService().remove(this.myProjekt);
            } catch (DataException e) {
                Helper.setErrorMessage("errorDeleting", new Object[] {Helper.getTranslation("project") }, logger, e);
                return null;
            }
        }
        return projectListPath;
    }

    /**
     * Add file group.
     *
     * @return String
     */
    public String filegroupAdd() {
        this.myFilegroup = new ProjectFileGroup();
        this.myFilegroup.setProject(this.myProjekt);
        this.newFileGroups.add(this.myFilegroup.getId());
        return this.zurueck;
    }

    /**
     * Save file group.
     */
    public void filegroupSave() {
        if (this.myProjekt.getProjectFileGroups() == null) {
            this.myProjekt.setProjectFileGroups(new ArrayList<>());
        }
        if (!this.myProjekt.getProjectFileGroups().contains(this.myFilegroup)) {
            this.myProjekt.getProjectFileGroups().add(this.myFilegroup);
        }
    }

    public String filegroupEdit() {
        return this.zurueck;
    }

    /**
     * Delete file group.
     *
     * @return page
     */
    public String filegroupDelete() {
        // to be deleted fileGroups ids are listed
        // and deleted after a commit
        this.deletedFileGroups.add(this.myFilegroup.getId());
        return null;
    }

    /*
     * Getter und Setter
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
     * The need to commit deleted fileGroups only after the save action requires a
     * filter, so that those filegroups marked for delete are not shown anymore.
     *
     * @return modified ArrayList
     */
    public ArrayList<ProjectFileGroup> getFileGroupList() {
        ArrayList<ProjectFileGroup> filteredFileGroupList = new ArrayList<>(this.myProjekt.getProjectFileGroups());

        for (Integer id : this.deletedFileGroups) {
            for (ProjectFileGroup f : this.myProjekt.getProjectFileGroups()) {
                if (f.getId() == null ? id == null : f.getId().equals(id)) {
                    filteredFileGroupList.remove(f);
                    break;
                }
            }
        }
        return filteredFileGroupList;
    }

    public ProjectFileGroup getMyFilegroup() {
        return this.myFilegroup;
    }

    public void setMyFilegroup(ProjectFileGroup myFilegroup) {
        this.myFilegroup = myFilegroup;
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
            Helper.setErrorMessage("errorLoadingOne", new Object[] {Helper.getTranslation("projekt"), id }, logger, e);
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
            Helper.setErrorMessage("errorLoadingMany", new Object[] {Helper.getTranslation("projekte") }, logger, e);
            return new LinkedList<>();
        }
    }

    /**
     * Return the template titles of the project with the given ID "id".
     *
     * @param id
     *            ID of the project for which the template titles are returned.
     * @return String containing the templates titles of the project with the given
     *         ID
     */
    public String getProjectTemplateTitles(int id) {
        try {
            return serviceManager.getProjectService().getProjectTemplatesTitlesAsString(id);
        } catch (DAOException e) {
            Helper.setErrorMessage("unableToRetrieveTemplates", logger, e);
            return null;
        }
    }

    /**
     * Gets all available clients.
     *
     * @return The list of clients.
     */
    public List<Client> getClients() {
        return serviceManager.getClientService().getAll();
    }
}
