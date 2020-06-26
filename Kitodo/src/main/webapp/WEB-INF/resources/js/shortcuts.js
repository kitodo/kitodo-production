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

let shortcuts = {
    listen() {
        $(document).on("keydown.shortcuts", ".shortcut-input", function (event) {
            let keyCombination = "";
            let modifierKeys = [];
            if (event.originalEvent.ctrlKey) modifierKeys.push("Control");
            if (event.originalEvent.altKey) modifierKeys.push("Alt");
            if (event.originalEvent.metaKey) modifierKeys.push("Meta");
            if (event.originalEvent.shiftKey) modifierKeys.push("Shift");
            if (event.originalEvent.altGraphKey) modifierKeys.push("AltGraph");
            modifierKeys.forEach(function (item, index) {
                keyCombination += item + " ";
            });
            keyCombination += event.originalEvent.code;
            event.originalEvent.target.value = keyCombination;
            event.preventDefault();
        });
    }
};

$(document).ready(function () {
    shortcuts.listen();
});
