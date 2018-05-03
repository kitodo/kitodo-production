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

var SEPARATOR_WIDTH = 14.5;
var COLLAPSED_COL_WIDTH = 42;
var dragging = false;
var target;

var wrapper;
var wrapperPositionX;
var firstColumn = $('#firstColumnWrapper');
var secondColumn = $('#secondColumnWrapper');
var thirdColumn = $('#thirdColumnWrapper');
var collapsedColumns;
var firstResizer;
var secondResizer;


$(document).ready(function() {
    $('#firstResizer').mousedown(function(e) {handleMouseDown(e)});
    $('#secondResizer').mousedown(function(e) {handleMouseDown(e)});
    setSizes();
});

$(document).mouseup(function(e) {
    if (dragging) {
        $(document).unbind('mousemove');
        dragging = false;
    }
});

function handleMouseDown(e) {
    e.preventDefault();
    dragging = true;
    target = e.target;
    getElements();

    $(document).mousemove(function(e) {
        if (target.id == 'firstResizer') {
            if (secondColumn.hasClass('collapsed')) {
                resizeFirstAndThird(e);
            } else {
                resizeFirstAndSecond(e);
            }
        } else {
            if (secondColumn.hasClass('collapsed')) {
                resizeFirstAndThird(e);
            } else {
                resizeSecondAndThird(e);
            }
        }
    });

}

function resizeFirstAndSecond(e) {
    if (e.pageX >= wrapperPositionX + firstColumn.data('min-width')
        && e.pageX <= wrapperPositionX + wrapper.width() - secondColumn.data('min-width') - SEPARATOR_WIDTH - thirdColumn.width()) {
        firstColumn.width(e.pageX - firstColumn.offset().left);
        secondColumn.width(wrapperPositionX + wrapper.width() - thirdColumn.width() - 2 * SEPARATOR_WIDTH - e.pageX);
        // TODO save width
    }
}

function resizeFirstAndThird(e) {
    if (e.pageX >= wrapperPositionX + firstColumn.data('min-width')
        && e.pageX <= wrapperPositionX + wrapper.width() - thirdColumn.data('min-width')) {
        firstColumn.width(e.pageX - firstColumn.offset().left);
        thirdColumn.width(wrapperPositionX + wrapper.width() - 2 * SEPARATOR_WIDTH - secondColumn.width() - e.pageX);
        // TODO save width
    }
}

function resizeSecondAndThird(e) {
    if (e.pageX >= wrapperPositionX + firstColumn.width() + SEPARATOR_WIDTH + secondColumn.data('min-width')
        && e.pageX <= wrapperPositionX + wrapper.width() - SEPARATOR_WIDTH - thirdColumn.data('min-width')) {
        secondColumn.width(e.pageX - secondColumn.offset().left);
        thirdColumn.width(wrapperPositionX + wrapper.width() - SEPARATOR_WIDTH - e.pageX);
        // TODO save width
    }
}

function getElements() {
    wrapper = $('#metadataEditorWrapper');
    wrapperPositionX = wrapper.offset().left;
    collapsedColumns = $('#metadataEditorWrapper .collapsed').length;
    var resizers = $('.resizer');
    firstResizer = resizers.first();
    secondResizer = resizers.last();
}

function setSizes() {
    getElements();

    firstColumn.width(firstColumn.data('min-width'));
    secondColumn.width(secondColumn.data('min-width'));
    thirdColumn.width(wrapper.width() - firstColumn.data('min-width') - secondColumn.data('min-width') - 2 * SEPARATOR_WIDTH);
}

function toggleResizers() {
    console.log(collapsedColumns);
    console.log(thirdColumn.hasClass('collapsed'));
    if (collapsedColumns >= 2) {
        $('.resizer').addClass('disabled');
    } else if (collapsedColumns > 0) {
        if (firstColumn.hasClass('collapsed')) {
            firstResizer.addClass('disabled');
            secondResizer.removeClass('disabled');
        } else if (thirdColumn.hasClass('collapsed')) {
            firstResizer.removeClass('disabled');
            secondResizer.addClass('disabled');
        }
    } else {
        firstResizer.removeClass('disabled');
        secondResizer.removeClass('disabled');
    }
}

function toggleCollapseButtons() {
    var firstButton = $('#firstColumnWrapper .columnExpandButton');
    var secondButton = $('#secondColumnWrapper .columnExpandButton');
    var thirdButton = $('#thirdColumnWrapper .columnExpandButton');

    if (collapsedColumns >= 2) {
        if (!firstColumn.hasClass('collapsed')) {
            firstButton.prop('disabled', true);
        } else if (!secondColumn.hasClass('collapsed')) {
            secondButton.prop('disabled', true);
        } else {
            thirdButton.prop('disabled', true);
        }
    } else {
        firstButton.prop('disabled', false);
        secondButton.prop('disabled', false);
        thirdButton.prop('disabled', false);
    }
}

function toggleFirstColumn() {
    firstColumn.toggleClass('collapsed');
    getElements();
    toggleResizers();
    toggleCollapseButtons();

    if (firstColumn.hasClass('collapsed')) {
        firstColumn.animate({width: COLLAPSED_COL_WIDTH});
        if (secondColumn.hasClass('collapsed')) {
            thirdColumn.animate({width: wrapper.width() - 2 * COLLAPSED_COL_WIDTH - 2 * SEPARATOR_WIDTH});
        } else {
            secondColumn.animate({width:  wrapper.width() - COLLAPSED_COL_WIDTH - thirdColumn.width() - 2 * SEPARATOR_WIDTH});
        }
    } else {
        var neededWidth = firstColumn.data('min-width') - (secondColumn.width() - secondColumn.data('min-width'));
        if (secondColumn.hasClass('collapsed')) {
            thirdColumn.animate({width: wrapper.width() - firstColumn.data('min-width') - COLLAPSED_COL_WIDTH - 2 * SEPARATOR_WIDTH});
            firstColumn.animate({width: firstColumn.data('min-width')});
        } else if (neededWidth > 0) {
            secondColumn.animate({width: secondColumn.data('min-width')});
            thirdColumn.animate({width: thirdColumn.width() - neededWidth});
            firstColumn.animate({width: firstColumn.data('min-width')});
        } else {
            secondColumn.animate({width: wrapper.width() - firstColumn.data('min-width') - thirdColumn.width() - 2 * SEPARATOR_WIDTH});
            firstColumn.animate({width: firstColumn.data('min-width')});
        }

    }
}

function toggleSecondColumn() {
    secondColumn.toggleClass('collapsed');
    getElements();
    toggleResizers();
    toggleCollapseButtons();

    if (secondColumn.hasClass('collapsed')) {
        secondColumn.animate({width: COLLAPSED_COL_WIDTH});
        if (thirdColumn.hasClass('collapsed')) {
            firstColumn.animate({width: wrapper.width() - 2 * COLLAPSED_COL_WIDTH - 2 * SEPARATOR_WIDTH});
        } else {
            thirdColumn.animate({width: wrapper.width() - COLLAPSED_COL_WIDTH - firstColumn.width() - 2 * SEPARATOR_WIDTH});
        }
    } else {
        var neededWidth = secondColumn.data('min-width') - (thirdColumn.width() - thirdColumn.data('min-width'));
        if (thirdColumn.hasClass('collapsed')) {
            firstColumn.animate({width: wrapper.width() - secondColumn.data('min-width') - COLLAPSED_COL_WIDTH - 2 * SEPARATOR_WIDTH});
            secondColumn.animate({width: secondColumn.data('min-width')});
        } else if (neededWidth > 0) {
            thirdColumn.animate({width: thirdColumn.data('min-width')});
            firstColumn.animate({width: wrapper.width() - secondColumn.data('min-width') - thirdColumn.data('min-width') - 2 * SEPARATOR_WIDTH});
            secondColumn.animate({width: secondColumn.data('min-width')});
        } else {
            thirdColumn.animate({width: wrapper.width() - firstColumn.width() - secondColumn.data('min-width') - 2 * SEPARATOR_WIDTH});
            secondColumn.animate({width: secondColumn.data('min-width')});
        }
    }
}

function toggleThirdColumn() {
    thirdColumn.toggleClass('collapsed');
    getElements();
    toggleResizers();
    toggleCollapseButtons();


    if (thirdColumn.hasClass('collapsed')) {
        thirdColumn.animate({width: COLLAPSED_COL_WIDTH});
        if (secondColumn.hasClass('collapsed')) {
            firstColumn.animate({width: wrapper.width() - 2 * COLLAPSED_COL_WIDTH - 2 * SEPARATOR_WIDTH});
        } else {
            secondColumn.animate({width: wrapper.width() - firstColumn.width() - COLLAPSED_COL_WIDTH - 2 * SEPARATOR_WIDTH});
        }
    } else {
        var neededWidth = thirdColumn.data('min-width') - (secondColumn.width() - secondColumn.data('min-width'));
        if (secondColumn.hasClass('collapsed')) {
            firstColumn.animate({width: wrapper.width() - COLLAPSED_COL_WIDTH - thirdColumn.data('min-width') - 2 * SEPARATOR_WIDTH});
            thirdColumn.animate({width: thirdColumn.data('min-width')});
        } else if (neededWidth > 0) {
            firstColumn.animate({width: wrapper.width() - secondColumn.data('min-width') - thirdColumn.data('min-width') - 2 * SEPARATOR_WIDTH});
            secondColumn.animate({width: secondColumn.data('min-width')});
            thirdColumn.animate({width: thirdColumn.data('min-width')});
        } else {
            secondColumn.animate({width: wrapper.width() - firstColumn.width() - thirdColumn.data('min-width') - 2 * SEPARATOR_WIDTH});
            thirdColumn.animate({width: thirdColumn.data('min-width')});
        }
    }
}
