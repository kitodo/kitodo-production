/*
 *
 * (c) Kitodo. Key to digital objects e. V. <contact@kitodo.org>
 *
 * This file is part of the Kitodo project.
 *
 * It is licensed under GNU General Public License version 3 or later.
 *
 * For the full copyright and license information, please read the
 * GPL3-License.txt file that was distributed with this source code.
 *
 */

function setFilter(filterString) {
    document.getElementById('filterMenu:filterfield').value = filterString;
    applyFilter(filterString);
}

function applyFilter(filterString) {
    var invisibleFilter = document.getElementById('tabs:processesForm:processList:titleColumn:filter');
    invisibleFilter.value = filterString;
    invisibleFilter.dispatchEvent(new Event('keyup'));
}

window.onload = function () {
    document.getElementById('filterMenu:filterfield').addEventListener('change', function () {
        applyFilter(this.value);
    });

    var filterString = document.getElementById('filterMenu:filterfield').value;
    if (filterString.length > 0) {
        applyFilter(filterString);
    }
}
