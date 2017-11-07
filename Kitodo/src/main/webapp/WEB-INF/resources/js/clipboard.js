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

function copyToClipboard(inputFieldName) {
    try {
        var inputField = document.getElementById(inputFieldName);
        inputField.select();
        document.execCommand('copy');

        PF('notifications').renderMessage({
            "summary": "String copied to clipboard:",
            "detail": inputField.value,
            "severity": "info"
        });
    } catch (e) {
        PF('notifications').renderMessage({
            "summary": "Error copying string to clipboard:",
            "detail": inputField.value,
            "severity": "error"
        });
    }
}

