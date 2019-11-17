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
/* globals select, setGalleryViewMode, destruct, initialize, scrollToSelectedThumbnail, changeToMapView, PF */

var metadataEditor = {
    dragging: false,
    handleMouseDown(event) {
        if ($(event.currentTarget).closest(".thumbnail-parent").find(".active").length === 0) {
            this.select(event);
        }
    },
    handleMouseUp(event) {
        this.dragdrop.removeDragAmountIcon();
        if (this.dragging) {
            this.dragging = false;
        } else if (event.button !== 2
            || $(event.currentTarget).closest(".thumbnail-parent").find(".active").length === 0) {
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

metadataEditor.contextMenu = {
    listen() {
        let imagePreviewForm = $("#imagePreviewForm");
        document.oncontextmenu = function() {
            return false;
        };
        $(document).on("mousedown", ".thumbnail-parent", function(event) {
            if (event.originalEvent.button === 2) {
                PF("galleryContextMenu").show(event);
                event.preventDefault();
                return false;
            }
        });
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

metadataEditor.shortcuts = {
    KEYS: {
        HELP: "?",
        STRUCTURED_VIEW: "_",
        DETAIL_VIEW: "*"
    },
    changeView(event, galleryViewMode) {
        let currentGalleryViewMode = $("#imagePreviewForm\\:galleryViewMode ").text().toUpperCase();
        if (currentGalleryViewMode !== galleryViewMode) {
            setGalleryViewMode([{name: "galleryViewMode", value: galleryViewMode}]);
        }
        event.preventDefault();
    },
    listen() {
        $(document).on("keydown.shortcuts", function (event) {
            if (!(document.activeElement.tagName === "INPUT" || document.activeElement.tagName === "TEXTAREA")
                && event.key === metadataEditor.shortcuts.KEYS.HELP) {
                PF("helpDialog").show();
                return;
            }

            if (event.key === metadataEditor.shortcuts.KEYS.STRUCTURED_VIEW && event.ctrlKey) {
                metadataEditor.shortcuts.changeView(event, "LIST");
            } else if (event.key === metadataEditor.shortcuts.KEYS.DETAIL_VIEW && event.ctrlKey) {
                metadataEditor.shortcuts.changeView(event, "PREVIEW");
            }
        });
    },
    ignore() {
        $(document).off("keydown.shortcuts");
    },
    updateViews() {
        switch ($("#imagePreviewForm\\:galleryViewMode ").text().toUpperCase()) {
            case "LIST":
                destruct();
                break;
            case "PREVIEW":
                initialize();
                scrollToSelectedThumbnail();
                changeToMapView();
                break;
        }
    }
};

$(document).ready(function () {
    metadataEditor.shortcuts.listen();
    metadataEditor.contextMenu.listen();
});
