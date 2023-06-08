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
/* globals setUnsavedChanges */

function setUnsavedChanges(state) {
    $("body").data("unsavedchanges", state);
}

window.onbeforeunload = function() {
    return $("body").data("unsavedchanges") ? true : void 0;
};
