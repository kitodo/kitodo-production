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

var scrollUp = function (elementID, triggerCompleteFunction) {
    var scrollableContent = $(elementID);
    if (triggerCompleteFunction) {
        scrollableContent.animate({
            scrollTop: scrollableContent.scrollTop() - SCROLL_SPEED
        }, 90, null, checkScrollPosition(scrollableContent))
    } else {
        scrollableContent.animate({
            scrollTop: scrollableContent.scrollTop() - SCROLL_SPEED
        }, 90, null, null)
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

function initialize() {
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

function destruct() {
    $(document).off("mouseenter.scrollGallery");
    $(document).off("mouseleave.scrollGallery");
}

$(document).ready(function () {
    if ($("#thumbnailStripeScrollableContent")[0] != null) {
        initialize();
    }
    if ($("#structureTreeForm\\:structurePanel").length) {
        initializeStructureTreeScrolling();
    }
});
