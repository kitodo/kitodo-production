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

/*global PF*/
/*eslint new-cap: ["error", { "capIsNewExceptionPattern": "^PF" }]*/

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
        let reselectAll = (typeof this.selection !== undefined && this.selection[0] === '@all');
        let result = cachedFunction.apply(this, arguments);
        if (reselectAll) {
            this.selectAllRows();
        }
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

$(document).on("click", ".allSelectable .ui-chkbox .ui-chkbox-box", function () {
    let tableId = $(this).closest(".allSelectable").attr('id').split(":").at(-1);
    let table = new PF(tableId);
    if (typeof table.selection !== undefined && table.selection[0] === '@all') {
        table.unselectAllRows();
    }
});