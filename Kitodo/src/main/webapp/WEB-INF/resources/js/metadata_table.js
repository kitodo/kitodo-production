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

/**
 * This class allows to focus the most recently added metadata row of a metadata table.
 * 
 * It requires each row to be annotated with a data attribute containing its metadata type.
 * When adding a new metadata row, the last row of the same metadata type is focused.
 */
class FocusMetadataRow {

    /**
     * Contains the row key (rk data attribute) of the metadata group whose add button (+) 
     * was last clicked by the user.
     */
    #lastSelectedRowKey;

    constructor() {
        this.#lastSelectedRowKey = "root";
    }

    /**
     * Retrieves the recently added metadata type (e.g. TitleDocMain or LABEL) from the 
     * "addMetadata" dialog. Requires "widgetVar" declaration on PrimeFaces element.
     */
    #getSelectedMetadataType() {
        return PF('metadataTypeSelection').getSelectedValue();
    }

    /**
     * Remove all highlights.
     */
    #removeHighlights() {
        $("div[id$='metadataTable'] tr.focusedRow").removeClass("focusedRow");
    }

    /**
     * Remembers the metadata group whose add button (+) was clicked by the user.
     * Is used to determine which metadata row needs to be focused if there are multiple 
     * groups with the same metadata type (e.g. ContributingPerson).
     * 
     * @param {String} rowId the id of the metadata row whose add button (+) was clicked by the user
     */
    remember(rowId) {
        this.#removeHighlights();
        // extract data attribute "rk" (row key?), which references the current row (tree node)
        let rowKey = $(document.getElementById(rowId)).closest("tr").data("rk");
        // remember the current row key if available
        this.#lastSelectedRowKey = typeof rowKey !== 'undefined' ? rowKey : "root";
    }

    /**
     * Focus the row that was added by a user.
     */
    focus() {
        this.#removeHighlights();

        // find last metadata row matching currently selected metadata type
        let row = $(
            "div[id$='metadataTable'] " + // metadata table selector
            "tr[data-prk='" + this.#lastSelectedRowKey + "'] " + // remembered metadata group
            "label[data-metadataid='" + this.#getSelectedMetadataType() + "']" // selected metadata type
        ).last().closest("tr");
        
        // if found, focus row
        if (row.length === 1) {
            row.addClass("focusedRow");
            row.get(0).scrollIntoView();
            row.find("input:enabled:visible,textarea:enabled:visible").first().focus();
        }
    }

}

// register class with global metadataTable namespace
var metadataTable = metadataTable || {};
metadataTable.focusMetadataRow = new FocusMetadataRow();
