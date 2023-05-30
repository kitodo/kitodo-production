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
// jshint unused:false

const SCROLL_SPEED = 40;
var interval;
var structureInterval;

function atTop(scrollableContent) {
    return scrollableContent.scrollTop() <= 0
}

function atBottom(scrollableContent) {
    if (scrollableContent.length) {
        return scrollableContent.scrollTop() + scrollableContent.height() >= scrollableContent[0].scrollHeight;
    } else {
        return true;
    }
}

function disableUpButton() {
    $("#imagePreviewForm\\:scroll-up").addClass("disabled");
}

function disableDownButton() {
    $("#imagePreviewForm\\:scroll-down").addClass("disabled");
}

function enableUpButton() {
    $("#imagePreviewForm\\:scroll-up").removeClass("disabled");
}

function enableDownButton() {
    $("#imagePreviewForm\\:scroll-down").removeClass("disabled");
}

function checkScrollPosition(scrollableContent) {
    if (scrollableContent) {
        if (atTop(scrollableContent)) {
            disableUpButton();
        } else {
            enableUpButton();
        }

        if (atBottom(scrollableContent)) {
            disableDownButton();
        } else {
            enableDownButton();
        }
    }
}

var scrollUp = function (elementID, triggerCompleteFunction) {
    var scrollableContent = $(elementID);
    if (triggerCompleteFunction) {
        scrollableContent.animate({
            scrollTop: scrollableContent.scrollTop() - SCROLL_SPEED
        }, 90, null, checkScrollPosition(scrollableContent));
    } else {
        scrollableContent.animate({
            scrollTop: scrollableContent.scrollTop() - SCROLL_SPEED
        }, 90, null, null);
    }
};

var scrollDown = function (elementID, triggerCompleteFunction) {
    var scrollableContent = $(elementID);
    if (triggerCompleteFunction) {
        scrollableContent.animate({
            scrollTop: scrollableContent.scrollTop() + SCROLL_SPEED
        }, 90, null, checkScrollPosition(scrollableContent));
    } else {
        scrollableContent.animate({
            scrollTop: scrollableContent.scrollTop() + SCROLL_SPEED
        }, 90, null, null);
    }
};

function destruct() {
    $(document).off("mouseenter.scrollGallery");
    $(document).off("mouseleave.scrollGallery");
}

function initialize() {
    // make sure that event handlers are only registered once
    destruct();

    checkScrollPosition($("#thumbnailStripeScrollableContent"));

    $(document).on("mouseenter.scrollGallery", ".scroll-button", function (e) {
        if (e.target.id === "imagePreviewForm:scroll-up") {
            interval = window.setInterval(function() {
                scrollUp("#thumbnailStripeScrollableContent", true);
            }, 100);
        } else {
            interval = window.setInterval(function() {
                scrollDown("#thumbnailStripeScrollableContent", true);
            }, 100);
        }
    });
    $(document).on("mouseleave.scrollGallery", ".scroll-button", function () {
        window.clearInterval(interval);
    });
}

function initializeStructureSpecificScrolling(structureIdentifier) {
    $(document).on("dragstart.structureTree", structureIdentifier, function(e) {
        $(structureIdentifier + " .scroll-up-area").css("display", "block");
        $(structureIdentifier + " .scroll-down-area").css("display", "block");
    });

    $(document).on("mouseenter.scrollUpArea", structureIdentifier + " .scroll-up-area", function (e) {
        if (e.originalEvent.buttons === 1 && $(".ui-tree-draghelper.ui-draggable-dragging").length) {
            structureInterval = window.setInterval(function() {
                scrollUp(structureIdentifier + " .scroll-wrapper", false);
            }, 100);
            $(this).css("opacity", ".2");
        }
    });

    $(document).on("mouseenter.scrollDownArea", structureIdentifier + " .scroll-down-area", function (e) {
        if (e.originalEvent.buttons === 1 && $(".ui-tree-draghelper.ui-draggable-dragging").length) {
            structureInterval = window.setInterval(function() {
                scrollDown(structureIdentifier + " .scroll-wrapper", false);
            }, 100);
            $(this).css("opacity", ".2");
        }
    });

    $(document).on("mouseleave.scrollUpArea", ".scroll-up-area", function () {
        window.clearInterval(structureInterval);
        $(this).css("opacity", "0");
    });

    $(document).on("mouseleave.scrollDownArea", ".scroll-down-area", function () {
        window.clearInterval(structureInterval);
        $(this).css("opacity", "0");
    });
}

function initializeStructureTreeScrolling() {
    $(document).on("mouseup", function() {
        $(".scroll-up-area").css("display", "none");
        $(".scroll-down-area").css("display", "none");
    });

    initializeStructureSpecificScrolling("#logicalStructure");
    initializeStructureSpecificScrolling("#physicalStructure");
}

function scrollToPreviewThumbnail(thumbnail, scrollable) {
    let thumbnailHeight = thumbnail.closest(".thumbnail-parent").height();
    let selectedIndex = scrollable.find(".thumbnail + .thumbnail-container").index(thumbnail);
    if (selectedIndex >= 0) {
        scrollable.animate({
            scrollTop: selectedIndex * thumbnailHeight - (scrollable.height()/2 - thumbnailHeight/2)
        }, 180, null, null);
    }
}

function scrollToSelectedPreviewThumbnail() {
    let scrollableContent = $("#thumbnailStripeScrollableContent");
    if (scrollableContent.length) {
        let selectedThumbnail = scrollableContent.find(".selected.last-selection + .thumbnail-container");
        if (selectedThumbnail.length) {
            scrollToPreviewThumbnail(selectedThumbnail.first(), scrollableContent);
        }
    }
}

function scrollToStructureThumbnail(thumbnail, scrollable) {
    let mediaPosition = thumbnail.closest(".media-position");
    scrollable.animate({
        scrollTop: mediaPosition[0].offsetTop
    }, 180, null, null);
}

function scrollToSelectedStripe(selectedStripe, scrollable) {
    scrollable.animate({
        scrollTop: selectedStripe[0].offsetTop
    }, 180, null, null);
}

function scrollToSelectedStructureThumbnail() {
    let scrollableContent = $("#imagePreviewForm\\:structuredPagesField");
    if (scrollableContent.length) {
        let selectedThumbnail = scrollableContent.find(".selected.last-selection");
        let selectedStripe = scrollableContent.find(".selected.stripe");
        if (selectedThumbnail.length) {
            scrollToStructureThumbnail(selectedThumbnail.first(), scrollableContent);
        } else if (selectedStripe.length) {
            scrollToSelectedStripe(selectedStripe.first(), scrollableContent);
        }
    }
}

function scrollToSelectedThumbnail() {
    scrollToSelectedStructureThumbnail();
    scrollToSelectedPreviewThumbnail();
}

function scrollToSelectedTreeNode() {
    let logicalStructure = $("#logicalStructure .scroll-wrapper");
    let physicalStructure = $("#physicalStructure .scroll-wrapper");
    let selectedLogicalNode = logicalStructure.find(".ui-treenode-selected");
    let selectedPhysicalNode = physicalStructure.find(".ui-treenode-selected");
    if (selectedLogicalNode.length === 1 && logicalStructure.length) {
        logicalStructure.animate({
            scrollTop: selectedLogicalNode.position().top - logicalStructure.height()/2
        }, 180, null, null);
    }
    if (selectedPhysicalNode.length === 1 && physicalStructure.length) {
        physicalStructure.animate({
            scrollTop: selectedPhysicalNode.position().top - physicalStructure.height()/2
        }, 180, null, null);
    }
}

function scrollToSelectedPaginationRow() {
    let container = $("#paginationForm\\:paginationSelection .ui-selectlistbox-listcontainer");
    let scrollTo = container.find("li.ui-state-highlight");
    if (scrollTo.length === 1) {
        container.animate({
            scrollTop: scrollTo.offset().top + container.scrollTop() - container.offset().top - container.height()/2
        }, 180, null, null);
    }
}


$(document).ready(function () {
    if ($("#thumbnailStripeScrollableContent")[0] != null) {
        initialize();
    }
    if ($("#structurePanel").length) {
        initializeStructureTreeScrolling();
    }
});
