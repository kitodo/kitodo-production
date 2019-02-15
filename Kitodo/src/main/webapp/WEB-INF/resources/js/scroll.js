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
var scrollUp = function () {
    var scrollableContent = $('#thumbnailStripeScrollableContent');
    scrollableContent.animate({
        scrollTop: scrollableContent.scrollTop() - SCROLL_SPEED
    }, 90, null, checkScrollPosition())
};
var scrollDown = function () {
    var scrollableContent = $('#thumbnailStripeScrollableContent');
    scrollableContent.animate({
        scrollTop: scrollableContent.scrollTop() + SCROLL_SPEED
    }, 90, null, checkScrollPosition())
};

function checkScrollPosition() {
    var scrollableContent = $('#thumbnailStripeScrollableContent');
    if (atTop(scrollableContent) && atBottom(scrollableContent)) {
        disableUpButton();
        disableDownButton();
    } else if (atTop(scrollableContent)) {
        disableUpButton();
        enableDownButton();
    } else if (atBottom(scrollableContent)) {
        disableDownButton();
        enableUpButton();
    } else {
        enableUpButton();
        enableDownButton();
    }
}

function atTop(scrollableContent) {
    return scrollableContent.scrollTop() <= 0
}

function atBottom(scrollableContent) {
    return scrollableContent.scrollTop() + scrollableContent.height() >= scrollableContent[0].scrollHeight;
}

function disableUpButton() {
    $('#imagePreviewForm\\:scroll-up').addClass('disabled');
}

function disableDownButton() {
    $('#imagePreviewForm\\:scroll-down').addClass('disabled');
}

function enableUpButton() {
    $('#imagePreviewForm\\:scroll-up').removeClass('disabled');
}

function enableDownButton() {
    $('#imagePreviewForm\\:scroll-down').removeClass('disabled');
}

function initialize() {
    checkScrollPosition();

    $(document).on('mouseenter.scrollGallery', '.scroll-button', function (e) {
        console.log("event");
        if (e.target.id === "imagePreviewForm:scroll-up") {
            interval = window.setInterval(scrollUp, 100);
        } else {
            interval = window.setInterval(scrollDown, 100);
        }
    });
    $(document).on('mouseleave.scrollGallery', '.scroll-button', function () {
        window.clearInterval(interval);
    });
}

function destruct() {
    $(document).off('mouseenter.scrollGallery');
    $(document).off('mouseleave.scrollGallery');
}

$(document).ready(function () {
    if ($('#thumbnailStripeScrollableContent')[0] != null) {
        initialize();
    }
});
