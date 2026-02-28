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

package org.kitodo.production.forms.process;

import java.io.Serializable;

import jakarta.enterprise.context.SessionScoped;

/**
 * Process list state that should presist over multiple views.
 * 
 * <p>This bean preserves the filter string that was last used while visiting the process list.
 * It is needed because sometimes users navigate away from the process list. Upon returning, they 
 * expect the current filter setting to be preserved. The filter is only applied if there is no
 * other filter provided as URL paramater.</p>
 */
@SessionScoped
public class ProcessListViewSessionState implements Serializable {
   
    /**
     * The filter that was last set by the user while visiting the process list view.
     * 
     * <p>This filter is used as the default filter in case the user navigates to the process list 
     * view from another page without passing a specific filter as URL parameter (e.g. through the
     * main menu).</p>
     */
    private String lastFilter;

    /**
     * Set the filter that was last used while visiting the process list view.
     * 
     * @param filter the filter string
     */
    public void setLastFilter(String filter) {
        this.lastFilter = filter;
    }

    /**
     * Return the filter thas was last used file visiting the process list view.
     * 
     * @return the filter string
     */
    public String getLastFilter() {
        return this.lastFilter;
    }

}
