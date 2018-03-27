/*
 * (c) Kitodo. Key to digital objects e. V. <contact@kitodo.org>
 *
 * This file is part of the Kitodo project.
 *
 * It is licensed under GNU General Public License version 3 or later.
 *
 * For the full copyright and license information, please read the
 * GPL3-License.txt file that was distributed with this source code.
 */

var DELAY_BETWEEN_ROW = 300
var SAVED_MOUSE_LOCATIONS = 2;

var menu = $("#menu");
var activeSubmenu = null;

var timeout = null;
var lastDelayLocation = null;

var tempMouseLocations = [];

// save mouse locations to detect mouse movement direction
var mousemoveDocument = function(e) {
    tempMouseLocations.push({x: e.pageX, y: e.pageY});

    if (tempMouseLocations.length > SAVED_MOUSE_LOCATIONS) {
        tempMouseLocations.shift();
    }
};

// remove menu-active class if mouse leaves the menu
var mouseleaveMenu = function() {
    $(".menu-active").removeClass("menu-active");
}

// mouse hovers button to activate menu
var mouseenterMenu = function() {
    if (timeout) {
        // reset timeout
        clearTimeout(timeout);
    }
    checkActivation(this);
}

// activate submenu
var activateSubmenu = function(submenu) {
    activeSubmenu = submenu;
    $(".menu-active").removeClass("menu-active");
    $(submenu).find("ul").addClass("menu-active");
};

var checkActivation = function(submenu) {
    var delay = activationDelay();

    if (delay) {
        timeout = setTimeout(function() {
            checkActivation(submenu);
        }, delay);
    } else {
        activateSubmenu(submenu);
    }
};

// return activation delay
var activationDelay = function() {
    // menu was not activated before
    if (!activeSubmenu || !$(activeSubmenu).is("*")) {
        return 0;
    }

    var menuOffset = menu.offset(),
        upperLeft = {
            x: menuOffset.left,
            y: menuOffset.top
        },
        lowerRight = {
            x: menuOffset.left + menu.outerWidth(),
            y: menuOffset.top + menu.outerHeight()
        },
        lastLocation = tempMouseLocations[tempMouseLocations.length - 1],
        location = tempMouseLocations[0];

    // mouse was out of menu
    if (location.y < menuOffset.top || location.y > lowerRight.y ||
        location.x < menuOffset.left || location.x > lowerRight.x) {
        return 0;
    }

    // mouse moves to submenu direction but the user tries to open next submenu
    function slope(a, b) {
        return (b.y - a.y) / (b.x - a.x);
    };

    decreasingCorner = lowerRight;
    increasingCorner = upperLeft;

    var decreasingSlope = slope(lastLocation, decreasingCorner),
        increasingSlope = slope(lastLocation, increasingCorner),
        prevDecreasingSlope = slope(location, decreasingCorner),
        prevIncreasingSlope = slope(location, increasingCorner);

    if (decreasingSlope < prevDecreasingSlope &&
            increasingSlope > prevIncreasingSlope) {
        lastDelayLocation = lastLocation;
        return DELAY_BETWEEN_ROW;
    }

    lastDelayLocation = null;
    return 0;
};

// init menu events
menu.mouseleave(mouseleaveMenu)
    .find("> li")
    .mouseenter(mouseenterMenu)

$(document).mousemove(mousemoveDocument);