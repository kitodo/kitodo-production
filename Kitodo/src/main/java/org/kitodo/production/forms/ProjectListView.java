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

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Set;

import jakarta.annotation.PostConstruct;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.exceptions.ProjectDeletionException;
import org.kitodo.production.enums.ObjectType;
import org.kitodo.production.helper.Helper;
import org.kitodo.production.model.LazyBeanModel;
import org.kitodo.production.services.ServiceManager;
import org.kitodo.production.services.data.ProjectService;
import org.primefaces.model.SortMeta;
import org.primefaces.model.SortOrder;

@Named("ProjectListView")
@ViewScoped
public class ProjectListView extends BaseListView {

    public static final String VIEW_PATH = MessageFormat.format(REDIRECT_PATH, "projects") + "&tab=projectTab";
    private static final Logger logger = LogManager.getLogger(ProjectListView.class);

    /**
     * Initialize the list of displayed list columns.
     */
    @PostConstruct
    public void init() {
        // Lists of available list columns
        columns = new ArrayList<>();
        try {
            columns.add(ServiceManager.getListColumnService().getListColumnsForListAsSelectItemGroup("project"));
        } catch (DAOException e) {
            Helper.setErrorMessage(e.getLocalizedMessage(), logger, e);
        }

        // Lists of selected list columns
        selectedColumns = new ArrayList<>();
        selectedColumns.addAll(ServiceManager.getListColumnService().getSelectedListColumnsForListAndClient("project"));

        sortBy = SortMeta.builder().field("title").order(SortOrder.ASCENDING).build();
        setLazyBeanModel(new LazyBeanModel(ServiceManager.getProjectService()));
    }

    /**
     * Create new project.
     *
     * @return page address
     */
    public String newProject() {
        return ProjectEditView.VIEW_PATH + "&referrer=projects";
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
     * The set of allowed sort fields (columns) to sanitize the URL query parameter "sortField".
     * 
     * @return the set of allowed sort fields (columns)
     */
    @Override
    protected Set<String> getAllowedSortFields() {
        return Set.of("title", "metsRightsOwner", "active");
    }

}
