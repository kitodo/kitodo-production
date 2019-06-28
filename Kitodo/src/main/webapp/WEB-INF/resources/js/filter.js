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

var filter = {
    PROCESS: "process",
    TASK: "task",
    invisibleProcessFilterID: "processesTabView:processesForm:processesTable:titleColumn:filter",
    invisibleTaskFilterID: "tasksTabView:tasksForm:taskTable:titleColumn:filter",
    filterInputId: "filterMenu:filterfield",

    applyFilter(type, filterString) {
        let invisibleFilterInput;
        if (type === this.PROCESS) {
            invisibleFilterInput = document.getElementById(this.invisibleProcessFilterID);
        } else if (type === this.TASK) {
            invisibleFilterInput = document.getElementById(this.invisibleTaskFilterID);
        }
        if (invisibleFilterInput != null) {
            invisibleFilterInput.value = filterString;
            invisibleFilterInput.dispatchEvent(new Event("keyup"));
        } else {
            let summary = "Error initializing filters: ";
            let detail = "Could not find invisible filter input.";
            let wrapperStructure = "<div class=\"ui-messages-error ui-corner-all\"><span class=\"ui-messages-error-icon\"></span><ul></ul></div>";
            let messageStructure = "<li><span class=\"ui-messages-error-summary\">" + summary + "</span><span class=\"ui-messages-error-detail\">" + detail + "</span></li>";
            $("#error-messages").append(wrapperStructure).find("ul").append(messageStructure);
        }
    },
    filterProcesses() {
        this.applyFilter(this.PROCESS, document.getElementById(this.filterInputId).value);
    },
    filterTasks() {
        this.applyFilter(this.TASK, document.getElementById(this.filterInputId).value);
    },
    init() {
        let filterInput = document.getElementById(this.filterInputId);
        if (filterInput.value.length > 0) {
            filterInput.dispatchEvent(new Event("change"));
        }
    }
};

$(document).ready(function () {
    $(document).ready(function () {
        // twice in document.ready to execute after PrimeFaces callbacks
        filter.init();
    });
});
