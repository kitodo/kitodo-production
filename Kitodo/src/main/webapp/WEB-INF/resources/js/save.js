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
    window.inputs;
    window.values = [];

    inputs = document.querySelectorAll('#' + form + ' .ui-inputtext');
    inputs.forEach(function (element) {
        values.push(element.value);
    });
}

function compareForm(form) {
    var currentValues = [];

    inputs.forEach(function (element) {
        currentValues.push(element.value);
    });

    for (let i = 0; i < values.length; i++) {
        if (values[i] != currentValues[i]) {
            jQuery('#' + form + '\\:saveButtonToggler').click();
            break;
        }
    }
}
