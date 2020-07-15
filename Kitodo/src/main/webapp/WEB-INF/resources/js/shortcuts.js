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
            keyCombination += event.originalEvent.ctrlKey ? "Control " : "";
            keyCombination += event.originalEvent.altKey ? "Alt " : "";
            keyCombination += event.originalEvent.metaKey ? "Meta " : "";
            keyCombination += event.originalEvent.shiftKey ? "Shift " : "";
            keyCombination += event.originalEvent.altGraphKey ? "AltGraph " : "";
            keyCombination += event.originalEvent.code;
            event.originalEvent.target.value = keyCombination;
            event.preventDefault();
        });
    }
};

$(document).ready(function () {
    shortcuts.listen();
});
