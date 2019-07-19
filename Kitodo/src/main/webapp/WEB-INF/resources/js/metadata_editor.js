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

var metadataEditor = {};

metadataEditor.select = {

    selectionType: function(event) {
        console.log(event);
        if (event.metaKey || event.ctrlKey) {
            multiSelect({'innerHTML': event.target.innerHTML});
        } else if (event.shiftKey) {
            rangeSelect({'innerHTML': event.target.innerHTML});
        } else {
            console.log("No modifier key detected: default select!");
        }
    }
};
