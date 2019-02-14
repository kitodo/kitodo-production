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

package org.kitodo.production.helper;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.faces.context.FacesContext;
import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;

import org.primefaces.model.menu.DefaultMenuItem;
import org.primefaces.model.menu.DefaultMenuModel;
import org.primefaces.model.menu.MenuItem;
import org.primefaces.model.menu.MenuModel;

@Named
@ApplicationScoped
public class BreadcrumbMenuModel {

    private static final String DESKTOP_PATH = "desktop.jsf";
    private static final String PROJECTS_PATH = "projects.jsf";
    private static final String PROJECT_EDIT_PATH = "projectEdit.jsf";
    private static final String PROCESSES_PATH = "processes.jsf";
    private static final String PROCESS_EDIT_PATH = "processEdit.jsf";
    private static final String USERS_PATH = "users.jsf";
    private static final String USER_EDIT_PATH = "userEdit.jsf";
    private static final String TASKS_PATH = "tasks.jsf";
    private static final String METADATA_EDITOR_PATH = "metadataEditor.jsf";

    private static DefaultMenuItem desktopItem = new DefaultMenuItem(Helper.getTranslation("desktop"), null, DESKTOP_PATH);
    private static DefaultMenuItem projectsItem = new DefaultMenuItem(Helper.getTranslation("projects"), null, PROJECTS_PATH);
    private static DefaultMenuItem projectEditItem = new DefaultMenuItem(Helper.getTranslation("projectEdit"), null, PROJECT_EDIT_PATH);
    private static DefaultMenuItem processesItem = new DefaultMenuItem(Helper.getTranslation("processes"), null, PROCESSES_PATH);
    private static DefaultMenuItem processEditItem = new DefaultMenuItem(Helper.getTranslation("processEdit"), null, PROCESS_EDIT_PATH);
    private static DefaultMenuItem usersItem = new DefaultMenuItem(Helper.getTranslation("users"), null, USERS_PATH);
    private static DefaultMenuItem userEditItem = new DefaultMenuItem(Helper.getTranslation("userEdit"), null, USER_EDIT_PATH);
    private static DefaultMenuItem tasksItem = new DefaultMenuItem(Helper.getTranslation("tasks"), null, TASKS_PATH);
    private static DefaultMenuItem metadataEditorItem = new DefaultMenuItem(
            Helper.getTranslation("metadataEdit"), null, METADATA_EDITOR_PATH);

    private static HashMap<String, List<MenuItem>> pathMap = new HashMap<>();

    static {
        pathMap.put(DESKTOP_PATH, Collections.singletonList(desktopItem));
        pathMap.put(PROJECTS_PATH, Arrays.asList(desktopItem, projectsItem));
        pathMap.put(PROJECT_EDIT_PATH, Arrays.asList(desktopItem, projectsItem, projectEditItem));
        pathMap.put(PROCESSES_PATH, Arrays.asList(desktopItem, processesItem));
        pathMap.put(PROCESS_EDIT_PATH, Arrays.asList(desktopItem, processesItem, processEditItem));
        pathMap.put(USERS_PATH, Arrays.asList(desktopItem, usersItem));
        pathMap.put(USER_EDIT_PATH, Arrays.asList(desktopItem, usersItem, userEditItem));
        pathMap.put(TASKS_PATH, Arrays.asList(desktopItem, tasksItem));
        pathMap.put(METADATA_EDITOR_PATH, Arrays.asList(desktopItem, metadataEditorItem));
    }

    private static MenuModel createMenuModel(List<MenuItem> menuItems) {
        MenuModel menuModel = new DefaultMenuModel();
        for (MenuItem menuItem : menuItems) {
            menuModel.addElement(menuItem);
        }
        return menuModel;
    }

    private static HashMap<String, MenuModel> breadcrumbModels;

    @PostConstruct
    private void init() {
        breadcrumbModels = new HashMap<>();
        for (Map.Entry<String, List<MenuItem>> page : pathMap.entrySet()) {
            breadcrumbModels.put(page.getKey(), createMenuModel(page.getValue()));
        }
    }

    /**
     * Return MenuModel for breadcrumb menu.
     *
     * @return MenuModel for breadcrumb menu
     */
    public MenuModel getBreadcrumbModel() {
        HttpServletRequest request = (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest();
        String page = request.getRequestURI().substring(request.getRequestURI().lastIndexOf('/') + 1);
        if (!pathMap.containsKey(page)) {
            return createMenuModel(
                    Arrays.asList(desktopItem, new DefaultMenuItem(Helper.getTranslation(page.replace(".jsf", "")), null, page)));
        }
        return breadcrumbModels.get(page);
    }
}
