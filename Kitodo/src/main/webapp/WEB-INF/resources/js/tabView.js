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

/**
 * Methods and functions related to the Primefaces TabView component.
 */
// eslint-disable-next-line no-use-before-define
var tabView = tabView || {};

/**
 * Update the current browser URL to include the selected tabIndex.
 * 
 * @param int tabIndex the selected tab index
 */
tabView.updateTabIndexQueryParameter = function (tabIndex) {
    const url = new URL(window.location.href);
    url.searchParams.set("tabIndex", tabIndex);
    window.history.replaceState({}, "", url);
};
