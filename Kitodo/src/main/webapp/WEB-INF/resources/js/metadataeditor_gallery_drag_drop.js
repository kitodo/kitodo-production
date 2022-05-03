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

/* global triggerOnPageDrop, PrimeFaces */

/**
 * Registers event handler that makes relevant components of gallery 
 * draggable and droppable via jquery-ui. 
 * 
 * This is much faster than adding individual <p:draggable/> and 
 * <p:droppable/> primefaces components, which would initialize drag and 
 * drop capabilities individually for each component.
 * Additionally, this reduces the form complexity of the gallery view, which 
 * results in faster form updates for very large galleries.
 */
function registerMakeDragAndDroppable() {

    // define debouce function that will allow to make components draggable in one go
    // after all of them have been updated via Primefaces when reloading the gallery
    function makeDebounced(func, timeout = 100) {
        let timer = null;
        return function () {
            clearTimeout(timer);
            timer = setTimeout(func, timeout);
        };
    }

    function onDrop(event, ui) {
        let dragId = ui.draggable.attr('id');
        let dropId = $(event.target).attr('id');     
        // trigger remote command, see gallery.xhtml and GalleryPanel.onPageDrop
        triggerOnPageDrop([{"name": "dragId", "value": dragId}, {"name": "dropId", "value": dropId}]);
    }

    // apply jquery draggable and droppable to the relevant dom elements
    let makeDragAndDroppable = makeDebounced(function () {
        // make individual pages draggable
        $("#imagePreviewForm\\:structuredPages .draggableStructurePagePanel").draggable({
            scope: "assignedPagesDroppable",
            stack: ".ui-panel",
            revert: true,
        });
        $("#imagePreviewForm\\:unstructuredMediaList .draggableUnstructuredMediaPanel").draggable({
            scope: "assignedPagesDroppable",
            stack: ".ui-panel",
            revert: true,
        });

        // make droppable containers before and after each page
        $("#imagePreviewForm\\:structuredPages .page-drop-area").droppable({
            scope: "assignedPagesDroppable",
            activeClass: "media-stripe-index-active",
            drop: onDrop,
        });
        $("#imagePreviewForm\\:unstructuredMediaList .page-drop-area").droppable({
            scope: "assignedPagesDroppable",
            activeClass: "media-stripe-index-active",
            drop: onDrop,
        });

        // make droppable container for empty stripes
        $("#imagePreviewForm\\:structuredPages .structureElementDataList").has(".ui-datalist-empty-message").droppable({
            scope: "assignedPagesDroppable",
            activeClass: "media-stripe-active",
            drop: onDrop,
        });
        $("#imagePreviewForm\\:unstructuredMediaList").has(".ui-datalist-empty-message").droppable({
            scope: "assignedPagesDroppable",
            activeClass: "media-stripe-active",
            drop: onDrop,
        });
    });

    // update components after they have been updated via PrimeFaces
    let backupFunc = PrimeFaces.ajax.Utils.updateElement;
    PrimeFaces.ajax.Utils.updateElement = function() {
        backupFunc.apply(this, arguments);
        makeDragAndDroppable();
    };

    // make components draggable the very first time the gallery loads
    makeDragAndDroppable();
}

$(function() {
    registerMakeDragAndDroppable();
});
