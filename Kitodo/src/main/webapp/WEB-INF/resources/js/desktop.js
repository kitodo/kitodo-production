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

$(window).on("load resize", function() {
    $("main").height(window.innerHeight - ($("footer").outerHeight(true) + $("#breadcrumb").outerHeight(true) + $("header").outerHeight(true)));

    // update widget heights
    const widgetTables = $("#taskTable, #processTable, #projectTable, #statisticsTable");
    const widgetHeight = widgetTables.parent().outerHeight();
    const headerHeight = widgetTables.prev(".content-header").outerHeight();
    widgetTables.height(widgetHeight - headerHeight);

    const tableBodies = $("#desktopGrid .ui-datatable-scrollable-body");

    // update table heights
    const tableHeight = tableBodies.parent().outerHeight();
    const tableHeaderHeight = tableBodies.prev(".ui-widget-header").outerHeight();
    tableBodies.height(tableHeight - tableHeaderHeight);

    // add right padding to header for scrollbars
    const headerBoxes = $("#desktopGrid .ui-datatable-scrollable-header-box");
    for (var i = 0; i < widgetTables.length; i++) {
        let scrollBarWidth = widgetTables.eq(i).outerWidth(true) - tableBodies.eq(i).find("table[role='grid']").outerWidth(true);
        headerBoxes.eq(i).css("padding-right", scrollBarWidth + "px");
    }
});
