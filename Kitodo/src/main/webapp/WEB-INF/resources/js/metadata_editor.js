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

var metadataEditor = {};

metadataEditor.select = {

    selectionType: function(event) {
        if (event.metaKey || event.ctrlKey) {
            select([{name: 'page', value: event.currentTarget.dataset.order},{name: 'selectionType', value: 'multi'}]);
        } else if (event.shiftKey) {
            select([{name: 'page', value: event.currentTarget.dataset.order},{name: 'selectionType', value: 'range'}]);
        } else {
            select([{name: 'page', value: event.currentTarget.dataset.order},{name: 'selectionType', value: 'default'}]);
        }
    }
};

metadataEditor.dragdrop = {
    addDragAmountIcon: function(event) {
        var dragAmount = document.querySelectorAll(".thumbnail.active").length;
        if (dragAmount > 1) {
            var element = document.createElement("div");
            element.id = "dragAmount";
            element.innerHTML = dragAmount;
            event.currentTarget.appendChild(element);
        }
    },
    removeDragAmountIcon: function(event) {
        var element = document.getElementById("dragAmount");
        if (element !== null) {
            element.parentNode.removeChild(element);
        }
    }
};
