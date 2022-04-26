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

function checkForm(form) {
    var inputs;
    window.values = [];

    inputs = document.querySelectorAll('#' + form + ' .ui-inputtext');
    $('#' + form).on('input', '.ui-inputtext', function () {compareForm(form)});
    inputs.forEach(function (element) {
        values.push(element.value);
    });
}

function compareForm(form) {
    var inputs;
    var currentValues = [];

    inputs = document.querySelectorAll('#' + form + ' .ui-inputtext');

    inputs.forEach(function (element) {
        currentValues.push(element.value);
    });

    for (let i = 0; i < values.length; i++) {
        if (values[i] !== currentValues[i]) {
            jQuery('#' + form + '\\:saveButtonToggler').click();
            break;
        }
    }
}

function toggleSave() {
    jQuery('#editForm\\:saveButtonToggler').click();
}

window.onload = function () {
    checkForm('editForm');
};
