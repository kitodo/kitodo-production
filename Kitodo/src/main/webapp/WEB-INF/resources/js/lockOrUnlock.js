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

 function enableOrDisable(target, locked, unlocked) {

    //Edit lock/Unlock button (icon and text)
    target.firstChild.classList.toggle('fa-lock');
    target.firstChild.classList.toggle('fa-unlock');
    $("span", target).filter(".ui-button-text").text(function(i, text){
        return text === unlocked ? locked : unlocked;
        });

    //Enable and Disable input elements
    var elements = $('#editForm\\:projectTabView input:visible');
    elements.toggleClass('ui-state-disabled');
    elements.prop('disabled', function (_, val){ return ! val; });

    //Enable and Disable selectOneMenu elements
    var selectOneMenus = $(".ui-selectonemenu").filter(":visible").toggleClass('ui-state-disabled');

    //Enable and Disable selectBooleanCheckbox elements
    $(".ui-chkbox-box").filter(":visible").toggleClass('ui-state-disabled');

    //Enable and Disable commandlink elements
    $(".ui-commandlink").filter(":visible").toggleClass('ui-state-disabled');

    //Enable and Disable calendar elements
    $(":button").filter(".ui-datepicker-trigger").filter(":visible").prop('disabled', function (_, val){ return ! val; });
    $(":button").filter(".ui-datepicker-trigger").filter(":visible").toggleClass('ui-state-disabled');

    //Enable and Disable command buttons
    $('#editForm\\:projectTabView :submit').filter(":visible").not(target).toggleClass('ui-state-disabled');
 }
 