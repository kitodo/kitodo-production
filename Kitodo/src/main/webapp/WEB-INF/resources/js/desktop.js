/*
 * (c) Kitodo. Key to digital objects e. V. <contact@kitodo.org>
 *
 * This file is part of the Kitodo project.
 *
 * It is licensed under GNU General Public License version 3 or later.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

function setScrollbarLayout() {
    $("main").height(window.innerHeight - ($("footer").outerHeight(true) + $("header").outerHeight(true)));

    const widgetTables = $("#taskTable, #processTable, #projectTable, #statisticsTable");
    const tableBodies = $("#desktopGrid .ui-datatable-scrollable-body");

    // add right padding to header for scrollbars
    const headerBoxes = $("#desktopGrid .ui-datatable-scrollable-header-box");
    for (var i = 0; i < widgetTables.length; i++) {
        let scrollBarWidth = widgetTables.eq(i).outerWidth(true) - tableBodies.eq(i).find("table[role='grid']").outerWidth(true);
        headerBoxes.eq(i).css("padding-right", scrollBarWidth + "px");
    }
}

$(window).on("load resize", setScrollbarLayout);
