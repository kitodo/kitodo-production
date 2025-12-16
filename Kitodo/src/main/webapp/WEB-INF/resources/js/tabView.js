/**
 * (c) Kitodo. Key to digital objects e. V. <contact@kitodo.org>
 *
 * This file is part of the Kitodo project.
 *
 * It is licensed under GNU General Public License version 3 or later.
 *
 * For the full copyright and license information, please read the
 * GPL3-License.txt file that was distributed with this source code.
 */

// eslint-disable-next-line no-use-before-define
var kitodo = kitodo || {};

// eslint-disable-next-line no-use-before-define
kitodo.tabView = kitodo.tabView || {};

/**
 * Event handler that is called when a user changes a tab in
 * one of the main views (users tabs, projects tabs).
 * 
 * @param int index the tabIndex of the tab now shown
 */
kitodo.tabView.onTabShow = function(index) {
    // write tabIndex into browser URL such that the page can be refreshed without loosing the active tab state
    kitodo.updateQueryParameter('tabIndex', index); 

    // remove URL query parameters related to list view pagination, sort state and filter state
    // such that it is not applied to another list in another tab
    kitodo.removeQueryParameter('firstRow'); 
    kitodo.removeQueryParameter('sortField');
    kitodo.removeQueryParameter('sortOrder');
}