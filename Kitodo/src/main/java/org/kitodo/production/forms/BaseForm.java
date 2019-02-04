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

import java.io.Serializable;
import java.util.List;
import java.util.Objects;

import javax.faces.model.SelectItem;

import org.kitodo.data.database.beans.Client;
import org.kitodo.data.database.beans.ListColumn;
import org.kitodo.data.database.beans.User;
import org.kitodo.production.helper.Helper;
import org.kitodo.production.model.LazyDTOModel;
import org.kitodo.production.services.ServiceManager;
import org.primefaces.event.TabChangeEvent;

public class BaseForm implements Serializable {

    private static final long serialVersionUID = 2950419497162710096L;
    String stayOnCurrentPage = null;
    protected String filter = "";
    protected User user;
    protected LazyDTOModel lazyDTOModel = null;
    private static final String REDIRECT_PARAMETER = "faces-redirect=true";
    private static final String TEMPLATE_ROOT = "/pages/";
    private int activeTabId = 0;
    private boolean saveDisabled = true;

    // error messages
    protected static final String ERROR_DATABASE_READING = "errorDatabaseReading";
    protected static final String ERROR_DUPLICATE = "errorDuplicate";
    protected static final String ERROR_EXPORTING = "errorExporting";
    protected static final String ERROR_LOADING_MANY = "errorLoadingMany";
    protected static final String ERROR_LOADING_ONE = "errorLoadingOne";
    protected static final String ERROR_DELETING = "errorDeleting";
    protected static final String ERROR_READING = "errorReading";
    protected static final String ERROR_RELOADING = "errorReloading";
    protected static final String ERROR_SAVING = "errorSaving";

    protected static final String REDIRECT_PATH = TEMPLATE_ROOT + "{0}?" + REDIRECT_PARAMETER;
    protected static final String DEFAULT_LINK = "desktop";

    protected List<SelectItem> columns;
    protected List<ListColumn> selectedColumns;

    /**
     * Getter: return lazyDTOModel.
     *
     * @return LazyDTOModel
     */
    public LazyDTOModel getLazyDTOModel() {
        return lazyDTOModel;
    }

    /**
     * Setter: set lazyDTOModel.
     *
     * @param lazyDTOModel
     *            LazyDTOModel to set for this class
     */
    public void setLazyDTOModel(LazyDTOModel lazyDTOModel) {
        this.lazyDTOModel = lazyDTOModel;
    }

    /**
     * Get overlay for return null.
     * 
     * @return to current page
     */
    public String getStayOnCurrentPage() {
        return this.stayOnCurrentPage;
    }

    /**
     * Set overlay for return null.
     * 
     * @param stayOnCurrentPage
     *            overlay for return null which ensures staying on the same
     *            page where it was called
     */
    public void setStayOnCurrentPage(String stayOnCurrentPage) {
        this.stayOnCurrentPage = stayOnCurrentPage;
    }

    /**
     * Get User.
     *
     * @return User
     */
    public User getUser() {
        if (Objects.isNull(this.user)) {
            this.user = ServiceManager.getUserService().getCurrentUser();
        }
        return this.user;
    }

    public String getFilter() {
        return this.filter;
    }

    public void setFilter(String filter) {
        this.filter = filter;
    }

    /**
     * Add filter to user.
     */
    public void addFilterToUser() {
        if (Objects.isNull(this.filter) || this.filter.isEmpty()) {
            return;
        }
        ServiceManager.getUserService().addFilter(getUser(), this.filter);
    }

    /**
     * Get user filters.
     */
    public List<String> getUserFilters() {
        return ServiceManager.getUserService().getFilters(getUser());
    }

    /**
     * Remove filter from user.
     */
    public void removeFilterFromUser() {
        if (Objects.isNull(this.filter) || this.filter.isEmpty()) {
            return;
        }
        ServiceManager.getUserService().removeFilter(getUser(), this.filter);
    }

    /**
     * Return index of active tab.
     *
     * @return index of active tab
     */
    public int getActiveTabIndex() {
        return activeTabId;
    }

    /**
     * Set index of active tab.
     *
     * @param id
     *            index of active tab
     */
    public void setActiveTabIndex(int id) {
        this.activeTabId = id;
    }

    /**
     * Updates the active tab index whenever the TabChangeEvent is fired.
     *
     * @param event
     *            TabChangeEvent is fired when the user changes the tab in the
     *            current tab view
     */
    public void onTabChange(TabChangeEvent event) {
        setActiveTabIndex(event.getComponent().getChildren().indexOf(event.getTab()));
    }

    /**
     * Return boolean to disable save button.
     *
     * @return status of save button
     */
    public boolean isSaveDisabled() {
        return saveDisabled;
    }

    /**
     * Set boolean to disable save button.
     *
     * @param saveDisabled
     *            true or false
     */
    public void setSaveDisabled(boolean saveDisabled) {
        this.saveDisabled = saveDisabled;
    }

    /**
     * Get list of configurable columns.
     * @return list of configurable columns
     */
    public List<SelectItem> getColumns() {
        return columns;
    }

    /**
     * Set list of configurable columns.
     * @param columns list of columns
     */
    public void setColumns(List<SelectItem> columns) {
        this.columns = columns;
    }

    /**
     * Get list of selected columns.
     * @return list of selected columns
     */
    public List<ListColumn> getSelectedColumns() {
        return selectedColumns;
    }

    /**
     * Set list of selected columns.
     * @param columns list of selected columns
     */
    public void setSelectedColumns(List<ListColumn> columns) {
        this.selectedColumns = columns;
    }

    /**
     * Checks whether the column with the provided name 'columnName' should be
     * shown be displayed in the corresponding list view or not.
     * @param columnName name of the column
     * @return true, if column should be displayed; false if column should be hidden
     */
    public boolean showColumn(String columnName) {
        for (ListColumn listColumn : this.selectedColumns) {
            if (listColumn.getTitle().equals(columnName)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Save selected columns to current client.
     */
    public void saveSelectedColumns() {
        try {
            ServiceManager.getListColumnService().saveSelectedColumnsToClient(selectedColumns);
        } catch (Exception e) {
            Helper.setErrorMessage(e.getLocalizedMessage());
            e.printStackTrace();
        }
    }
}
