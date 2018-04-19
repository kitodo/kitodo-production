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

function getKeyCode(e) {
    var keycode;

    keycode = e.keyCode ? e.keyCode : e.charCode;

    return keycode;
}

function checkOpac(commandId, e) {
    var keycode;

    keycode = getKeyCode(e);

    e.stopPropagation();
    if (keycode === 36) {
        return false;
    } else if ((keycode === 13) && (commandId === 'OpacRequest')) {
        var element = document.getElementById('editForm:processFromTemplateTabView:performOpacQuery');
        if (element) {
            element.click();
            return false;
        }
    } else {
        return true;
    }
    return true;
}

function ignoreEnterKey(e) {
    var keycode;
    keycode = getKeyCode(e);
    return keycode !== 13;
}
