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
/* globals PF, metadataEditor */
// jshint unused:false

var SEPARATOR_WIDTH = 3;
var COLLAPSED = 'collapsed';
var COLLAPSED_COL_WIDTH = 42;
var HEADING_HEIGHT = 40;
var SEPARATOR_HEIGHT = 5;
var dragging = false;
var target;

var wrapper;
var wrapperPositionX;
var firstColumn = $('#firstColumnWrapper');
var secondColumn = $('#secondColumnWrapper');
var thirdColumn = $('#thirdColumnWrapper');
var firstColumnWidth = firstColumn.width();
var secondColumnWidth = secondColumn.width();
var thirdColumnWidth = thirdColumn.width();
var collapsedColumns;
var firstResizer;
var secondResizer;

var sectionWrapperPosFirstColumn;
var sectionWrapperHeightFirstColumn;
var sectionWrapperPosSecondColumn;
var sectionWrapperHeightSecondColumn;
var firstSectionFirstColumn = $('#structurePanel');
var secondSectionFirstColumn = $('#commentsPanel');
var firstSectionSecondColumn = $('#metadataPanel');
var secondSectionSecondColumn = $('#paginationPanel');
var firstSectionFirstColumnHeight;
var secondSectionFirstColumnHeight;
var firstSectionSecondColumnHeight;
var secondSectionSecondColumnHeight;
var firstSectionFirstColumnToggler = $('#firstSectionFirstColumnToggler');
var secondSectionFirstColumnToggler = $('#secondSectionFirstColumnToggler');
var firstSectionSecondColumnToggler = $('#firstSectionSecondColumnToggler');
var secondSectionSecondColumnToggler = $('#secondSectionSecondColumnToggler');
var verticalResizerFirstColumn = $('#verticalResizerFirstColumn');
var verticalResizerSecondColumn = $('#verticalResizerSecondColumn');


$(document).ready(function() {
    $('#firstResizer').mousedown(function(e) {handleMouseDown(e)});
    $('#secondResizer').mousedown(function(e) {handleMouseDown(e)});
    $('#verticalResizerFirstColumn').mousedown(function(e) {handleMouseDown(e)});
    $('#verticalResizerSecondColumn').mousedown(function(e) {handleMouseDown(e)});
    setSizes();
    $("#loadingScreen").hide();
});

$(window).resize(setSizes);

$(document).mouseup(function(e) {
    if (dragging) {
        $(document).unbind('mousemove');
        dragging = false;
    }
});

function getStructureWidthInput() {
    return $('#metadataEditorLayoutForm\\:structureWidth');
}

function getMetadataWidthInput() {
    return $('#metadataEditorLayoutForm\\:metadataWidth');
}

function getGalleryWidthInput() {
    return $('#metadataEditorLayoutForm\\:galleryWidth');
}

function handleMouseDown(e) {
    e.preventDefault();
    dragging = true;
    target = e.target;
    getElements();

    $(document).mousemove(function(e) {
        if (target.id === 'firstResizer') {
            if (secondColumn.hasClass(COLLAPSED)) {
                resizeFirstAndThird(e);
            } else {
                resizeFirstAndSecond(e);
            }
        } else if (target.id === 'secondResizer') {
            if (secondColumn.hasClass(COLLAPSED)) {
                resizeFirstAndThird(e);
            } else {
                resizeSecondAndThird(e);
            }
        } else if (target.id === 'verticalResizerFirstColumn') {
            resizeVerticalFirstColumn(e);
        } else if (target.id === 'verticalResizerSecondColumn') {
            resizeVerticalSecondColumn(e);
        }
    });

}

function resizeFirstAndSecond(e) {
    if (e.pageX >= wrapperPositionX + firstColumn.data('min-width')
        && e.pageX <= wrapperPositionX + wrapper.width() - secondColumn.data('min-width') - SEPARATOR_WIDTH - thirdColumn.width()) {
        firstColumn.width(e.pageX - firstColumn.offset().left);
        secondColumn.width(wrapperPositionX + wrapper.width() - thirdColumn.width() - 2 * SEPARATOR_WIDTH - e.pageX);
        firstColumnWidth = firstColumn.width();
        secondColumnWidth = secondColumn.width();
    }
}

function resizeFirstAndThird(e) {
    if (e.pageX >= wrapperPositionX + firstColumn.data('min-width')
        && e.pageX <= wrapperPositionX + wrapper.width() - thirdColumn.data('min-width')) {
        firstColumn.width(e.pageX - firstColumn.offset().left);
        thirdColumn.width(wrapperPositionX + wrapper.width() - 2 * SEPARATOR_WIDTH - secondColumn.width() - e.pageX);
        firstColumnWidth = firstColumn.width();
        thirdColumnWidth = thirdColumn.width();
        thirdColumn[0].dispatchEvent(new Event('resize'));
    }
}

function resizeSecondAndThird(e) {
    if (e.pageX >= wrapperPositionX + firstColumn.width() + SEPARATOR_WIDTH + secondColumn.data('min-width')
        && e.pageX <= wrapperPositionX + wrapper.width() - SEPARATOR_WIDTH - thirdColumn.data('min-width')) {
        secondColumn.width(e.pageX - secondColumn.offset().left);
        thirdColumn.width(wrapperPositionX + wrapper.width() - SEPARATOR_WIDTH - e.pageX);
        secondColumnWidth = secondColumn.width();
        thirdColumnWidth = thirdColumn.width();
        thirdColumn[0].dispatchEvent(new Event('resize'));
    }
}

function resizeVerticalFirstColumn(e) {
    if (e.pageY >= sectionWrapperPosFirstColumn + firstSectionFirstColumn.data('min-height')
        && e.pageY <= sectionWrapperPosFirstColumn + sectionWrapperHeightFirstColumn - HEADING_HEIGHT - secondSectionFirstColumn.data('min-height')) {
        firstSectionFirstColumn.height(e.pageY - sectionWrapperPosFirstColumn);
        secondSectionFirstColumn.height(sectionWrapperPosFirstColumn + sectionWrapperHeightFirstColumn - e.pageY - SEPARATOR_HEIGHT - HEADING_HEIGHT);
    }
}
function resizeVerticalSecondColumn(e) {
    if (e.pageY >= sectionWrapperPosSecondColumn + firstSectionSecondColumn.data('min-height')
        && e.pageY <= sectionWrapperPosSecondColumn + sectionWrapperHeightSecondColumn - HEADING_HEIGHT - secondSectionSecondColumn.data('min-height')) {
        firstSectionSecondColumn.height(e.pageY - sectionWrapperPosSecondColumn);
        secondSectionSecondColumn.height(sectionWrapperPosSecondColumn + sectionWrapperHeightSecondColumn - e.pageY - SEPARATOR_HEIGHT - HEADING_HEIGHT);
    }
}

function getElements() {
    wrapper = $('#metadataEditorWrapper');
    wrapperPositionX = wrapper.offset().left;
    collapsedColumns = $('#metadataEditorWrapper > ' + COLLAPSED).length;
    var resizers = $('.resizer');
    firstResizer = resizers.first();
    secondResizer = resizers.last();

    var firstColumnPanel = $('#firstColumnPanel');
    sectionWrapperPosFirstColumn = firstColumnPanel.offset().top;
    sectionWrapperHeightFirstColumn = firstColumnPanel.height();

    var secondColumnPanel = $('#secondColumnPanel');
    sectionWrapperPosSecondColumn = secondColumnPanel.offset().top;
    sectionWrapperHeightSecondColumn = secondColumnPanel.height();
}

function setSizes() {
    getElements();

    wrapper.height(window.innerHeight - wrapper.offset().top - $('footer').height());

    let savedStructureWidth = parseFloat(getStructureWidthInput().val()) * wrapper.width();
    let savedMetadataWidth = parseFloat(getMetadataWidthInput().val()) * wrapper.width();
    let savedGalleryWidth = parseFloat(getGalleryWidthInput().val()) * wrapper.width();

    if (!(savedStructureWidth === 0 && savedMetadataWidth === 0 && savedGalleryWidth === 0)) {
        // TODO set widths
        if (savedStructureWidth === 0) {
            firstColumn.addClass(COLLAPSED);
        }
        if (savedMetadataWidth === 0) {
            secondColumn.addClass(COLLAPSED);
        }
        if (savedGalleryWidth === 0) {
            thirdColumn.addClass(COLLAPSED);
        }
    }

    if (firstColumn.hasClass(COLLAPSED)) {
        firstColumn.width(COLLAPSED_COL_WIDTH);
        firstColumnWidth = firstColumn.data('min-width');
    } else if (secondColumn.hasClass(COLLAPSED) && thirdColumn.hasClass(COLLAPSED)) {
        firstColumn.width(wrapper.width() - 2 * COLLAPSED_COL_WIDTH - 2 * SEPARATOR_WIDTH);
    } else {
        let minWidth = firstColumn.data('min-width');
        firstColumn.width(savedStructureWidth > minWidth ? savedStructureWidth : minWidth);
    }

    if (secondColumn.hasClass(COLLAPSED)) {
        secondColumn.width(COLLAPSED_COL_WIDTH);
        secondColumnWidth = secondColumn.data('min-width');
    } else if(firstColumn.hasClass(COLLAPSED) && thirdColumn.hasClass(COLLAPSED)) {
        secondColumn.width(wrapper.width() - 2 * COLLAPSED_COL_WIDTH - 2 * SEPARATOR_WIDTH);
    } else {
        let minWidth = secondColumn.data('min-width');
        secondColumn.width(savedMetadataWidth > minWidth ? savedMetadataWidth : minWidth);
    }

    if (thirdColumn.hasClass(COLLAPSED)) {
        thirdColumn.width(COLLAPSED_COL_WIDTH);
        thirdColumnWidth = thirdColumn.data('min-width');
        if (!secondColumn.hasClass(COLLAPSED)) {
            secondColumn.width(wrapper.width() - firstColumn.width() - thirdColumn.width() - 2 * SEPARATOR_WIDTH);
        }
    } else {
        thirdColumn.width(wrapper.width() - firstColumn.width() - secondColumn.width() - 2 * SEPARATOR_WIDTH);
    }

    setSectionHeightFirstColumn();
    setSectionHeightSecondColumn();

    thirdColumn[0].dispatchEvent(new Event('resize'));
    toggleResizers();
    toggleCollapseButtons();
}

function setHeight() {
    wrapper.height(window.innerHeight - wrapper.offset().top - $('footer').height());
    setSectionHeightFirstColumn();
    setSectionHeightSecondColumn();
}

function setSectionHeightFirstColumn() {
    if (firstSectionFirstColumn.hasClass(COLLAPSED)) {
        firstSectionFirstColumnHeight = wrapper.height() / 2 - HEADING_HEIGHT - (parseInt(firstColumn.css('padding-top')) / 2);
        secondSectionFirstColumn.height(secondSectionFirstColumn.height() + firstSectionFirstColumnHeight);
    } else if (secondSectionFirstColumn.hasClass(COLLAPSED)) {
        firstSectionFirstColumn.height(wrapper.height() - 2 * HEADING_HEIGHT - (parseInt(firstColumn.css('padding-top'))) - SEPARATOR_HEIGHT);
        secondSectionFirstColumn.height(wrapper.height() / 2 - HEADING_HEIGHT - (parseInt(firstColumn.css('padding-top')) / 2) - SEPARATOR_HEIGHT);
    } else {
        firstSectionFirstColumn.height(wrapper.height() / 2 - HEADING_HEIGHT - (parseInt(firstColumn.css('padding-top')) / 2));
        secondSectionFirstColumn.height(wrapper.height() / 2 - HEADING_HEIGHT - (parseInt(firstColumn.css('padding-top')) / 2) - SEPARATOR_HEIGHT);
    }
}

function setSectionHeightSecondColumn() {
    if (firstSectionSecondColumn.hasClass(COLLAPSED)) {
        firstSectionSecondColumnHeight = wrapper.height() / 2 - HEADING_HEIGHT - (parseInt(secondColumn.css('padding-top')) / 2);
        secondSectionSecondColumn.height(secondSectionSecondColumn.height() + firstSectionSecondColumnHeight);
    } else if (secondSectionSecondColumn.hasClass(COLLAPSED)) {
        firstSectionSecondColumn.height(wrapper.height() - 2 * HEADING_HEIGHT - (parseInt(secondColumn.css('padding-top'))) - SEPARATOR_HEIGHT);
        secondSectionSecondColumn.height(wrapper.height() / 2 - HEADING_HEIGHT - (parseInt(secondColumn.css('padding-top')) / 2) - SEPARATOR_HEIGHT);
    } else {
        firstSectionSecondColumn.height(wrapper.height() / 2 - HEADING_HEIGHT - (parseInt(secondColumn.css('padding-top')) / 2));
        secondSectionSecondColumn.height(wrapper.height() / 2 - HEADING_HEIGHT - (parseInt(secondColumn.css('padding-top')) / 2) - SEPARATOR_HEIGHT);
    }
}

function toggleResizers() {
    if (collapsedColumns >= 2) {
        firstResizer.addClass('disabled');
        secondResizer.addClass('disabled');
    } else if (firstColumn.hasClass(COLLAPSED)) {
        firstResizer.addClass('disabled');
        secondResizer.removeClass('disabled');
    } else if (thirdColumn.hasClass(COLLAPSED)) {
        firstResizer.removeClass('disabled');
        secondResizer.addClass('disabled');
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
        if (!firstColumn.hasClass(COLLAPSED)) {
            firstButton.prop('disabled', true);
        } else if (!secondColumn.hasClass(COLLAPSED)) {
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
    if (!firstColumn.hasClass(COLLAPSED)) {
        firstColumnWidth = firstColumn.width();
    }
    firstColumn.toggleClass(COLLAPSED);
    getElements();
    toggleResizers();
    toggleCollapseButtons();

    if (firstColumn.hasClass(COLLAPSED)) {
        firstColumn.animate({width: COLLAPSED_COL_WIDTH});
        if (secondColumn.hasClass(COLLAPSED)) {
            thirdColumn.animate({width: wrapper.width() - 2 * COLLAPSED_COL_WIDTH - 2 * SEPARATOR_WIDTH}, function() {resizeMap()});
        } else {
            secondColumn.animate({width:  wrapper.width() - COLLAPSED_COL_WIDTH - thirdColumn.width() - 2 * SEPARATOR_WIDTH});
        }
    } else {
        var neededWidth = firstColumnWidth - COLLAPSED_COL_WIDTH - (secondColumn.width() - secondColumn.data('min-width'));
        if (secondColumn.hasClass(COLLAPSED)) {
            thirdColumn.animate({width: wrapper.width() - firstColumnWidth - COLLAPSED_COL_WIDTH - 2 * SEPARATOR_WIDTH}, function() {resizeMap()});
            firstColumn.animate({width: firstColumnWidth});
        } else if (neededWidth > 0) {
            var substractFromWidth = firstColumnWidth - (wrapper.width() - secondColumn.data('min-width') - thirdColumn.data('min-width') - 2 * SEPARATOR_WIDTH);
            if (substractFromWidth > 0) {
                firstColumn.animate({width: firstColumnWidth - substractFromWidth});
                secondColumn.animate({width: secondColumn.data('min-width')});
                thirdColumn.animate({width: thirdColumn.data('min-width')}, function() {resizeMap();});
            } else {
                secondColumn.animate({width: secondColumn.data('min-width')});
                thirdColumn.animate({width: wrapper.width() - firstColumnWidth - secondColumn.data('min-width') - 2 * SEPARATOR_WIDTH}, function() {resizeMap()});
                firstColumn.animate({width: firstColumnWidth});
            }
        } else {
            secondColumn.animate({width: wrapper.width() - firstColumnWidth - thirdColumn.width() - 2 * SEPARATOR_WIDTH});
            firstColumn.animate({width: firstColumnWidth});
        }
    }
}

function toggleSecondColumn() {
    if (!secondColumn.hasClass(COLLAPSED)) {
        secondColumnWidth = secondColumn.width();
    }
    secondColumn.toggleClass(COLLAPSED);
    getElements();
    toggleResizers();
    toggleCollapseButtons();

    if (secondColumn.hasClass(COLLAPSED)) {
        secondColumn.animate({width: COLLAPSED_COL_WIDTH});
        if (thirdColumn.hasClass(COLLAPSED)) {
            firstColumn.animate({width: wrapper.width() - 2 * COLLAPSED_COL_WIDTH - 2 * SEPARATOR_WIDTH});
        } else {
            thirdColumn.animate({width: wrapper.width() - COLLAPSED_COL_WIDTH - firstColumn.width() - 2 * SEPARATOR_WIDTH},function() {resizeMap()});
        }
    } else {
        var neededWidth = secondColumnWidth - COLLAPSED_COL_WIDTH - (thirdColumn.width() - thirdColumn.data('min-width'));
        if (thirdColumn.hasClass(COLLAPSED)) {
            firstColumn.animate({width: wrapper.width() - secondColumnWidth - COLLAPSED_COL_WIDTH - 2 * SEPARATOR_WIDTH});
            secondColumn.animate({width: secondColumnWidth});
        } else if (neededWidth > 0) {
            var substractFromWidth = secondColumnWidth - (wrapper.width() - firstColumn.data('min-width') - thirdColumn.data('min-width') - 2 * SEPARATOR_WIDTH);
            if (substractFromWidth > 0) {
                firstColumn.animate({width: firstColumn.data('min-width')});
                secondColumn.animate({width: secondColumnWidth - substractFromWidth});
                thirdColumn.animate({width: thirdColumn.data('min-width')},function() {resizeMap()});
            } else {
                thirdColumn.animate({width: thirdColumn.data('min-width')},function() {resizeMap()});
                firstColumn.animate({width: wrapper.width() - secondColumnWidth - thirdColumn.data('min-width') - 2 * SEPARATOR_WIDTH});
                secondColumn.animate({width: secondColumnWidth});
            }
        } else {
            thirdColumn.animate({width: wrapper.width() - firstColumn.width() - secondColumnWidth - 2 * SEPARATOR_WIDTH},function() {resizeMap()});
            secondColumn.animate({width: secondColumnWidth});
        }
    }
}

function toggleThirdColumn() {
    if (!thirdColumn.hasClass(COLLAPSED)) {
        thirdColumnWidth = thirdColumn.width();
    }
    thirdColumn.toggleClass(COLLAPSED);
    getElements();
    toggleResizers();
    toggleCollapseButtons();


    if (thirdColumn.hasClass(COLLAPSED)) {
        thirdColumn.animate({width: COLLAPSED_COL_WIDTH});
        if (secondColumn.hasClass(COLLAPSED)) {
            firstColumn.animate({width: wrapper.width() - 2 * COLLAPSED_COL_WIDTH - 2 * SEPARATOR_WIDTH});
        } else {
            secondColumn.animate({width: wrapper.width() - firstColumn.width() - COLLAPSED_COL_WIDTH - 2 * SEPARATOR_WIDTH});
        }
    } else {
        var neededWidth = thirdColumnWidth - COLLAPSED_COL_WIDTH - (secondColumn.width() - secondColumn.data('min-width'));
        if (secondColumn.hasClass(COLLAPSED)) {
            firstColumn.animate({width: wrapper.width() - COLLAPSED_COL_WIDTH - thirdColumnWidth - 2 * SEPARATOR_WIDTH});
            thirdColumn.animate({width: thirdColumnWidth}, function() {resizeMap()});
        } else if (neededWidth > 0) {
            var substractFromWidth = thirdColumnWidth - (wrapper.width() - firstColumn.data('min-width') - secondColumn.data('min-width') - 2 * SEPARATOR_WIDTH);
            if (substractFromWidth > 0) {
                firstColumn.animate({width: firstColumn.data('min-width')});
                secondColumn.animate({width: secondColumn.data('min-width')});
                thirdColumn.animate({width: thirdColumnWidth - substractFromWidth}, function() {resizeMap()});
            } else {
                firstColumn.animate({width: wrapper.width() - secondColumn.data('min-width') - thirdColumnWidth - 2 * SEPARATOR_WIDTH});
                secondColumn.animate({width: secondColumn.data('min-width')});
                thirdColumn.animate({width: thirdColumnWidth}, function() {resizeMap()});
            }
        } else {
            secondColumn.animate({width: wrapper.width() - firstColumn.width() - thirdColumnWidth - 2 * SEPARATOR_WIDTH});
            thirdColumn.animate({width: thirdColumnWidth}, function() {resizeMap()});
        }
    }
}

function toggleFirstSectionFirstColumn() {
    if (!firstSectionFirstColumn.hasClass(COLLAPSED)) {
        firstSectionFirstColumnHeight = firstSectionFirstColumn.height();
    }
    firstSectionFirstColumn.toggleClass(COLLAPSED);
    firstSectionFirstColumnToggler.toggleClass(COLLAPSED);
    verticalResizerFirstColumn.toggleClass('disabled');

    if (firstSectionFirstColumn.hasClass(COLLAPSED)) {
        secondSectionFirstColumn.height(secondSectionFirstColumn.height() + firstSectionFirstColumnHeight);
        secondSectionFirstColumnToggler.prop('disabled', true);
    } else {
        secondSectionFirstColumn.height(secondSectionFirstColumn.height() - firstSectionFirstColumnHeight);
        secondSectionFirstColumnToggler.prop('disabled', false);
    }
}
function toggleFirstSectionSecondColumn() {
    if (!firstSectionSecondColumn.hasClass(COLLAPSED)) {
        firstSectionSecondColumnHeight = firstSectionSecondColumn.height();
    }
    firstSectionSecondColumn.toggleClass(COLLAPSED);
    firstSectionSecondColumnToggler.toggleClass(COLLAPSED);
    verticalResizerSecondColumn.toggleClass('disabled');

    if (firstSectionSecondColumn.hasClass(COLLAPSED)) {
        secondSectionSecondColumn.height(secondSectionSecondColumn.height() + firstSectionSecondColumnHeight);
        secondSectionSecondColumnToggler.prop('disabled', true);
    } else {
        secondSectionSecondColumn.height(secondSectionSecondColumn.height() - firstSectionSecondColumnHeight);
        secondSectionSecondColumnToggler.prop('disabled', false);
    }
}

function toggleSecondSection(firstSection, secondSection, firstSectionToggler, secondSectionHeight) {
    if (secondSection.hasClass(COLLAPSED)) {
        firstSection.height(firstSection.height() + secondSectionHeight);
        firstSectionToggler.prop('disabled', true);
    } else {
        firstSection.height(firstSection.height() - secondSectionHeight);
        firstSectionToggler.prop('disabled', false);
    }
}

function toggleHeightOfSecondSectionFirstColumn() {
    if (typeof secondSectionFirstColumnHeight === 'undefined') {
        secondSectionFirstColumnHeight = wrapper.height() / 2 - HEADING_HEIGHT - (parseInt(firstColumn.css('padding-top')) / 2) - SEPARATOR_HEIGHT;
    }
    toggleSecondSection(firstSectionFirstColumn, secondSectionFirstColumn, firstSectionFirstColumnToggler, secondSectionFirstColumnHeight);
}

function toggleHeightOfSecondSectionSecondColumn() {
    if (typeof secondSectionSecondColumnHeight === 'undefined') {
        secondSectionSecondColumnHeight = wrapper.height() / 2 - HEADING_HEIGHT - (parseInt(secondColumn.css('padding-top')) / 2) - SEPARATOR_HEIGHT;
    }
    toggleSecondSection(firstSectionSecondColumn, secondSectionSecondColumn, firstSectionSecondColumnToggler, secondSectionSecondColumnHeight);
}

function toggleSecondSectionFirstColumn() {
    if (!secondSectionFirstColumn.hasClass(COLLAPSED)) {
        secondSectionFirstColumnHeight = secondSectionFirstColumn.height();
    }
    secondSectionFirstColumn.toggleClass(COLLAPSED);
    secondSectionFirstColumnToggler.toggleClass(COLLAPSED);
    verticalResizerFirstColumn.toggleClass('disabled');
    toggleHeightOfSecondSectionFirstColumn();
}
function toggleSecondSectionSecondColumn() {
    if (!secondSectionSecondColumn.hasClass(COLLAPSED)) {
        secondSectionSecondColumnHeight = secondSectionSecondColumn.height();
    }
    secondSectionSecondColumn.toggleClass(COLLAPSED);
    secondSectionSecondColumnToggler.toggleClass(COLLAPSED);
    verticalResizerSecondColumn.toggleClass('disabled');
    toggleHeightOfSecondSectionSecondColumn();
}

function expandFirstColumn() {
    if (firstSectionFirstColumnToggler.length && firstColumn.hasClass(COLLAPSED)) {
        toggleFirstColumn();
    }
}

function expandSecondColumn() {
    if (firstSectionSecondColumnToggler.length && secondColumn.hasClass(COLLAPSED)) {
        toggleSecondColumn();
    }
}

function expandThirdColumn() {
    if ($('#thirdColumnWrapper .columnExpandButton').length && thirdColumn.hasClass(COLLAPSED)) {
        toggleThirdColumn();
    }
}

function updateMetadataEditorView(showMetadataColumn) {
    PF('dialogAddDocStrucType').hide();
    PF('dialogEditDocStrucType').hide();
    expandFirstColumn();
    if (showMetadataColumn) {
        expandSecondColumn();
    }
    expandThirdColumn();
    scrollToSelectedThumbnail();
    initializeImage();
    metadataEditor.gallery.mediaView.update();
    scrollToSelectedTreeNode();
    scrollToSelectedPaginationRow();
}

function resizeMap() {
    if (kitodo.map) {
        kitodo.map.updateSize();
    }
}

function saveLayout() {
    getStructureWidthInput().val(firstColumn.hasClass(COLLAPSED) ? 0 : firstColumn.width()/wrapper.width());
    getMetadataWidthInput().val(secondColumn.hasClass(COLLAPSED) ? 0 : secondColumn.width()/wrapper.width());
    getGalleryWidthInput().val(thirdColumn.hasClass(COLLAPSED) ? 0 : thirdColumn.width()/wrapper.width());
}
