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

    selectionType(event) {
        if (event.metaKey || event.ctrlKey) {
            select([{name: "page", value: event.currentTarget.dataset.order},{name: "selectionType", value: "multi"}]);
        } else if (event.shiftKey) {
            select([{name: "page", value: event.currentTarget.dataset.order},{name: "selectionType", value: "range"}]);
        } else {
            select([{name: "page", value: event.currentTarget.dataset.order},{name: "selectionType", value: "default"}]);
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

metadataEditor.shortcuts = {
    KEYS: {
        STRUCTURED_VIEW: 83, // "S"
        UNSTRUCTURED_VIEW: 85, // "U"
        DETAIL_VIEW: 68 // "D"
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
            if (event.metaKey ||event.ctrlKey) {
                switch (event.keyCode) {
                    case metadataEditor.shortcuts.KEYS.STRUCTURED_VIEW:
                        metadataEditor.shortcuts.changeView(event, "LIST");
                        break;
                    case metadataEditor.shortcuts.KEYS.UNSTRUCTURED_VIEW:
                        metadataEditor.shortcuts.changeView(event, "GRID");
                        break;
                    case metadataEditor.shortcuts.KEYS.DETAIL_VIEW:
                        metadataEditor.shortcuts.changeView(event, "PREVIEW");
                        break;
                }
            }
        });
    },
    ignore() {
        $(document).off("keydown.shortcuts");
    },
    updateViews() {
        switch ($("#imagePreviewForm\\:galleryViewMode ").text().toUpperCase()) {
            case "LIST":
            case "GRID":
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
});
