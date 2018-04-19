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

var i = 0;
var dragging = false;

$(document).ready(function() {
    $('#firstResizer').mousedown(function(e) {handleMouseDown(e)});
    $('#secondResizer').mousedown(function(e) {handleMouseDown(e)});
});


function handleMouseDown(e) {
    e.preventDefault();
    dragging = true;
    var firstColumn;
    var secondColumn;

    if (e.target.id == 'firstResizer') {
        firstColumn = $('#firstColumnWrapper');
        secondColumn = $('#secondColumnWrapper');
    } else {
        firstColumn = $('#secondColumn');
        secondColumn = $('#thirdColumn');
    }

    var ghostResizer = $('<div>', {
        id:'ghostResizer',
        css: {
            height: e.target.offsetHeight,
            top: e.target.getBoundingClientRect().top + window.scrollY,
            left: e.pageX
        }
    }).appendTo($('body'));

    $(document).mousemove(function(e) {
       ghostResizer.css('left', e.pageX);
    });

}

$(document).mouseup(function(e) {
   if (dragging) {
       // do resizing
       $('#ghostResizer').remove();
       $(document).unbind('mousemove');
       dragging = false;
   }
});