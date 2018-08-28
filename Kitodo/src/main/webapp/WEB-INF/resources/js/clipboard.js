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

function copyToClipboard(inputFieldName, successMessage, errorMessage) {
    let inputField = document.getElementById(inputFieldName);
    try {
        // Temporarily activate deactivated inputFields to enable selecting
        if (inputField.getAttribute('disabled') && inputField.getAttribute('disabled').localeCompare('disabled') === 0) {
            inputField.removeAttribute('disabled');
            inputField.select();
            inputField.setAttribute('disabled', 'disabled');
        } else {
            inputField.select();
        }

        document.execCommand('copy');

        $(inputField).removeClass('ui-state-focus');

        PF('notifications').renderMessage({
            "summary": successMessage,
            "detail": inputField.value,
            "severity": "info"
        });
    } catch (e) {
        PF('notifications').renderMessage({
            "summary": errorMessage,
            "detail": inputField.value,
            "severity": "error"
        });
    }
}

