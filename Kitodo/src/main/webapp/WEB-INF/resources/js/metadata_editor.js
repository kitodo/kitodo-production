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
/* globals sendGallerySelect, setGalleryViewMode, destruct, initialize, scrollToSelectedThumbnail, changeToMapView, PF,
   scrollToStructureThumbnail, scrollToPreviewThumbnail, expandMetadata, preserveMetadata, setConfirmUnload,
   activateButtons, PF */
/*eslint new-cap: ["error", { "capIsNewExceptionPattern": "^PF" }]*/

var metadataEditor = {};

metadataEditor.gallery = {
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
                // do not trigger selection, if thumbnail was previously selected, such that 
                // drag and drop for multiple selected thumbnails is possible
                // otherwise, this event would select this thumbnail as only selection before dragging starts
                this.select(event, target);
            }
        },
        handleMouseUp(event, target) {
            metadataEditor.gallery.dragdrop.removeDragAmountIcon();
            if (metadataEditor.gallery.dragging) {
                metadataEditor.gallery.dragging = false;
            } else if (event.button !== 2 || target.closest(".thumbnail-parent").find(".selected").length === 0) {
                this.select(event, target);
            }
        },
        handleDragStart(event) {
            metadataEditor.gallery.dragging = true;
            metadataEditor.gallery.dragdrop.addDragAmountIcon(event);
        },
        select(event, target) {
            // get the logical tree node id of the thumbnail that is being selected
            let treeNodeId = target[0].dataset.logicaltreenodeid;
            if (event.metaKey || event.ctrlKey) {
                this.addManyToSelection([treeNodeId]);
                metadataEditor.gallery.select(target[0].dataset.order, target[0].dataset.stripe, "multi", event);
            } else if (event.shiftKey) {
                this.addSequenceToSelection(treeNodeId);
                metadataEditor.gallery.select(target[0].dataset.order, target[0].dataset.stripe, "range", event);
            } else {
                // single thumbnail is selected
                this.markManyAsSelected([treeNodeId]);
                metadataEditor.gallery.select(target[0].dataset.order, target[0].dataset.stripe, "default", event);
            }
            metadataEditor.pagination.markManyAsSelected(this.findSelectedTreeNodeIds());
            if (metadataEditor.physicalTree.isAvailable()) {
                let stripeTreeNodeId = treeNodeId.slice(0, treeNodeId.lastIndexOf("_"));
                metadataEditor.gallery.stripes.markOneSelected(stripeTreeNodeId);
                metadataEditor.logicalTree.markNodeAsSelected(stripeTreeNodeId);
                metadataEditor.physicalTree.markNodeAsSelected(treeNodeId);
            } else {
                metadataEditor.gallery.stripes.resetSelectionStyle();
                metadataEditor.logicalTree.markNodeAsSelected(treeNodeId);
            }
        },
        findThumbnailByTreeNodeId: function(treeNodeId) {
            return $("#imagePreviewForm .thumbnail-container[data-logicaltreenodeid=\"" + treeNodeId + "\"]").prev();
        },
        findOrderByTreeNodeId: function(treeNodeId) {
            let div = $("#imagePreviewForm .thumbnail-container[data-logicaltreenodeid=\"" + treeNodeId + "\"]");
            if (div.length > 0) {
                return div[0].dataset.order;
            }
            return null;
        },
        findTreeNodeIdByOrder: function(order) {
            let div = $("#imagePreviewForm .thumbnail-container[data-order=\"" + order + "\"]");
            if (div.length > 0) {
                return div[0].dataset.logicaltreenodeid;
            }
            return null;
        },
        findTreeNodeIdsByOrderList: function(orderList) {
            let treeNodeIds = [];
            for (let i = 0; i < orderList.length; i++) {
                treeNodeIds.push(this.findTreeNodeIdByOrder(orderList[i]));
            }
            return treeNodeIds;
        },
        findSelectedTreeNodeIds: function() {
            let treeNodeIds = [];
            $("#imagePreviewForm .thumbnail.selected").each(function () {
                treeNodeIds.push($(this).next()[0].dataset.logicaltreenodeid);
            });
            return treeNodeIds;
        },
        addSequenceToSelection: function(toTreeNodeId) {
            let fromThumbnail = $("#imagePreviewForm .thumbnail.last-selection");
            let fromTreeNodeId = fromThumbnail.next()[0].dataset.logicaltreenodeid;
            let toThumbnail = this.findThumbnailByTreeNodeId(toTreeNodeId);
            // check whether both are from the same stripe
            let fromStripeTreeNodeId = fromTreeNodeId.slice(0, fromTreeNodeId.lastIndexOf("_"));
            let toStripeTreeNodeId = toTreeNodeId.slice(0, toTreeNodeId.lastIndexOf("_"));
            let sameStripe = fromStripeTreeNodeId == toStripeTreeNodeId;
            let selectionClasses = sameStripe ? "selected" : "selected discontinuous";
            // iterate over all thumbnails in the order they appear in the dom
            let inBetweenSelectedThumbnails = false;
            $("#imagePreviewForm .thumbnail").each(function () {
                let currentThumbnail = $(this);
                if (inBetweenSelectedThumbnails) {
                    currentThumbnail.addClass(selectionClasses);
                }
                if (!inBetweenSelectedThumbnails && (currentThumbnail.is(fromThumbnail) || currentThumbnail.is(toThumbnail))) {
                    inBetweenSelectedThumbnails = true;
                    // continue iterating through thumbnails
                    return true;
                }
                if (inBetweenSelectedThumbnails && (currentThumbnail.is(fromThumbnail) || currentThumbnail.is(toThumbnail))) {
                    inBetweenSelectedThumbnails = false;
                    // stop iterating through thumbnails
                    return false;
                }
            });
            // update selection status on previously selected thumbnail
            fromThumbnail.removeClass("last-selection");
            fromThumbnail.addClass(selectionClasses);
            // update selection status for currently selected thumbnail
            toThumbnail.addClass(selectionClasses + " last-selection");
        },
        markManyAsSelected: function(treeNodeIds) {
            this.resetSelectionStyle();
            this.addManyToSelection(treeNodeIds);
        },
        addManyToSelection: function(treeNodeIds) {
            // keep previous selection but remove last-selection class
            $("#imagePreviewForm .thumbnail.last-selection").removeClass("last-selection");
            
            // find gallery thumbnail corresponding to each treeNodeId and add selected status
            let thumbnail = null;
            for(let i = 0; i < treeNodeIds.length; i++) {
                thumbnail = this.findThumbnailByTreeNodeId(treeNodeIds[i]);
                thumbnail.addClass("selected");
            }
            if (thumbnail !== null) {
                thumbnail.addClass("last-selection");
            }

            // check whether all selected thumbnails are from the same stripe 
            let stripeTreeNodeIdSet = new Set();
            let treeNodeLastNumbers = [];
            $("#imagePreviewForm .thumbnail.selected").each(function () {
                let treeNodeId = $(this).next()[0].dataset.logicaltreenodeid;
                let stripeTreeNodeId = treeNodeId.slice(0, treeNodeId.lastIndexOf("_"));
                let treeNodeLastNumber = treeNodeId.slice(treeNodeId.lastIndexOf("_") + 1);
                stripeTreeNodeIdSet.add(stripeTreeNodeId);
                treeNodeLastNumbers.push(treeNodeLastNumber);
            });
            let minNumber = Math.min.apply(null, treeNodeLastNumbers);
            let maxNumber = Math.max.apply(null, treeNodeLastNumbers);
            
            // update discontinuous style class
            $("#imagePreviewForm .thumbnail.discontinuous").removeClass("discontinuous");
            if (stripeTreeNodeIdSet.size > 1 || (maxNumber - minNumber != treeNodeLastNumbers.length - 1)) {
                // selection is not continuous
                $("#imagePreviewForm .thumbnail.selected").addClass("discontinuous");
            }
        },
        resetSelectionStyle: function() {
            // remove current selection styling from thumbnails
            $("#imagePreviewForm .thumbnail.discontinuous").removeClass("discontinuous");
            $("#imagePreviewForm .thumbnail.selected").removeClass("selected");
            $("#imagePreviewForm .thumbnail.last-selection").removeClass("last-selection");
        }
    },
    stripes: {
        handleMouseDown(event) {
            if (!$(event.target).hasClass("selected")) {
                // retrieve logical tree node id for stripe that is being clicked on
                let stripeTreeNodeId = $(event.target)[0].dataset.logicaltreenodeid;
                // mark corresponding node in logical tree as selected
                metadataEditor.logicalTree.markNodeAsSelected(stripeTreeNodeId);
                // reset selection of other panels
                metadataEditor.gallery.pages.resetSelectionStyle();
                metadataEditor.pagination.resetSelectionStyle();
                // mark gallery stripe as selected
                this.markOneSelected(stripeTreeNodeId)
                if (metadataEditor.physicalTree.isAvailable()) {
                    // mark first thumbnail as selected 
                    let treeNodeId = this.findFirstThumbnailLogicalTreeNodeId(stripeTreeNodeId);
                    metadataEditor.physicalTree.markNodeAsSelected(treeNodeId);
                    metadataEditor.gallery.pages.markManyAsSelected([treeNodeId]);
                    metadataEditor.pagination.markManyAsSelected([treeNodeId]);
                }
                // send new selection to backend
                metadataEditor.gallery.select(null, event.target.dataset.stripe, "default", event);
            }
        },
        findFirstThumbnailLogicalTreeNodeId: function (stripeTreeNodeId) {
            // let stripe = $("#imagePreviewForm .stripe[data-logicaltreenodeid=\"" + stripeTreeNodeId + "\"]");
            let firstTreeNodeId = null;
            $("#imagePreviewForm .thumbnail-container").each(function() {
                let treeNodeId = this.dataset.logicaltreenodeid;
                let currentStripeTreeNodeId = treeNodeId.slice(0, treeNodeId.lastIndexOf("_"));
                if (currentStripeTreeNodeId == stripeTreeNodeId) {
                    firstTreeNodeId = treeNodeId;
                    return false;
                }
                return true;
            });
            return firstTreeNodeId;
        },
        markOneSelected: function(stripeTreeNodeId) {
            this.resetSelectionStyle();
            let stripe = $("#imagePreviewForm .stripe[data-logicaltreenodeid=\"" + stripeTreeNodeId + "\"]");
            stripe.addClass("selected");
        },
        resetSelectionStyle: function() {
            // remove current selection styling from gallery stripes
            $("#imagePreviewForm .stripe.selected").removeClass("selected");
        },
    },
    dragdrop: {
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
    },
    select(pageIndex, stripeIndex, selectionType, triggerEvent = null) {
        // call the remoteCommand in gallery.xhtml
        if (triggerEvent == null) {
            sendGallerySelect([
                {name: "page", value: pageIndex},
                {name: "stripe", value: stripeIndex},
                {name: "selectionType", value: selectionType}
            ]);
        } else {
            sendGallerySelect([
                {name: "page", value: pageIndex},
                {name: "stripe", value: stripeIndex},
                {name: "selectionType", value: selectionType},
                {name: "pageX", value: triggerEvent.pageX},
                {name: "pageY", value: triggerEvent.pageY},
                {name: "triggerContextMenu", value: triggerEvent.button === 2}
            ]);
        }
    },
};

metadataEditor.logicalTree = {
    onNodeClick: function (node, event) {
        let treeNodeId = node.attr("id").split(":")[1];
        let isPage = node.hasClass("ui-treenode-leaf") 
            && node.find("> .ui-treenode-content > .ui-icon-document").length > 0;
        if (isPage) {
            metadataEditor.gallery.stripes.resetSelectionStyle();
            metadataEditor.gallery.pages.markManyAsSelected([treeNodeId]);
            metadataEditor.pagination.markManyAsSelected([treeNodeId]);
        } else {
            metadataEditor.gallery.pages.resetSelectionStyle();
            metadataEditor.pagination.resetSelectionStyle();
            metadataEditor.gallery.stripes.markOneSelected(treeNodeId);
            if (metadataEditor.physicalTree.isAvailable()) {
                let firstTreeNodeId = metadataEditor.gallery.stripes.findFirstThumbnailLogicalTreeNodeId(treeNodeId);
                if (firstTreeNodeId !== null) {
                    metadataEditor.pagination.markManyAsSelected([firstTreeNodeId]);
                    metadataEditor.gallery.pages.markManyAsSelected([firstTreeNodeId]);
                    metadataEditor.physicalTree.markNodeAsSelected(firstTreeNodeId);
                }
            }
        }
    },
    resetSelectionStyle: function() {
        // make all tree nodes not selected
        let nodes = $("#logicalTree .ui-treenode.ui-treenode-selected");
        nodes.removeClass("ui-treenode-selected").addClass("ui-treenode-unselected");
        nodes.attr("aria-selected", "false");
        $("#logicalTree .ui-treenode-label.ui-state-highlight").removeClass("ui-state-highlight");
        $("#logicalTree .ui-treenode-label.ui-treenode-outline").removeClass("ui-treenode-outline");
    },
    markNodeAsSelected: function(treeNodeId) {
        this.resetSelectionStyle();
        this.styleNodeAsSelected($("#logicalTree\\:" + treeNodeId));
    },
    styleNodeAsSelected: function(node) {
        let label = node.find("> .ui-treenode-content > .ui-treenode-label");
        node.attr("aria-selected", "true");
        node.removeClass("ui-treenode-unselected").addClass("ui-treenode-selected");
        label.addClass("ui-state-highlight ui-treenode-outline");
    }
};

metadataEditor.physicalTree = {
    isAvailable: function() {
        return $("#physicalTree").length > 0;
    },
    onNodeClick: function (node, event) {
        let order = node.find("> .ui-treenode-content span[data-order]")[0].dataset.order;
        let treeNodeId = metadataEditor.gallery.pages.findTreeNodeIdByOrder(order);
        if (treeNodeId !== null) {
            let isPage = node.find("> .ui-treenode-content > .ui-icon-document").length > 0;
            if (isPage) {
                let stripeTreeNodeId = treeNodeId.slice(0, treeNodeId.lastIndexOf("_"));
                metadataEditor.logicalTree.markNodeAsSelected(stripeTreeNodeId);
                metadataEditor.pagination.markManyAsSelected([treeNodeId]);
                metadataEditor.gallery.stripes.markOneSelected(stripeTreeNodeId);
                metadataEditor.gallery.pages.markManyAsSelected([treeNodeId]);
            }
        }
    },
    resetSelectionStyle: function() {
        // make all tree nodes not selected
        let nodes = $("#physicalTree .ui-treenode.ui-treenode-selected");
        nodes.removeClass("ui-treenode-selected").addClass("ui-treenode-unselected");
        nodes.attr("aria-selected", "false");
        $("#physicalTree .ui-treenode-label.ui-state-highlight").removeClass("ui-state-highlight");
        $("#physicalTree .ui-treenode-label.ui-treenode-outline").removeClass("ui-treenode-outline");
    },
    markNodeAsSelected: function(treeNodeId) {
        this.resetSelectionStyle();
        let order = metadataEditor.gallery.pages.findOrderByTreeNodeId(treeNodeId);
        if (order !== null) {
            let span = $("#physicalTree span[data-order=\"" + order + "\"]");
            let node = span.closest(".ui-treenode");
            this.styleNodeAsSelected(node);
        }
    },
    styleNodeAsSelected: function(node) {
        let label = node.find("> .ui-treenode-content > .ui-treenode-label");
        node.attr("aria-selected", "true");
        node.removeClass("ui-treenode-unselected").addClass("ui-treenode-selected");
        label.addClass("ui-state-highlight ui-treenode-outline");
    }
}

metadataEditor.pagination = {
    onChange: function(event) {
        let selectedOrder = [];
        for(let i = 0; i < event.target.length; i++) {
            if ($(event.target[i]).prop("selected")) {
                selectedOrder.push(event.target[i].index + 1);
            }
        }
        let treeNodeIds = metadataEditor.gallery.pages.findTreeNodeIdsByOrderList(selectedOrder);
        metadataEditor.gallery.pages.markManyAsSelected(treeNodeIds);
        if (treeNodeIds.length > 0) {
            let lastTreeNodeId = treeNodeIds[treeNodeIds.length - 1];
            metadataEditor.logicalTree.markNodeAsSelected(lastTreeNodeId);
            metadataEditor.physicalTree.markNodeAsSelected(lastTreeNodeId);
        }
    },
    resetSelectionStyle: function() {
        $("#paginationForm\\:paginationSelection .ui-state-highlight").removeClass("ui-state-highlight");
        $("#paginationForm\\:paginationSelection .ui-icon-check").removeClass("ui-icon-check").addClass("ui-icon-blank");
        $("#paginationForm\\:paginationSelection .ui-state-active").removeClass("ui-state-active");
        $("#paginationForm\\:paginationSelection select option").removeAttr("selected");
        $("#paginationForm\\:paginationSelection select option").prop("selected", false);
    },
    markManyAsSelected: function(treeNodeIds) {
        this.resetSelectionStyle();
        for (let i = 0; i < treeNodeIds.length; i++) {
            // find treeNode in gallery view
            let thumbnailContainer = metadataEditor.gallery.pages.findThumbnailByTreeNodeId(treeNodeIds[i]).next();
            if (thumbnailContainer.length > 0) {
                let order = thumbnailContainer[0].dataset.order;
                // make checkbox checked
                let selectManyMenu = $("#paginationForm\\:paginationSelection");
                let selectListBox = selectManyMenu.find(".ui-selectlistbox-list");
                let selectItem = selectListBox.children().eq(order - 1);
                selectItem.addClass("ui-state-highlight");
                selectItem.find(".ui-chkbox-box").addClass("ui-state-active");
                selectItem.find(".ui-chkbox-icon").removeClass("ui-icon-blank").addClass("ui-icon-check");
                // mark invisible select
                let options = $("#paginationForm\\:paginationSelection select option");
                options.eq(order - 1).attr("selected", "selected");
                options.eq(order - 1).prop("selected", true);
            }
        }        
    }
}

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
            metadataEditor.gallery.select(selectableThumbnails[newIndex].dataset.order, selectableThumbnails[newIndex].dataset.stripe, "default");
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
