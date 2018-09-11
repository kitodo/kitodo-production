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


/**
* The function newNameBox() opens a prompt to ask the user for the name of
* the batch. The name is written to the hidden form field "batchName" from
* where it is passed to the field batchTitle in BatchForm where it can be
* picked up by the method createNewBatch() later. If everything is ready
* the function returns true. If no processes have been selected, the user
* is alerted and the function returns false. It will also return false if
    * the user clicks the cancel button in the prompt opening up.
*
* @return true if we are ready to create a batch, false otherwise
*/
function newNameBox(enterBatchName, noProcessSelected) {
    var selectBatches = document.getElementById('processesTabView:batchForm:selectProcesses');
    var size = 0;
    for (var i = 0; i < selectBatches.length; i++) {
        if (selectBatches.options[i].selected) size++;
    }
    if(size === 0){
        alert(noProcessSelected);
        return false;
    }
    var batchName = prompt(enterBatchName, "");
    if(batchName != null){
        document.getElementById('processesTabView:batchForm:batchName').value = batchName;
        return true;
    }else{
        return false;
    }
}

/**
* The function renameBox() opens a prompt to ask the user for the new name
* of the batch. The new name is written to the hidden form field
* "batchName" from where it is passed to the field batchTitle in BatchForm
* where it can be picked up by the method renameBatch() later. If
* everything is ready the function returns true. If none or several batches
* have been selected, the user is alerted and the function returns false.
* It will also return false if the user clicks the cancel button in the
* prompt opening up.
*
* @return true if we are ready to rename, false otherwise
*/
function renameBox(enterBatchName, noBatchSelected, tooManyBatchesSelected) {
    var selectBatches = document.getElementById('processesTabView:batchForm:selectBatches');
    var size = 0;
    for (var i = 0; i < selectBatches.length; i++) {
        if (selectBatches.options[i].selected) size++;
    }
    if(size === 0){
        alert(noBatchSelected);
        return false;
    }
    if(size > 1){
        alert(tooManyBatchesSelected);
        return false;
    }
    var text = selectBatches.options[selectBatches.selectedIndex].text;
    var newName = prompt(enterBatchName, text.replace(/ \[.*?\]$/, "").replace(/ \(.*?\)$/, ""));
    if(newName != null){
        document.getElementById('processesTabView:batchForm:batchName').value = newName;
        return true;
    }else{
        return false;
    }
}
