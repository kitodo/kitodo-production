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

function initializeStructureTreeScrolling() {

    $(document).on("mousemove.structureTreeForm", "#structureTreeForm", function(e) {
        if (e.originalEvent.buttons === 1 && $(".ui-tree-draghelper.ui-draggable-dragging").length) {
            $("#scrollUpArea").css("display", "block");
            $("#scrollDownArea").css("display", "block");
        }
    });

    $(document).on("mouseup", function() {
        $("#scrollUpArea").css("display", "none");
        $("#scrollDownArea").css("display", "none");
    });

    $(document).on("mouseenter.scrollUpArea", "#scrollUpArea", function (e) {
        if (e.originalEvent.buttons === 1 && $(".ui-tree-draghelper.ui-draggable-dragging").length) {
            structureInterval = window.setInterval(function() {
                scrollUp("#structureTreeForm\\:structurePanel", false);
            }, 100);
            $(this).css("opacity", ".2");
        }
    });

    $(document).on("mouseenter.scrollDownArea", "#scrollDownArea", function (e) {
        if (e.originalEvent.buttons === 1 && $(".ui-tree-draghelper.ui-draggable-dragging").length) {
            structureInterval = window.setInterval(function() {
                scrollDown("#structureTreeForm\\:structurePanel", false);
            }, 100);
            $(this).css("opacity", ".2");
        }
    });

    $(document).on("mouseleave.scrollUpArea", "#scrollUpArea", function () {
        window.clearInterval(structureInterval);
        $(this).css("opacity", "0");
    });

    $(document).on("mouseleave.scrollDownArea", "#scrollDownArea", function () {
        window.clearInterval(structureInterval);
        $(this).css("opacity", "0");
    });
}

function scrollToSelectedPreviewThumbnail() {
    var scrollableContent = $("#thumbnailStripeScrollableContent");
    if (scrollableContent.length) {
        var selectedThumbnail = scrollableContent.find(".active.last-selection");
        if (selectedThumbnail.length) {
            var thumbnailHeight = selectedThumbnail.first().parent().parent().height();
            var selectedIndex = scrollableContent.find(".thumbnail").index(selectedThumbnail);
            if (selectedIndex >= 0) {
                scrollableContent.animate({
                    scrollTop: selectedIndex * thumbnailHeight - (scrollableContent.height()/2 - thumbnailHeight/2)
                }, 180, null, null);
            }
        }
    }
}

function scrollToSelectedStructureThumbnail() {
    let scrollableContent = $("#imagePreviewForm\\:structuredPagesField");
    if (scrollableContent.length) {
        let selectedThumbnail = scrollableContent.find(".active.last-selection");
        if (selectedThumbnail.length) {
            let mediaPosition = selectedThumbnail.first().closest(".media-position");
            scrollableContent.animate({
                scrollTop: mediaPosition[0].offsetTop
            }, 180, null, null);
        } else {
            let selectedStripe = scrollableContent.find(".selected.stripe");
            if (selectedStripe.length) {
                scrollableContent.animate({
                    scrollTop: selectedStripe[0].offsetTop
                }, 180, null, null);
            }
        }
    }
}

function scrollToSelectedThumbnail() {
    scrollToSelectedStructureThumbnail();
    scrollToSelectedPreviewThumbnail();
}

function scrollToSelectedTreeNode() {
    let selectedTreeNode = $(".ui-treenode-selected");
    let structureTree = $("#structureTreeForm\\:structurePanel");
    if (selectedTreeNode.length === 1 && structureTree.length) {
        structureTree.animate({
            scrollTop: selectedTreeNode.position().top - structureTree.height()/2
        }, 180, null, null);
    }
}

$(document).ready(function () {
    if ($("#thumbnailStripeScrollableContent")[0] != null) {
        initialize();
    }
    if ($("#structureTreeForm\\:structurePanel").length) {
        initializeStructureTreeScrolling();
    }
});
