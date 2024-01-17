/*
 * (c) Kitodo. Key to digital objects e. V. <contact@kitodo.org>
 *
 * This file is part of the Kitodo project.
 *
 * It is licensed under GNU General Public License version 3 or later.
 *
 * For the full copyright and license information, please read the
 * GPL3-License.txt file that was distributed with this source code.
 */

/*global PF, PrimeFaces*/
/*eslint new-cap: ["error", { "capIsNewExceptionPattern": "^PF" }]*/
/*eslint wrap-iife: ["error", "any"]*/

function registerRowToggleEvents(event) {
    // add listener to expanded rows
    $(".ui-expanded-row").hover(function() {
            $(this).next(".ui-expanded-row-content").addClass("row-hover");
        }, function () {
            $(this).next(".ui-expanded-row-content").removeClass("row-hover");
        }
    );
    $(".ui-expanded-row:hover").next(".ui-expanded-row-content").addClass("row-hover");

    // add listener to expanded rows content
    $(".ui-expanded-row-content").hover(function() {
            $(this).prev(".ui-expanded-row:last").addClass("row-hover");
        }, function () {
            $(this).prev(".ui-expanded-row:last").removeClass("row-hover");
        }
    );
}

PrimeFaces.widget.DataTable.prototype.updateData = (function () {
    let cachedFunction = PrimeFaces.widget.DataTable.prototype.updateData;
    return function () {
        let result = cachedFunction.apply(this, arguments);
        return result;
    };
})();

$(document).on("click", ".allSelectable .ui-chkbox-all .ui-chkbox-box", function () {
    let tableId = $(this).closest(".allSelectable").attr('id').split(":").at(-1);
    let table = new PF(tableId);
    if ($(this).hasClass("ui-state-active")) {
        new PF('allSelectableOverlayPanel').show();
    }
    table.unselectAllRows();
});

$(window).on("load", function () {
    $.ready.then(function () {
        if ($('#processesTabView\\:processesForm\\:processesTable').length && typeof PF('processesTable').selection !== "undefined" && (PF('processesTable').selection[0] === '@all' || PF('processesTable').selection.length === PF('processesTable').cfg.paginator.rowCount )) {
            PF('processesTable').selectAllRows();
            PF('processesTable').selection=new Array("@all");
            $(PF('processesTable').selectionHolder).val('@all');
            let excludedIds = $('#excludedProcessIds').children();
            for(let i = 0; i < excludedIds.length; i++) {
                let processId = excludedIds.get(i).textContent;
                PF('processesTable').unselectRow($('tr[data-rk="' + processId + '"]'), true);
            }

        }
    });
});
