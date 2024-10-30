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

var metadataTable = {};

metadataTable.addedMetadataRowKey = "root";

metadataTable.rememberMetadataGroup = function(event) {
    let rowKey = $(document.getElementById(event.source)).closest("tr").data("rk");
    metadataTable.addedMetadataRowKey = typeof rowKey !== 'undefined' ? rowKey : "root";
}

metadataTable.scrollToAddedMetadataRow = function() {
    let parentRowKey = metadataTable.addedMetadataRowKey;
    let metadataID = PF('metadataTypeSelection').getSelectedValue();
    let label = $("div[id$='metadataTable'] tr[data-prk='" + parentRowKey + "'] label[data-metadataid='" + metadataID + "']");
    let last_row = label.last().closest("tr");
    
    if (last_row.length === 1) {
        last_row.css("background-color", "#ffe");
        last_row.get(0).scrollIntoView();
        last_row.find("input:enabled:visible,textarea:enabled:visible").first().focus();
    }
}