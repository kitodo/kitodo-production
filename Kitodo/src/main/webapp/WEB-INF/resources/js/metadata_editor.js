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
/* globals select, setGalleryViewMode, destruct, initialize, scrollToSelectedThumbnail, changeToMapView, PF,
   scrollToStructureThumbnail, scrollToPreviewThumbnail, expandMetadata, preserveMetadata, setConfirmUnload,
   activateButtons */

var metadataEditor = {
    dragging: false,
    handleMouseDown(event) {
        $(document.activeElement).blur();
        let target = $(event.target);
        if (target.closest(".stripe").length === 1) {
            this.stripes.handleMouseDown(event);
        } else if (target.closest(".thumbnail-container").length === 1) {
            this.pages.handleMouseDown(event, target.closest(".thumbnail-container"));
        }
        // set focus for gallery (used for keyboard shortcuts)
        $(".focusable").removeClass("focused");
        $("#imagePreviewForm").addClass("focused");
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
                metadataEditor.select(target[0].dataset.order, target[0].dataset.stripe, "multi", event);
            } else if (event.shiftKey) {
                metadataEditor.select(target[0].dataset.order, target[0].dataset.stripe, "range", event);
            } else {
                metadataEditor.select(target[0].dataset.order, target[0].dataset.stripe, "default", event);
            }
        }
    },
    stripes: {
        handleMouseDown(event) {
            if (!$(event.target).hasClass("selected")) {
                metadataEditor.select(null, event.target.dataset.stripe, "default", event);
            }
        },
    },
    select(pageIndex, stripeIndex, selectionType, triggerEvent = null) {
        // call the remoteCommand in gallery.xhtml
        if (triggerEvent == null) {
            select([
                {name: "page", value: pageIndex},
                {name: "stripe", value: stripeIndex},
                {name: "selectionType", value: selectionType}
            ]);
        } else {
            select([
                {name: "page", value: pageIndex},
                {name: "stripe", value: stripeIndex},
                {name: "selectionType", value: selectionType},
                {name: "pageX", value: triggerEvent.pageX},
                {name: "pageY", value: triggerEvent.pageY},
                {name: "triggerContextMenu", value: triggerEvent.button === 2}
            ]);
        }
    }
};

metadataEditor.contextMenu = {
    listen() {
        document.oncontextmenu = function(event) {
            return event.target.tagName === "INPUT" || event.target.tagName === "TEXTAREA";
        };
        $(document).on("mousedown.thumbnail", ".thumbnail-parent", function(event) {
            if (event.originalEvent.button === 2 && $(event.target).closest(".thumbnail-parent").find(".selected").length === 1) {
                PF("mediaContextMenu").show(event);
            }
        });
        $(document).on("mousedown.stripe", ".stripe", function(event) {
            if (event.originalEvent.button === 2 && $(event.target).closest(".stripe").find(".selected").length === 1) {
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
    getGalleryViewMode() {
        return $("#imagePreviewForm\\:galleryViewMode ").text().toUpperCase();
    },
    changeView(galleryViewMode) {
        if (this.getGalleryViewMode() !== galleryViewMode) {
            setGalleryViewMode([{name: "galleryViewMode", value: galleryViewMode}]);
        }
    },
    jumpToGalleryImage(thumbnails, selectedThumbnail, delta, vertical) {
        let currentIndex;
        let selectableThumbnails;
        if (vertical) {
            let posX = selectedThumbnail.offset().left;
            selectableThumbnails = $([]);
            thumbnails.each(function () {
                if (posX === $(this).offset().left) {
                    selectableThumbnails = selectableThumbnails.add($(this));
                }
            });
            currentIndex = selectableThumbnails.get().indexOf(selectedThumbnail[0]);
        } else {
            selectableThumbnails = thumbnails;
            currentIndex = thumbnails.index(selectedThumbnail);
        }
        let newIndex = currentIndex + delta;
        if (currentIndex >= 0 && newIndex >= 0 && newIndex < selectableThumbnails.length) {
            metadataEditor.select(selectableThumbnails[newIndex].dataset.order, selectableThumbnails[newIndex].dataset.stripe, "default");
            let galleryViewMode = this.getGalleryViewMode();
            if (galleryViewMode === "LIST") {
                scrollToStructureThumbnail(selectableThumbnails.eq(newIndex), $("#imagePreviewForm\\:structuredPagesField"));
            } else if (galleryViewMode === "PREVIEW") {
                scrollToPreviewThumbnail(selectableThumbnails.eq(newIndex), $("#thumbnailStripeScrollableContent"));
            }
            return true;
        }
        return false;
    },
    jumpToSelectedImage(delta, vertical) {
        let gallery = $("#galleryWrapperPanel");
        let lastSelection = gallery.find(".thumbnail.last-selection + .thumbnail-container");
        if (lastSelection.length === 1) {
            let thumbnails = gallery.find(".thumbnail + .thumbnail-container");
            if (delta > 0) {
                for (; delta > 0; delta--) {
                    if (vertical && this.jumpToGalleryImage(thumbnails, lastSelection, delta, true)) {
                        break;
                    } else if (!vertical && this.jumpToGalleryImage(thumbnails, lastSelection, delta, false)) {
                        break;
                    }
                }
            } else if (delta < 0) {
                for (; delta < 0; delta++) {
                    if (vertical && this.jumpToGalleryImage(thumbnails, lastSelection, delta, true)) {
                        break;
                    } else if (!vertical && this.jumpToGalleryImage(thumbnails, lastSelection, delta, false)) {
                        break;
                    }
                }
            }
        }
    },
    jumpToTreeNode(treeNodes, selectedTreeNode, delta) {
        let currentIndex = treeNodes.index(selectedTreeNode);
        let newIndex = currentIndex + delta;
        if (currentIndex >= 0 && newIndex >= 0 && newIndex < treeNodes.length) {
            treeNodes.eq(newIndex).children(".ui-treenode-content").children(".ui-treenode-label").click();
            return true;
        }
        return false;
    },
    jumpToSelectedTreeNode(tree, delta, vertical) {
        let lastSelection = tree.find("li.ui-treenode[aria-selected='true']");
        if (!vertical && lastSelection.length === 1) {
            lastSelection.children(".ui-treenode-children").is(":visible");
            if (delta <= -1 && lastSelection.children(".ui-treenode-children").is(":visible")) {
                lastSelection.children(".ui-treenode-content").children(".ui-tree-toggler").click();
            } else if (delta >= 1 && lastSelection.children(".ui-treenode-children").is(":hidden")) {
                lastSelection.children(".ui-treenode-content").children(".ui-tree-toggler").click();
            }
        } else if (vertical && lastSelection.length === 1) {
            let treeNodes = tree.find("li.ui-treenode:visible");
            if (delta > 0) {
                for (; delta > 0; delta--) {
                    if (this.jumpToTreeNode(treeNodes, lastSelection, delta)) {
                        break;
                    }
                }
            } else if (delta < 0) {
                for (; delta < 0; delta++) {
                    if (this.jumpToTreeNode(treeNodes, lastSelection, delta)) {
                        break;
                    }
                }
            }
        }
    },
    navigateByShortcut(delta, vertical) {
        let focusedArea = $(".focused");
        if (focusedArea.length !== 1 || focusedArea[0] === null) {
            return;
        }
        switch (focusedArea[0].id) {
            case "logicalTree":
            case "physicalTree":
                metadataEditor.shortcuts.jumpToSelectedTreeNode(focusedArea.eq(0), delta, vertical);
                break;
            case "imagePreviewForm":
            default:
                metadataEditor.shortcuts.jumpToSelectedImage(delta, vertical); // gallery
        }
    },
    handleShortcut(shortcut) {
        switch (shortcut) {
            case "help":
                if (!(document.activeElement.tagName === "INPUT" || document.activeElement.tagName === "TEXTAREA")) {
                    PF("helpDialog").show();
                }
                break;
            case "structuredView":
                metadataEditor.shortcuts.changeView("LIST");
                break;
            case "detailView":
                metadataEditor.shortcuts.changeView("PREVIEW");
                break;
            case "nextItem":
                metadataEditor.shortcuts.navigateByShortcut(1, false);
                break;
            case "previousItem":
                metadataEditor.shortcuts.navigateByShortcut(-1, false);
                break;
            case "nextItemMulti":
                metadataEditor.shortcuts.navigateByShortcut(20, false);
                break;
            case "previousItemMulti":
                metadataEditor.shortcuts.navigateByShortcut(-20, false);
                break;
            case "downItem":
                metadataEditor.shortcuts.navigateByShortcut(1, true);
                break;
            case "upItem":
                metadataEditor.shortcuts.navigateByShortcut(-1, true);
                break;
            case "downItemMulti":
                metadataEditor.shortcuts.navigateByShortcut(20, true);
                break;
            case "upItemMulti":
                metadataEditor.shortcuts.navigateByShortcut(-20, true);
                break;
            default:
                // This default case is only reached when shortcuts exist which are not implemented.
        }
    },
    evaluateKeys(event) {
        Object.keys(metadataEditor.shortcuts.KEYS).forEach((key) => {
            let keyCombination = metadataEditor.shortcuts.KEYS[key];
            if (event.ctrlKey === keyCombination.includes("Control")
                && event.shiftKey === keyCombination.includes("Shift")
                && event.metaKey === keyCombination.includes("Meta")
                && event.altKey === keyCombination.includes("Alt")
                && keyCombination.includes(event.code)
            ) {
                metadataEditor.shortcuts.handleShortcut(key);
                event.preventDefault();
            }
        });
    },
    listen (shortcuts) {
        metadataEditor.shortcuts.KEYS = shortcuts;
        $(document).on("keydown.shortcuts", function (event) {
            metadataEditor.shortcuts.evaluateKeys(event.originalEvent);
        });
        $(document).on("click.shortcuts", ".focusable", function (event) {
            $(".focused").removeClass("focused");
            event.currentTarget.classList.add("focused");
        });
    },
    ignore() {
        $(document).off("keydown.shortcuts");
    },
    updateViews() {
        switch (this.getGalleryViewMode()) {
            case "LIST":
                destruct();
                scrollToSelectedThumbnail();
                break;
            case "PREVIEW":
                initialize();
                scrollToSelectedThumbnail();
                changeToMapView();
                break;
        }
    }
};

function expandMetadata(panelClass) {
    $("." + panelClass + "[aria-expanded='false']").click();
}

function setConfirmUnload(on) {
    window.onbeforeunload = (on) ? function() { return true; } : void 0;
}

function activateButtons() {
    PF('saveExit').enable();
    PF('save').enable();
    PF('validate').enable();
    PF('close').enable();
}

function deactivateButtons() {
    PF('saveExit').disable();
    PF('save').disable();
    PF('validate').disable();
    PF('close').disable();
}
