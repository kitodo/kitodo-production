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


import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

import jakarta.annotation.PostConstruct;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import org.kitodo.production.forms.process.ProcessListView;
import org.kitodo.production.services.ServiceManager;
import org.kitodo.production.services.security.SecurityAccessService;

@Named("ProcessesTabView")
@ViewScoped
public class ProcessesTabView extends BaseTabView {

    private static final SecurityAccessService securityAccessService = ServiceManager.getSecurityAccessService();

    private static final String PROCESS_TAB_ID = "processTab";
    private static final String BATCH_TAB_ID = "batchTab";

    @Inject
    private ProcessListView processListView;
    
    /**
     * Initialize users tab view.
     */
    @PostConstruct
    public void init() {
        setActiveTabId(PROCESS_TAB_ID);
    }

    /**
     * Apply view parameter "firstRow" to currently active list view depending on tab index.
     * 
     * @param firstRow the row index of the first row to be displayed in the active list view
     */
    @Override
    public void setFirstRowFromTemplate(String firstRow) {
        if (getActiveTabId().equals(PROCESS_TAB_ID)) {
            processListView.setFirstRowFromTemplate(firstRow);
        }
    }

    /**
     * Sets the sort by query parameters for the currently active list view.
     * 
     * @param field the sort by field
     * @param order the sort by order
     */
    public void setSortByFromTemplate(String field, String order) {
        if (getActiveTabId().equals(PROCESS_TAB_ID)) {
            processListView.setSortByFromTemplate(field, order);
        }
    }

    /**
     * Sets the filter from a URL query parameter.
     * 
     * @param encodedFilter the filter value provided as encoded URL query parameter
     */
    public void setFilterFromTemplate(String encodedFilter) {
        if (getActiveTabId().equals(PROCESS_TAB_ID)) {
            // user list view
            processListView.setFilterFromTemplate(encodedFilter);
        }
    }

    /** 
     * Overwrite allowed tab ids for sanitization of URL parameter.
     */
    @Override
    protected List<String> getAllowedTabIds() {
        return Stream.of(
            securityAccessService.hasAuthorityToViewProcessList() ? PROCESS_TAB_ID : null,
            securityAccessService.hasAuthorityToViewBatchList() ? BATCH_TAB_ID : null
        ).filter(Objects::nonNull).toList();
    }

}
