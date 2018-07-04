/*
 *
 * (c) Kitodo. Key to digital objects e. V. <contact@kitodo.org>
 *
 * This file is part of the Kitodo project.
 *
 * It is licensed under GNU General Public License version 3 or later.
 *
 * For the full copyright and license information, please read the
 * GPL3-License.txt file that was distributed with this source code.
 *
 */

function createMapping(objectTypes) {
    toggleButtons(false, objectTypes);
    document.getElementById('systemTabView:indexing_form:create-mapping-loading').style.display='inline';
    return true;
}

function deleteIndex(objectTypes) {
    toggleButtons(false, objectTypes);
    document.getElementById('systemTabView:indexing_form:delete-index-loading').style.display='inline';
    return true;
}

function toggleProgressPolling(message) {
    if (message.includes("indexing_started")) {
        PF('progressPoll').start();
    } else if (message.includes("indexing_finished")) {
        PF('progressPoll').stop();
        $('.refreshTable').click();
    } else if (message.includes("mapping")) {
        $('.refreshTable').click();
        if (message.endsWith("started")) {
            PF('progressPoll').start();
        } else if (message.endsWith("finished")) {
            document.getElementById('systemTabView:indexing_form:create-mapping-loading').style.display='none';
            PF('progressPoll').stop();
        } else if (message.endsWith("failed")) {
            document.getElementById('systemTabView:indexing_form:create-mapping-loading').style.display='none';
            PF('progressPoll').stop();
        }
    } else if (message.includes("deletion")) {
        $('.refreshTable').click();
        if (message.endsWith("started")) {
            PF('progressPoll').start();
        }
        if (message.endsWith("finished")) {
            document.getElementById('systemTabView:indexing_form:delete-index-loading').style.display='none';
            PF('progressPoll').stop();
        } else if (message.endsWith("failed")) {
            document.getElementById('systemTabView:indexing_form:delete-index-loading').style.display='none';
            PF('progressPoll').stop();
        }
    }
    $('.refreshTable').click();
}

function toggleButtons(state, objectTypes) {
    if(state) {
        PF('createMapping').enable();
        PF('deleteIndex').enable();
        for (var i = 0; i < objectTypes.length; i++) {
            if (objectTypes[i] !== 'NONE') {
                PF('startIndexing' + objectTypes[i]).enable();
            }
        }
        PF('startIndexingAll').enable();
    } else {
        PF('createMapping').disable();
        PF('deleteIndex').disable();
        for (var i = 0; i < objectTypes.length; i++) {
            if (objectTypes[i] !== 'NONE') {
                PF('startIndexing' + objectTypes[i]).disable();
            }
        }
        PF('startIndexingAll').disable();
    }
}
