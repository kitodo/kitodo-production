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

var PROCESS = "process";
var TASK = "task";
var invisibleProcessFilterID = "processesTabView:processesForm:processesTable:titleColumn:filter";
var invisibleTaskFilterID = "tasksTabView:tasksForm:taskTable:titleColumn:filter";
var filterInputId = "filterMenu:filterfield";

function filterProcesses() {
    applyFilter(PROCESS, document.getElementById(filterInputId).value);
}

function filterTasks() {
    applyFilter(TASK, document.getElementById(filterInputId).value);
}

function applyFilter(type, filterString) {
    var invisibleFilterInput;
    if (type === PROCESS) {
        invisibleFilterInput = document.getElementById(invisibleProcessFilterID);
    } else if (type === TASK) {
        invisibleFilterInput = document.getElementById(invisibleTaskFilterID);
    }
    if (invisibleFilterInput != null) {
        invisibleFilterInput.value = filterString;
        invisibleFilterInput.dispatchEvent(new Event("keyup"));
    } else {
        console.error("Could not find invisible filter input");
    }
}

$(document).ready(function () {
    $(document).ready(function () {
        // twice in document.ready to execute after PrimeFaces callbacks
        var filterInput = document.getElementById(filterInputId);
        if (filterInput.value.length > 0) {
            filterInput.dispatchEvent(new Event("change"));
        }
    });
});
