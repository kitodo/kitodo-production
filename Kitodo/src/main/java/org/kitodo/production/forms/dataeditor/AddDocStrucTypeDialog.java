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

package org.kitodo.production.forms.dataeditor;

import static org.kitodo.production.metadata.InsertionPosition.AFTER_CURRENT_ELEMENT;
import static org.kitodo.production.metadata.InsertionPosition.BEFOR_CURRENT_ELEMENT;
import static org.kitodo.production.metadata.InsertionPosition.FIRST_CHILD_OF_CURRENT_ELEMENT;
import static org.kitodo.production.metadata.InsertionPosition.LAST_CHILD_OF_CURRENT_ELEMENT;
import static org.kitodo.production.metadata.InsertionPosition.PARENT_OF_CURRENT_ELEMENT;

import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;

import javax.faces.model.SelectItem;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kitodo.api.dataeditor.rulesetmanagement.MetadataViewInterface;
import org.kitodo.api.dataeditor.rulesetmanagement.StructuralElementViewInterface;
import org.kitodo.api.dataformat.IncludedStructuralElement;
import org.kitodo.api.dataformat.MediaUnit;
import org.kitodo.api.dataformat.View;
import org.kitodo.data.database.beans.Process;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.data.exceptions.DataException;
import org.kitodo.production.dto.ProcessDTO;
import org.kitodo.production.helper.Helper;
import org.kitodo.production.metadata.InsertionPosition;
import org.kitodo.production.metadata.MetadataEditor;
import org.kitodo.production.services.ServiceManager;
import org.primefaces.PrimeFaces;

/**
 * Backing bean for the add doc struc type dialog of the meta-data editor.
 */
public class AddDocStrucTypeDialog {
    private static final Logger logger = LogManager.getLogger(AddDocStrucTypeDialog.class);

    private final DataEditorForm dataEditor;
    private List<SelectItem> docStructAddTypeSelectionItemsForChildren;
    private List<SelectItem> docStructAddTypeSelectionItemsForParent;
    private List<SelectItem> docStructAddTypeSelectionItemsForSiblings;
    private String docStructAddTypeSelectionSelectedItem;
    private List<SelectItem> docStructPositionSelectionItems;
    private InsertionPosition docStructPositionSelectionSelectedItem = LAST_CHILD_OF_CURRENT_ELEMENT;
    private int elementsToAddSpinnerValue;
    private String inputMetaDataValue = "";
    private LinkedList<IncludedStructuralElement> parents;
    private List<SelectItem> selectAddableMetadataTypesItems;
    private String selectAddableMetadataTypesSelectedItem = "";
    private String selectFirstPageOnAddNodeSelectedItem;
    private String selectLastPageOnAddNodeSelectedItem;
    private List<SelectItem> selectPageOnAddNodeItems;
    private AddDocStrucTypeDialogMode subdialog = AddDocStrucTypeDialogMode.ADD_MULTIPLE_LOGICAL_ELEMENTS;
    private String processNumber = "";
    private Process selectedProcess;
    private List<ProcessDTO> processes = Collections.emptyList();
    private BigInteger orderSpinnerValue;

    /**
     * Backing bean for the add doc struc type dialog of the meta-data editor.
     *
     * @see "WEB-INF/templates/includes/metadataEditor/dialogs/addDocStrucType.xhtml"
     */
    AddDocStrucTypeDialog(DataEditorForm dataEditor) {
        this.dataEditor = dataEditor;
    }

    /**
     * This method is invoked if the user clicks on the add multi doc struc
     * submit btn command button.
     */
    public void addMultiDocStruc() {
        if (dataEditor.getSelectedStructure().isPresent()) {
            MetadataEditor.addMultipleStructures(elementsToAddSpinnerValue, docStructAddTypeSelectionSelectedItem,
                dataEditor.getWorkpiece(), dataEditor.getSelectedStructure().get(),
                docStructPositionSelectionSelectedItem, selectAddableMetadataTypesSelectedItem,
                    inputMetaDataValue);
            dataEditor.refreshStructurePanel();
        }
    }

    /**
     * This method is invoked if the user clicks on the add single doc struc
     * submit btn command button.
     */
    public void addSingleDocStruc() {
        if (dataEditor.getSelectedStructure().isPresent()) {
            MetadataEditor.addStructure(docStructAddTypeSelectionSelectedItem, dataEditor.getWorkpiece(),
                dataEditor.getSelectedStructure().get(), docStructPositionSelectionSelectedItem, getViewsToAdd());
            dataEditor.refreshStructurePanel();
        }
    }

    /**
     * Returns the selected item of the docStructAddTypeSelection drop-down
     * menu.
     *
     * @return the selected item of the docStructAddTypeSelection
     */
    public List<SelectItem> getDocStructAddTypeSelectionItems() {
        switch (docStructPositionSelectionSelectedItem) {
            case AFTER_CURRENT_ELEMENT:
            case BEFOR_CURRENT_ELEMENT:
                return docStructAddTypeSelectionItemsForSiblings;
            case FIRST_CHILD_OF_CURRENT_ELEMENT:
            case LAST_CHILD_OF_CURRENT_ELEMENT:
                return docStructAddTypeSelectionItemsForChildren;
            case PARENT_OF_CURRENT_ELEMENT:
                return docStructAddTypeSelectionItemsForParent;
            default:
                return Collections.emptyList();
        }
    }

    /**
     * Return selected doc struct type.
     *
     * @return selected doc struct type
     */
    public String getDocStructAddTypeSelectionSelectedItem() {
        return docStructAddTypeSelectionSelectedItem;
    }

    /**
     * Sets the selected item of the docStructAddTypeSelection drop-down menu.
     *
     * @param docStructAddTypeSelectionSelectedItem
     *            selected item to set
     */
    public void setDocStructAddTypeSelectionSelectedItem(String docStructAddTypeSelectionSelectedItem) {
        this.docStructAddTypeSelectionSelectedItem = docStructAddTypeSelectionSelectedItem;
    }

    /**
     * Returns the items of the docStructPositionSelection drop-down menu.
     *
     * @return the items of the docStructPositionSelection
     */
    public List<SelectItem> getDocStructPositionSelectionItems() {
        return docStructPositionSelectionItems;
    }

    /**
     * Returns the selected item of the docStructPositionSelection drop-down
     * menu.
     *
     * @return the selected item of the docStructPositionSelection
     */
    public InsertionPosition getDocStructPositionSelectionSelectedItem() {
        return docStructPositionSelectionSelectedItem;
    }

    /**
     * Sets the selected item of the docStructPositionSelection drop-down menu.
     *
     * @param docStructPositionSelectionSelectedItem
     *            selected item to set
     */
    public void setDocStructPositionSelectionSelectedItem(InsertionPosition docStructPositionSelectionSelectedItem) {
        this.docStructPositionSelectionSelectedItem = docStructPositionSelectionSelectedItem;
    }

    /**
     * Returns the value of the elementsToAddSpinner number input box.
     *
     * @return the value of the elementsToAddSpinner
     */
    public int getElementsToAddSpinnerValue() {
        return elementsToAddSpinnerValue;
    }

    /**
     * Sets the value of the elementsToAddSpinner number input box.
     *
     * @param elementsToAddSpinnerValue
     *            value to set
     */
    public void setElementsToAddSpinnerValue(int elementsToAddSpinnerValue) {
        this.elementsToAddSpinnerValue = elementsToAddSpinnerValue;
    }

    /**
     * Returns the value of the inputMetaData text box.
     *
     * @return the value of the inputMetaData
     */
    public String getInputMetaDataValue() {
        return inputMetaDataValue;
    }

    /**
     * Sets the value of the inputMetaData text box.
     *
     * @param inputMetaDataValue
     *            value to set
     */
    public void setInputMetaDataValue(String inputMetaDataValue) {
        this.inputMetaDataValue = inputMetaDataValue;
    }

    /**
     * Returns the items of the selectAddableMetadataTypes drop-down menu.
     *
     * @return the items of the selectAddableMetadataTypes
     */
    public List<SelectItem> getSelectAddableMetadataTypesItems() {
        return selectAddableMetadataTypesItems;
    }

    /**
     * Returns the selected item of the selectAddableMetadataTypes drop-down
     * menu.
     *
     * @return the selected item of the selectAddableMetadataTypes
     */
    public String getSelectAddableMetadataTypesSelectedItem() {
        return selectAddableMetadataTypesSelectedItem;
    }

    /**
     * Sets the selected item of the selectAddableMetadataTypes drop-down menu.
     *
     * @param selectAddableMetadataTypesSelectedItem
     *            selected item to set
     */
    public void setSelectAddableMetadataTypesSelectedItem(String selectAddableMetadataTypesSelectedItem) {
        this.selectAddableMetadataTypesSelectedItem = selectAddableMetadataTypesSelectedItem;
    }

    /**
     * Returns the selected item of the selectFirstPageOnAddNode drop-down menu.
     *
     * @return the selected item of the selectFirstPageOnAddNode
     */
    public String getSelectFirstPageOnAddNodeSelectedItem() {
        return selectFirstPageOnAddNodeSelectedItem;
    }

    /**
     * Sets the selected item of the selectFirstPageOnAddNode drop-down menu.
     *
     * @param selectFirstPageOnAddNodeSelectedItem
     *            selected item to set
     */
    public void setSelectFirstPageOnAddNodeSelectedItem(String selectFirstPageOnAddNodeSelectedItem) {
        this.selectFirstPageOnAddNodeSelectedItem = selectFirstPageOnAddNodeSelectedItem;
    }

    /**
     * Returns the selected item of the selectLastPageOnAddNode drop-down menu.
     *
     * @return the selected item of the selectLastPageOnAddNode
     */
    public String getSelectLastPageOnAddNodeSelectedItem() {
        return selectLastPageOnAddNodeSelectedItem;
    }

    /**
     * Sets the selected item of the selectLastPageOnAddNode drop-down menu.
     *
     * @param selectLastPageOnAddNodeSelectedItem
     *            selected item to set
     */
    public void setSelectLastPageOnAddNodeSelectedItem(String selectLastPageOnAddNodeSelectedItem) {
        this.selectLastPageOnAddNodeSelectedItem = selectLastPageOnAddNodeSelectedItem;
    }

    /**
     * Returns the items of the selectFirstPageOnAddNode and
     * selectLastPageOnAddNode drop-down menus.
     *
     * @return the items of the selectFirstPageOnAddNode and
     *         selectLastPageOnAddNode
     */
    public List<SelectItem> getSelectPageOnAddNodeItems() {
        return selectPageOnAddNodeItems;
    }

    /**
     * Returns the number of the subdialog to be displayed. This dialog has
     * several sub-dialogs for switching. Strictly speaking, there are three (1
     * to 3).
     *
     * @return subdialog by number
     */
    public int getSubdialog() {
        return Arrays.binarySearch(AddDocStrucTypeDialogMode.values(), subdialog) + 1;
    }

    /**
     * Sets the subdialog selected by the user.
     *
     * @param subdialog
     *            subdialog, from 1 to 3
     */
    public void setSubdialog(int subdialog) {
        this.subdialog = AddDocStrucTypeDialogMode.values()[subdialog - 1];
    }

    private List<View> getViewsToAdd() {
        try {
            int firstPage = Integer.parseInt(selectFirstPageOnAddNodeSelectedItem);
            int lastPage = Integer.parseInt(selectLastPageOnAddNodeSelectedItem);
            return dataEditor.getEditPagesDialog().getViewsToAdd(firstPage, lastPage);
        } catch (NumberFormatException e) {
            // user didn’t select both start and end page
            logger.catching(Level.TRACE, e);
            return Collections.emptyList();
        }
    }

    /**
     * Prepare popup dialog by retrieving available insertion positions and doc struct types for selected element.
     */
    public void prepare() {
        if (dataEditor.getSelectedStructure().isPresent()) {
            this.parents = MetadataEditor.getAncestorsOfStructure(dataEditor.getSelectedStructure().get(),
                dataEditor.getWorkpiece().getRootElement());
            prepareDocStructPositionSelectionItems(parents.isEmpty());
            prepareSelectAddableMetadataTypesItems();
        } else {
            docStructPositionSelectionItems = Collections.emptyList();
            selectAddableMetadataTypesItems = Collections.emptyList();
        }
        this.prepareDocStructTypes();
        prepareSelectPageOnAddNodeItems();
    }

    /**
     * Update lists of available doc struct types that can be added to the currently selected structure element in the
     * currently selected position.
     */
    public void prepareDocStructTypes() {
        if (dataEditor.getSelectedStructure().isPresent()) {
            this.parents = MetadataEditor.getAncestorsOfStructure(dataEditor.getSelectedStructure().get(),
                    dataEditor.getWorkpiece().getRootElement());
            if (parents.isEmpty()) {
                docStructAddTypeSelectionItemsForParent = Collections.emptyList();
            } else {
                prepareDocStructAddTypeSelectionItemsForParent();
            }
            prepareDocStructAddTypeSelectionItemsForChildren();
            prepareDocStructAddTypeSelectionItemsForSiblings();
        } else {
            docStructAddTypeSelectionItemsForChildren = Collections.emptyList();
            docStructAddTypeSelectionItemsForParent = Collections.emptyList();
            docStructAddTypeSelectionItemsForSiblings = Collections.emptyList();
        }
    }

    private void prepareDocStructAddTypeSelectionItemsForChildren() {
        docStructAddTypeSelectionItemsForChildren = new ArrayList<>();
        StructuralElementViewInterface divisionView = dataEditor.getRuleset().getStructuralElementView(
            dataEditor.getSelectedStructure().orElseThrow(IllegalStateException::new).getType(),
            dataEditor.getAcquisitionStage(), dataEditor.getPriorityList());
        for (Entry<String, String> entry : divisionView.getAllowedSubstructuralElements().entrySet()) {
            docStructAddTypeSelectionItemsForChildren.add(new SelectItem(entry.getKey(), entry.getValue()));
        }
    }

    private void prepareDocStructAddTypeSelectionItemsForParent() {
        docStructAddTypeSelectionItemsForParent = new ArrayList<>();
        if (!parents.isEmpty()) {
            StructuralElementViewInterface parentDivisionView = dataEditor.getRuleset().getStructuralElementView(
                parents.getLast().getType(), dataEditor.getAcquisitionStage(), dataEditor.getPriorityList());
            for (Entry<String, String> entry : parentDivisionView.getAllowedSubstructuralElements().entrySet()) {
                String newParent = entry.getKey();
                StructuralElementViewInterface newParentDivisionView = dataEditor.getRuleset().getStructuralElementView(
                    newParent, dataEditor.getAcquisitionStage(), dataEditor.getPriorityList());
                if (newParentDivisionView.getAllowedSubstructuralElements().containsKey(
                    dataEditor.getSelectedStructure().orElseThrow(IllegalStateException::new).getType())) {
                    docStructAddTypeSelectionItemsForParent.add(new SelectItem(newParent, entry.getValue()));
                }
            }
        }
    }

    private void prepareDocStructAddTypeSelectionItemsForSiblings() {
        docStructAddTypeSelectionItemsForSiblings = new ArrayList<>();
        if (!parents.isEmpty()) {
            StructuralElementViewInterface parentDivisionView = dataEditor.getRuleset().getStructuralElementView(
                parents.getLast().getType(), dataEditor.getAcquisitionStage(), dataEditor.getPriorityList());
            for (Entry<String, String> entry : parentDivisionView.getAllowedSubstructuralElements().entrySet()) {
                docStructAddTypeSelectionItemsForSiblings.add(new SelectItem(entry.getKey(), entry.getValue()));
            }
        }
    }

    private void prepareDocStructPositionSelectionItems(boolean rootNode) {
        docStructPositionSelectionItems = new ArrayList<>();
        if (!rootNode) {
            docStructPositionSelectionItems.add(new SelectItem(BEFOR_CURRENT_ELEMENT,
                    Helper.getTranslation("vorDasAktuelleElement")));
            docStructPositionSelectionItems.add(new SelectItem(AFTER_CURRENT_ELEMENT,
                    Helper.getTranslation("hinterDasAktuelleElement")));
        }
        docStructPositionSelectionItems.add(new SelectItem(FIRST_CHILD_OF_CURRENT_ELEMENT,
                Helper.getTranslation("asFirstChildOfCurrentElement")));
        docStructPositionSelectionItems.add(new SelectItem(LAST_CHILD_OF_CURRENT_ELEMENT,
                Helper.getTranslation("asLastChildOfCurrentElement")));
        docStructPositionSelectionItems.add(new SelectItem(PARENT_OF_CURRENT_ELEMENT,
                Helper.getTranslation("asParentOfCurrentElement")));
    }

    private void prepareSelectAddableMetadataTypesItems() {
        selectAddableMetadataTypesItems = new ArrayList<>();
        Collection<MetadataViewInterface> addableMetadata = getStructuralElementView()
                .getAddableMetadata(Collections.emptyMap(), Collections.emptyList());
        for (MetadataViewInterface keyView : addableMetadata) {
            selectAddableMetadataTypesItems.add(new SelectItem(keyView.getId(), keyView.getLabel()));
        }
    }

    private StructuralElementViewInterface getStructuralElementView() {
        return dataEditor.getRuleset()
                .getStructuralElementView(
                        dataEditor.getSelectedStructure().orElseThrow(IllegalStateException::new).getType(),
                        dataEditor.getAcquisitionStage(), dataEditor.getPriorityList());
    }

    private void prepareSelectPageOnAddNodeItems() {
        List<MediaUnit> mediaUnits = dataEditor.getWorkpiece().getMediaUnits();
        selectPageOnAddNodeItems = new ArrayList<>(mediaUnits.size());
        for (int i = 0; i < mediaUnits.size(); i++) {
            MediaUnit mediaUnit = mediaUnits.get(i);
            String label = Objects.isNull(mediaUnit.getOrderlabel()) ? Integer.toString(mediaUnit.getOrder())
                    : mediaUnit.getOrder() + " : " + mediaUnit.getOrderlabel();
            selectPageOnAddNodeItems.add(new SelectItem(Integer.toString(i), label));
        }
    }

    /**
     * Returns the process number. The process number is an input field where
     * the user can enter the process number or the process title, and then it
     * is searched for. But search is only when the button is clicked (too much
     * load otherwise).
     *
     * @return the process number
     */
    public String getProcessNumber() {
        return processNumber;
    }

    /**
     * Sets the process number when the user entered it.
     *
     * @param processNumber
     *            process number to set
     */
    public void setProcessNumber(String processNumber) {
        this.processNumber = processNumber;
    }

    /**
     * Function for the button for the search. Looks for suitable processes. If
     * the process number is a number and the process exists, it is already
     * found. Otherwise it must be searched for, excluding the wrong ruleset or
     * the wrong client.
     */
    public void searchButtonClick() {
        if (processNumber.trim().isEmpty()) {
            alert(Helper.getTranslation("dialogAddDocStrucType.searchButtonClick.empty"));
            return;
        }
        try {
            Set<String> allowedSubstructuralElements = getStructuralElementView().getAllowedSubstructuralElements()
                    .keySet();
            processes = ServiceManager.getProcessService().findLinkableProcesses(processNumber,
                dataEditor.getProcess().getRuleset().getId(), allowedSubstructuralElements);
            if (processes.isEmpty()) {
                alert(Helper.getTranslation("dialogAddDocStrucType.searchButtonClick.noHits"));
            }
        } catch (DataException | IOException | DAOException e) {
            logger.catching(e);
            alert(Helper.getTranslation("dialogAddDocStrucType.searchButtonClick.error", e.getMessage()));
        }
    }

    /**
     * Displays a dialog box with a message to the user.
     *
     * @param message
     *            message to show
     */
    private void alert(String message) {
        PrimeFaces.current().executeScript("alert('" + message + "');");
    }

    /**
     * Returns the process selected by the user in the drop-down list.
     *
     * @return the selected process
     */
    public Process getSelectedProcess() {
        return selectedProcess;
    }

    /**
     * Sets the number of the process selected by the user.
     *
     * @param selectedProcess
     *            selected process
     */
    public void setSelectedProcess(Process selectedProcess) {
        this.selectedProcess = selectedProcess;
    }

    /**
     * Returns the list of items to populate the drop-down list to select a
     * process.
     *
     * @return the list of processes
     */
    public List<ProcessDTO> getProcesses() {
        return processes;
    }

    /**
     * Returns the value of the order spinner. The order has an influence on the
     * order when adding links.
     *
     * @return the value of the order spinner
     */
    public BigInteger getOrderSpinnerValue() {
        return orderSpinnerValue;
    }

    /**
     * Sets the value of the order spinner if the user has entered a value here.
     *
     * @param orderSpinnerValue
     *            value to set
     */
    public void setOrderSpinnerValue(BigInteger orderSpinnerValue) {
        this.orderSpinnerValue = orderSpinnerValue;
    }

    /**
     * Adds the link when the user clicks OK.
     */
    public void addLinkButtonClick() {
        if (processNumber.trim().isEmpty()) {
            alert(Helper.getTranslation("dialogAddDocStrucType.searchButtonClick.empty"));
            return;
        } else {
            try {
                selectedProcess = ServiceManager.getProcessService().getById(Integer.valueOf(processNumber.trim()));
            } catch (DAOException e) {
                logger.catching(Level.TRACE, e);
                alert(Helper.getTranslation("dialogAddDocStrucType.searchButtonClick.empty"));
            }
        }
        dataEditor.getCurrentChildren().add(selectedProcess);
        MetadataEditor.addLink(dataEditor.getSelectedStructure().orElseThrow(IllegalStateException::new),
            orderSpinnerValue, selectedProcess.getId());
        dataEditor.getStructurePanel().show(true);
        if (processNumber.trim().equals(Integer.toString(selectedProcess.getId()))) {
            alert(Helper.getTranslation("dialogAddDocStrucType.searchButtonClick.hint"));
        }
        processNumber = "";
        processes = Collections.emptyList();
        orderSpinnerValue = null;
    }
}
