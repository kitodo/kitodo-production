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
/* globals select */

var metadataEditor = {
    dragging: false,
    handleMouseDown(event) {
        if (event.currentTarget.querySelectorAll(".active").length === 0) {
            this.select(event);
        }
    },
    handleMouseUp(event) {
        this.dragdrop.removeDragAmountIcon();
        if (this.dragging) {
            this.dragging = false;
        } else {
            this.select(event);
        }
    },
    handleDragStart(event) {
        this.dragging = true;
        this.dragdrop.addDragAmountIcon(event);
    },
    select(event) {
        if (event.metaKey || event.ctrlKey) {
            select([
                {name: "page", value: event.currentTarget.dataset.order},
                {name: "stripe", value: event.currentTarget.dataset.stripe},
                {name: "selectionType", value: "multi"}
            ]);
        } else if (event.shiftKey) {
            select([
                {name: "page", value: event.currentTarget.dataset.order},
                {name: "stripe", value: event.currentTarget.dataset.stripe},
                {name: "selectionType", value: "range"}
            ]);
        } else {
            select([
                {name: "page", value: event.currentTarget.dataset.order},
                {name: "stripe", value: event.currentTarget.dataset.stripe},
                {name: "selectionType", value: "default"}
            ]);
        }
    }
};

metadataEditor.dragdrop = {
    addDragAmountIcon(event) {
        var dragAmount = document.querySelectorAll(".thumbnail.active").length;
        if (dragAmount > 1) {
            var element = document.createElement("div");
            element.id = "dragAmount";
            element.innerText = dragAmount;
            event.currentTarget.appendChild(element);
        }
    },
    removeDragAmountIcon() {
        var element = document.getElementById("dragAmount");
        if (element !== null) {
            element.parentNode.removeChild(element);
        }
    }
};
