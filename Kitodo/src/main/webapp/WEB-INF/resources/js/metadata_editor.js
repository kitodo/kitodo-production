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
        let target = $(event.target);
        if (target.closest(".stripe").length === 1) {
            this.stripes.handleMouseDown(event);
        } else if (target.closest(".thumbnail-container").length === 1) {
            this.pages.handleMouseDown(event, target.closest(".thumbnail-container"));
        }
    },
    handleMouseUp(event) {
        let target = $(event.target);
        if (target.closest(".thumbnail-container").length === 1) {
            this.pages.handleMouseUp(event, target.closest(".thumbnail-container"));
        }
    },
    handleDragStart(event) {
        this.pages.handleDragStart(event);
    },
    pages: {
        handleMouseDown(event, target) {
            if (target.closest(".thumbnail-parent").find(".selected").length === 0) {
                this.select(event, target);
            }
        },
        handleMouseUp(event, target) {
            metadataEditor.dragdrop.removeDragAmountIcon();
            if (metadataEditor.dragging) {
                metadataEditor.dragging = false;
            } else if (event.button !== 2 || target.closest(".thumbnail-parent").find(".selected").length === 0) {
                this.select(event, target);
            }
        },
        handleDragStart(event) {
            metadataEditor.dragging = true;
            metadataEditor.dragdrop.addDragAmountIcon(event);
        },
        select(event, target) {
            if (event.metaKey || event.ctrlKey) {
                metadataEditor.select(target[0].dataset.order, target[0].dataset.stripe, "multi");
            } else if (event.shiftKey) {
                metadataEditor.select(target[0].dataset.order, target[0].dataset.stripe, "range");
            } else {
                metadataEditor.select(target[0].dataset.order, target[0].dataset.stripe, "default");
            }
        }
    },
    stripes: {
        handleMouseDown(event) {
            if (!$(event.target).hasClass("selected")) {
                metadataEditor.select(null, event.target.dataset.stripe, "default");
            }
        },
    },
    select(pageIndex, stripeIndex, selectionType) {
        // call the remoteCommand in gallery.xhtml
        select([
            {name: "page", value: pageIndex},
            {name: "stripe", value: stripeIndex},
            {name: "selectionType", value: selectionType}
        ]);
    }
};

metadataEditor.contextMenu = {
    listen() {
        document.oncontextmenu = function() {
            return false;
        };
        $(document).on("mousedown.thumbnail", ".thumbnail-parent", function(event) {
            if (event.originalEvent.button === 2) {
                PF("mediaContextMenu").show(event);
            }
        });
        $(document).on("mousedown.stripe", ".stripe", function(event) {
            if (event.originalEvent.button === 2) {
                PF("stripeContextMenu").show(event);
            }
        });
    }
};

metadataEditor.dragdrop = {
    addDragAmountIcon(event) {
        var dragAmount = document.querySelectorAll(".thumbnail.selected").length;
        if (dragAmount > 1) {
            var element = document.createElement("div");
            element.id = "dragAmount";
            element.innerText = dragAmount;
            event.target.appendChild(element);
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
