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

package org.kitodo.production.forms.user;

import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import jakarta.annotation.PostConstruct;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kitodo.data.database.beans.Project;
import org.kitodo.data.database.beans.User;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.production.enums.ObjectType;
import org.kitodo.production.forms.BaseForm;
import org.kitodo.production.helper.Helper;
import org.kitodo.production.services.ServiceManager;
import org.primefaces.model.SortMeta;
import org.primefaces.model.SortOrder;

@Named("UserEditViewProjectsTab")
@ViewScoped
public class UserEditViewProjectsTab extends BaseForm {

    /**
     * The user object that is being edited (variable "user" references to the user currently logged in, see BaseForm).
     */
    private User userObject;

    private List<Project> projects;

    private static final Logger logger = LogManager.getLogger(UserEditViewProjectsTab.class);

    /**
     * Initialize UserEditViewProjectsTab.
     */
    @PostConstruct
    public void init() {
        sortBy = SortMeta.builder().field("title").order(SortOrder.ASCENDING).build();
    }

    /**
     * Return user object currently being edited.
     * 
     * @return the user currently being edited
     */
    public User getUserObject() {
        return this.userObject;
    }

    /**
     * Return list of projects available for assignment to the user.
     *
     * @return list of projects available for assignment to the user
     */
    public List<Project> getProjects() {
        return projects;
    }
   
    /**
     * Method that is called from viewAction of user edit form.
     *
     * @param userObject
     *            the user currently being edited
     */
    public void load(User userObject) {
        this.userObject = userObject;        

        try {
            this.projects = ServiceManager.getProjectService().findAllAvailableForAssignToUser(this.userObject)
                    .stream().sorted(Comparator.comparing(Project::getTitle)).collect(Collectors.toList());
        } catch (DAOException e) {
            Helper.setErrorMessage(ERROR_LOADING_MANY, new Object[] {ObjectType.PROJECT.getTranslationPlural() },
                logger, e);
            this.projects = new LinkedList<>();
        }
    }

    /**
     * Save user projects tab.
     *
     * @return true if information can be saved and was updated on user object
     */
    public boolean save() {
        return true;
    }

    /**
     * Remove user from project.
     *
     * @return null (to stay on the same page)
     */
    public String deleteFromProject() {
        String idParameter = Helper.getRequestParameter("ID");
        if (Objects.nonNull(idParameter)) {
            try {
                int projectId = Integer.parseInt(idParameter);
                for (Project project : this.userObject.getProjects()) {
                    if (project.getId().equals(projectId)) {
                        this.userObject.getProjects().remove(project);
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
     * Add user to project.
     *
     * @return null (to stay on the same page)
     */
    public String addToProject() {
        String idParameter = Helper.getRequestParameter("ID");
        if (Objects.nonNull(idParameter)) {
            int projectId = 0;
            try {
                projectId = Integer.parseInt(idParameter);
                Project project = ServiceManager.getProjectService().getById(projectId);

                if (!this.userObject.getProjects().contains(project)) {
                    this.userObject.getProjects().add(project);
                }
            } catch (DAOException e) {
                Helper.setErrorMessage(ERROR_DATABASE_READING,
                        new Object[] {ObjectType.PROJECT.getTranslationSingular(), projectId }, logger, e);
            } catch (NumberFormatException e) {
                Helper.setErrorMessage(e.getLocalizedMessage(), logger, e);
            }
        } else {
            Helper.setErrorMessage(ERROR_PARAMETER_MISSING, new Object[] {ID_PARAMETER});
        }
        return this.stayOnCurrentPage;
    }

}
