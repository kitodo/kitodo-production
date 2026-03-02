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

package org.kitodo.production.forms.task;

import java.io.Serializable;

import jakarta.enterprise.context.SessionScoped;

/**
 * Task list state that should presist over multiple views.
 * 
 * <p>This bean preserves the filter string that was last used while visiting the task list.
 * It is needed because sometimes users navigate away from the task list. Upon returning, they 
 * expect the current filter setting to be preserved. The filter is only applied if there is no
 * other filter provided as URL paramater.</p>
 */
@SessionScoped
public class TaskListViewSessionState implements Serializable {
   
    /**
     * The filter that was last set by the user while visiting the task list view.
     * 
     * <p>This filter is used as the default filter in case the user navigates to the task list 
     * view from another page without passing a specific filter as URL parameter (e.g. through the
     * main menu).</p>
     */
    private String lastFilter;

    /**
     * Set the filter that was last used while visiting the task list view.
     * 
     * @param filter the filter string
     */
    public void setLastFilter(String filter) {
        this.lastFilter = filter;
    }

    /**
     * Return the filter thas was last used file visiting the task list view.
     * 
     * @return the filter string
     */
    public String getLastFilter() {
        return this.lastFilter;
    }

}
