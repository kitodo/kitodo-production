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

import java.util.Collections;
import java.util.List;
import java.util.Objects;

import org.primefaces.PrimeFaces;
import org.primefaces.event.TabChangeEvent;

/**
 * Base class for a tab view.
 */
public class BaseTabView extends BaseForm {

    /**
     * The id of the active tab. Should match the "id" attribute of the `p:tab` element.
     */
    private String activeTabId;
    
    /**
     * Method that is called from viewAction of a tab view containing the URL parameter
     * signaling which tab should be active.
     *
     * @param tabId the id of the tab that should be active
     */
    public void setActiveTabIdFromTemplate(String tabId) {
        if (Objects.nonNull(tabId) && !tabId.isEmpty()) {
            if (getAllowedTabIds().contains(tabId)) {
                this.activeTabId = tabId;
            }
        }
    }

    /**
     * Return the currently active tab id.
     * 
     * @return the id of the tab currently active
     */
    public String getActiveTabId() {
        return this.activeTabId;
    }

    /**
     * Sets the currently active tab id.
     * 
     * @param tabId the id of the new currently active tab
     */
    protected void setActiveTabId(String tabId) {
        this.activeTabId = tabId;
    }

    /**
     * Updates the active tab whenever the TabChangeEvent of the `p:tabView` component is fired.
     *
     * @param event TabChangeEvent is fired when the user changes the tab
     */
    @Override
    public void onTabChange(TabChangeEvent event) {
        String tabId = event.getTab().getId();
        setActiveTabId(tabId);
        String script = "kitodo.tabView.onTabChange('" + tabId + "');";
        PrimeFaces.current().executeScript(script);
    }

    /**
     * The list of allowed tab ids (in order) for a specific tab view (to sanitize URL parameter value) 
     * and determine the correct active tab index. 
     * 
     * <p>This method needs to be overwritten by each tab view. It should only return tabs that are actually 
     * shown to the user (considering authorities), such that the correct index can be calculated.
     * 
     * @return the set of allowed tab ids
     */
    protected List<String> getAllowedTabIds() {
        return Collections.emptyList();
    }

    /**
     * Return the index of the tab that is shown when first visiting the tab view.
     * 
     * @return the index of the tab view that is shown by the `p:tabView` component
     */
    public int getDerivedActiveTabIndex() {
        return getAllowedTabIds().indexOf(getActiveTabId());
    }
}
