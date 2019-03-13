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
    target.firstChild.classList.toggle('fa-lock');
    target.firstChild.classList.toggle('fa-unlock');

    var elements = $('#editForm\\:projectTabView input:visible');
    var calandars = $('#editForm\\:projectTabView .ui-datepicker-trigger');
    if ($("span", target).filter(".ui-button-text").text() === locked) {
        elements.removeClass('ui-state-disabled');
        elements.prop('disabled', false);
        $("#editForm\\:projectTabView .ui-selectonemenu:visible").removeClass('ui-state-disabled');
        $("#editForm\\:projectTabView .ui-chkbox-box:visible").removeClass('ui-state-disabled');
        calandars.prop('disabled', false);
        calandars.removeClass('ui-state-disabled');
        $("#editForm\\:projectTabView .ui-commandlink:visible").removeClass('ui-state-disabled');
        $('#editForm\\:projectTabView :submit:visible').not(target).removeClass('ui-state-disabled');
    } else {
        elements.addClass('ui-state-disabled');
        elements.prop('disabled', true);
        $("#editForm\\:projectTabView .ui-selectonemenu:visible").addClass('ui-state-disabled');
        $("#editForm\\:projectTabView .ui-chkbox-box:visible").addClass('ui-state-disabled');
        calandars.prop('disabled', 'disabled');
        calandars.addClass('ui-state-disabled');
        $("#editForm\\:projectTabView .ui-commandlink:visible").addClass('ui-state-disabled');
        $('#editForm\\:projectTabView :submit:visible').not(target).addClass('ui-state-disabled');
    }
    $("span", target).filter(".ui-button-text").text(function (i, text) {
        return text === unlocked ? locked : unlocked;
    });
}

function disableGlobally(locked) {
    var lockbutons = $("#editForm\\:projectTabView\\:detailLockedButton, #editForm\\:projectTabView\\:technicalLockedButton," +
        " #editForm\\:projectTabView\\:metsParamLockedButton, #editForm\\:projectTabView\\:templateLockedButton");
    lockbutons.each(function () {
        $(this).children(':first').removeClass('fa-unlock');
        $(this).children(':first').addClass('fa-lock');
        $("span", $(this)).filter(".ui-button-text").text(locked);
    });
    var all = $('#editForm\\:projectTabView input');
    all.each(function () {
        var disabledAttr = $(this).attr('disabled');
        if (typeof disabledAttr === typeof undefined || disabledAttr === false) {
            $(this).attr('disabled', 'disabled');
            $(this).addClass('ui-state-disabled');
        }
    });
    $("#editForm\\:projectTabView .ui-chkbox-box").addClass('ui-state-disabled');
    $("#editForm\\:projectTabView .ui-commandlink").addClass('ui-state-disabled');
    var calandars = $('#editForm\\:projectTabView .ui-datepicker-trigger');
    calandars.prop('disabled', 'disabled');
    calandars.addClass('ui-state-disabled');
    $('#editForm\\:projectTabView :submit').not(lockbutons).addClass('ui-state-disabled');
    $("#editForm\\:projectTabView .ui-selectonemenu").addClass("ui-state-disabled");
}
