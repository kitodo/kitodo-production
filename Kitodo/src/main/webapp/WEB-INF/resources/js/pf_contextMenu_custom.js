/**
 * (c) Kitodo. Key to digital objects e. V. <contact@kitodo.org>
 *
 * This file is part of the Kitodo project.
 *
 * It is licensed under GNU General Public License version 3 or later.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

/* global PrimeFaces */

let currentEvent;

$(document).ready(function() {
    PrimeFaces.widget.ContextMenu.prototype.show = function(e) {
        // hide other context menus if any
        $(document.body).children('.ui-contextmenu:visible').hide();

        if(e) {
            currentEvent = e;
        }

        let win = $(window),
            left = e.pageX,
            top = e.pageY,
            width = this.jq.outerWidth(),
            height = this.jq.outerHeight();

        // collision detection for window boundaries
        if((left + width) > (win.width())+ win.scrollLeft()) {
            left = left - width;
        }
        if((top + height ) > (win.height() + win.scrollTop())) {
            top = top - height;
        }

        if(this.cfg.beforeShow) {
            this.cfg.beforeShow.call(this);
        }

        this.jq.css({
            left,
            top,
            'z-index': ++PrimeFaces.zindex
        }).show();

        e.preventDefault();
    };
});
