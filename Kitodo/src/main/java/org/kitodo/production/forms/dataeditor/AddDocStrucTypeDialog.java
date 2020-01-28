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
import static org.kitodo.production.metadata.InsertionPosition.BEFORE_CURRENT_ELEMENT;
import static org.kitodo.production.metadata.InsertionPosition.FIRST_CHILD_OF_CURRENT_ELEMENT;
import static org.kitodo.production.metadata.InsertionPosition.LAST_CHILD_OF_CURRENT_ELEMENT;
import static org.kitodo.production.metadata.InsertionPosition.PARENT_OF_CURRENT_ELEMENT;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import javax.faces.model.SelectItem;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kitodo.api.Metadata;
import org.kitodo.api.dataeditor.rulesetmanagement.MetadataViewInterface;
import org.kitodo.api.dataeditor.rulesetmanagement.SimpleMetadataViewInterface;
import org.kitodo.api.dataeditor.rulesetmanagement.StructuralElementViewInterface;
import org.kitodo.api.dataformat.IncludedStructuralElement;
import org.kitodo.api.dataformat.MediaUnit;
import org.kitodo.api.dataformat.View;
import org.kitodo.data.database.beans.Process;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.data.exceptions.DataException;
import org.kitodo.exceptions.InvalidMetadataValueException;
import org.kitodo.exceptions.UnknownTreeNodeDataException;
import org.kitodo.production.dto.ProcessDTO;
import org.kitodo.production.forms.createprocess.ProcessDetail;
import org.kitodo.production.helper.Helper;
import org.kitodo.production.metadata.InsertionPosition;
import org.kitodo.production.metadata.MetadataEditor;
import org.kitodo.production.services.ServiceManager;
import org.primefaces.PrimeFaces;
import org.primefaces.model.TreeNode;

/**
 * Backing bean for the add doc struc type dialog of the metadata editor.
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
    private List<View> preselectedViews;
    private String processNumber = "";
    private Process selectedProcess;
    private List<Process> processes = Collections.emptyList();
    private boolean linkSubDialogVisible = false;
    private static final String PREVIEW_MODE = "preview";
    private static final String LIST_MODE = "list";

    /**
     * Backing bean for the add doc struc type dialog of the metadata editor.
     *
     * @see "WEB-INF/templates/includes/metadataEditor/dialogs/addDocStrucType.xhtml"
     */
    AddDocStrucTypeDialog(DataEditorForm dataEditor) {
        this.dataEditor = dataEditor;
    }

    /**
     * Check and return if sub dialog is visible.
     *
     * @return sub dialog visibility
     */
    public boolean isLinkSubDialogVisible() {
        return this.linkSubDialogVisible;
    }

    /**
     * Set sub dialog visibility.
     *
     * @param visible whether sub dialog is visible or not
     */
    public void setLinkSubDialogVisible(boolean visible) {
        this.linkSubDialogVisible = visible;
    }

    /**
     * Add structure element.
     */
    public void addDocStruc(boolean preview) {
        if (this.elementsToAddSpinnerValue > 1) {
            this.addMultiDocStruc();
        } else {
            this.addSingleDocStruc(preview);
        }
        if (preview && (!(StringUtils.isEmpty(selectFirstPageOnAddNodeSelectedItem)
                || StringUtils.isEmpty(this.selectLastPageOnAddNodeSelectedItem))
                || Objects.nonNull(this.preselectedViews) && this.preselectedViews.size() > 0)) {
            dataEditor.getGalleryPanel().setGalleryViewMode(PREVIEW_MODE);
        } else {
            dataEditor.getGalleryPanel().setGalleryViewMode(LIST_MODE);
        }
        try {
            dataEditor.getStructurePanel().preserve();
            dataEditor.refreshStructurePanel();
        } catch (UnknownTreeNodeDataException e) {
            Helper.setErrorMessage(e.getMessage());
        }
    }

    /**
     * This method is invoked if the user clicks on the add multi doc struc
     * submit btn command button.
     */
    private void addMultiDocStruc() {
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
    private void addSingleDocStruc(boolean selectViews) {
        if (dataEditor.getSelectedStructure().isPresent()) {
            IncludedStructuralElement newStructure = MetadataEditor.addStructure(docStructAddTypeSelectionSelectedItem,
                    dataEditor.getWorkpiece(), dataEditor.getSelectedStructure().get(),
                    docStructPositionSelectionSelectedItem, getViewsToAdd());
            dataEditor.getSelectedMedia().clear();
            if (selectViews) {
                for (View view : getViewsToAdd()) {
                    dataEditor.getSelectedMedia().add(new ImmutablePair<>(view.getMediaUnit(), newStructure));
                }
            }
            dataEditor.refreshStructurePanel();
            TreeNode selectedLogicalTreeNode = dataEditor.getStructurePanel().updateLogicalNodeSelectionRecursive(newStructure,
                    this.dataEditor.getStructurePanel().getLogicalTree());
            if (Objects.nonNull(selectedLogicalTreeNode)) {
                this.dataEditor.getStructurePanel().setSelectedLogicalNode(selectedLogicalTreeNode);
                this.dataEditor.getMetadataPanel().showLogical(this.dataEditor.getSelectedStructure());
            }
            List<Pair<MediaUnit, IncludedStructuralElement>> selectedMedia = this.dataEditor.getSelectedMedia().stream()
                    .sorted(Comparator.comparingInt(p -> p.getLeft().getOrder()))
                    .collect(Collectors.toList());
            Collections.reverse(selectedMedia);
            this.dataEditor.setSelectedMedia(selectedMedia);
        }
    }

    /**
     * Returns the selected item of the docStructAddTypeSelection drop-down
     * menu.
     *
     * @return the selected item of the docStructAddTypeSelection
     */
    public List<SelectItem> getDocStructAddTypeSelectionItems() {
        if (Objects.isNull(docStructPositionSelectionSelectedItem)) {
            return Collections.emptyList();
        }
        switch (docStructPositionSelectionSelectedItem) {
            case AFTER_CURRENT_ELEMENT:
            case BEFORE_CURRENT_ELEMENT:
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
        dataEditor.getMetadataPanel().setAddMetadataKeySelectedItem(selectAddableMetadataTypesSelectedItem);
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

    private List<View> getViewsToAdd() {
        if (Objects.nonNull(preselectedViews) && preselectedViews.size() > 0) {
            return preselectedViews;
        }
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
            prepareSelectAddableMetadataTypesItems(true);
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
            docStructPositionSelectionItems.add(new SelectItem(BEFORE_CURRENT_ELEMENT,
                    Helper.getTranslation("dataEditor.position.beforeCurrentElement")));
            docStructPositionSelectionItems.add(new SelectItem(AFTER_CURRENT_ELEMENT,
                    Helper.getTranslation("dataEditor.position.afterCurrentElement")));
        }
        docStructPositionSelectionItems.add(new SelectItem(FIRST_CHILD_OF_CURRENT_ELEMENT,
                Helper.getTranslation("dataEditor.position.asFirstChildOfCurrentElement")));
        docStructPositionSelectionItems.add(new SelectItem(LAST_CHILD_OF_CURRENT_ELEMENT,
                Helper.getTranslation("dataEditor.position.asLastChildOfCurrentElement")));
        docStructPositionSelectionItems.add(new SelectItem(PARENT_OF_CURRENT_ELEMENT,
                Helper.getTranslation("dataEditor.position.asParentOfCurrentElement")));
    }

    /**
     * Prepare the list of available Metadata that can be added to the currently selected structure element.
     *
     * @param currentElement flag controlling whether to return a list of metadata for the currently selected logical
     *                       structure element (currentElement = true) or for a new element to be added to the structure
     *                       (currentElement = false)
     * @param metadataNodes list of TreeNodes containing the metadata that is already assigned to the structure element
     */
    public void prepareSelectAddableMetadataTypesItems(boolean currentElement, List<TreeNode> metadataNodes) {
        selectAddableMetadataTypesItems = new ArrayList<>();
        setSelectAddableMetadataTypesSelectedItem("");
        Map<Metadata, String> existingMetadata = Collections.emptyMap();
        StructuralElementViewInterface structure;
        try {
            if (currentElement) {
                structure = getStructuralElementView();
                existingMetadata = getExistingMetadataRows(metadataNodes);
            } else {
                structure = dataEditor.getRuleset()
                        .getStructuralElementView(docStructAddTypeSelectionSelectedItem,
                                dataEditor.getAcquisitionStage(), dataEditor.getPriorityList());
            }
            for (MetadataViewInterface keyView : structure.getAddableMetadata(existingMetadata,
                    Collections.emptyList())) {
                selectAddableMetadataTypesItems.add(
                        new SelectItem(keyView.getId(), keyView.getLabel(),
                                keyView instanceof SimpleMetadataViewInterface
                                        ? ((SimpleMetadataViewInterface) keyView).getInputType().toString()
                                        : "dataTable"));
            }
        } catch (InvalidMetadataValueException e) {
            Helper.setErrorMessage(e);
        }
    }

    /**
     * Convenience function to call metadata preparation with a single parameter.
     *
     * @param currentElement flag controlling whether to return a list of metadata for the currently selected logical
     *                       structure element (currentElement = true) or for a new element to be added to the structure
     *                       (currentElement = false)
     */
    public void prepareSelectAddableMetadataTypesItems(boolean currentElement) {
        prepareSelectAddableMetadataTypesItems(currentElement, Collections.emptyList());
    }

    private Map<Metadata, String> getExistingMetadataRows(List<TreeNode> metadataTreeNodes) throws InvalidMetadataValueException {
        Map<Metadata, String> existingMetadataRows = new HashMap<>();

        for (TreeNode metadataNode : metadataTreeNodes) {
            if (metadataNode.getData() instanceof ProcessDetail) {
                try {
                    for (Metadata metadata : ((ProcessDetail) metadataNode.getData()).getMetadata()) {
                        existingMetadataRows.put(metadata, metadata.getKey());
                    }
                } catch (NullPointerException e) {
                    logger.error(e);
                }
            }
        }

        return existingMetadataRows;
    }

    private StructuralElementViewInterface getStructuralElementView() {
        if (dataEditor.getSelectedStructure().isPresent()) {
            return dataEditor.getRuleset()
                    .getStructuralElementView(
                            dataEditor.getSelectedStructure().get().getType(),
                            dataEditor.getAcquisitionStage(), dataEditor.getPriorityList());
        } else {
            TreeNode selectedLogicalNode = dataEditor.getStructurePanel().getSelectedLogicalNode();
            if (Objects.nonNull(selectedLogicalNode)
                    && selectedLogicalNode.getData() instanceof StructureTreeNode) {
                StructureTreeNode structureTreeNode = (StructureTreeNode) selectedLogicalNode.getData();
                if (structureTreeNode.getDataObject() instanceof View) {
                    View view = (View) structureTreeNode.getDataObject();
                    if (Objects.nonNull(view.getMediaUnit())) {
                        return dataEditor.getRuleset().getStructuralElementView(view.getMediaUnit().getType(),
                                dataEditor.getAcquisitionStage(), dataEditor.getPriorityList());
                    }
                }
            }
        }
        throw new IllegalStateException();
    }

    private void prepareSelectPageOnAddNodeItems() {
        List<MediaUnit> mediaUnits = dataEditor.getWorkpiece().getAllMediaUnitsSorted();
        selectPageOnAddNodeItems = new ArrayList<>(mediaUnits.size());
        for (int i = 0; i < mediaUnits.size(); i++) {
            MediaUnit mediaUnit = mediaUnits.get(i);
            String label = Objects.isNull(mediaUnit.getOrderlabel()) ? Integer.toString(mediaUnit.getOrder())
                    : mediaUnit.getOrder() + " : " + mediaUnit.getOrderlabel();
            selectPageOnAddNodeItems.add(new SelectItem(Integer.toString(i), label));
        }
    }

    /**
     * Fill preselectedViews with the views matching the List of selectedMedia.
     */
    public void preparePreselectedViews() {
        preselectedViews = new ArrayList<>();
        List<Pair<MediaUnit, IncludedStructuralElement>> selectedMedia = dataEditor.getSelectedMedia();
        for (Pair<MediaUnit, IncludedStructuralElement> pair : selectedMedia) {
            for (View view : pair.getValue().getViews()) {
                if (Objects.equals(view.getMediaUnit(), pair.getKey())) {
                    preselectedViews.add(view);
                }
            }
        }
        preselectedViews.sort(Comparator.comparingInt(v -> v.getMediaUnit().getOrder()));
    }

    /**
     * Reset values.
     */
    public void resetValues() {
        preselectedViews = Collections.emptyList();
        processNumber = "";
        processes = Collections.emptyList();
        linkSubDialogVisible = false;
        inputMetaDataValue = "";
        elementsToAddSpinnerValue = 1;
        selectFirstPageOnAddNodeSelectedItem = null;
        selectLastPageOnAddNodeSelectedItem = null;
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
    public void search() {
        if (processNumber.trim().isEmpty()) {
            alert(Helper.getTranslation("dialogAddDocStrucType.searchButtonClick.empty"));
            return;
        }
        try {
            Set<String> allowedSubstructuralElements = getStructuralElementView().getAllowedSubstructuralElements()
                    .keySet();
            List<Integer> ids = ServiceManager.getProcessService().findLinkableChildProcesses(processNumber,
                dataEditor.getProcess().getRuleset().getId(), allowedSubstructuralElements)
                    .stream().map(ProcessDTO::getId).collect(Collectors.toList());
            if (ids.isEmpty()) {
                alert(Helper.getTranslation("dialogAddDocStrucType.searchButtonClick.noHits"));
            }
            processes = new LinkedList<>();
            for (int processId : ids) {
                processes.add(ServiceManager.getProcessService().getById(processId));
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
    public List<Process> getProcesses() {
        return processes;
    }

    /**
     * Get preselectedViews.
     *
     * @return value of preselectedViews
     */
    public List<View> getPreselectedViews() {
        return preselectedViews;
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
            selectedProcess.getId());
        dataEditor.getStructurePanel().show(true);
        if (processNumber.trim().equals(Integer.toString(selectedProcess.getId()))) {
            alert(Helper.getTranslation("dialogAddDocStrucType.searchButtonClick.hint"));
        }
        processNumber = "";
        processes = Collections.emptyList();
    }

    /**
     * Return the label of the currently selected structure.
     *
     * @return label of the currently selected structure
     */
    public String getCurrentStructureLabel() {
        if (dataEditor.getSelectedStructure().isPresent()) {
            StructuralElementViewInterface divisionView = dataEditor.getRuleset().getStructuralElementView(
                    dataEditor.getSelectedStructure().get().getType(), dataEditor.getAcquisitionStage(),
                    dataEditor.getPriorityList());
            return divisionView.getLabel();
        } else {
            return " - ";
        }
    }
}
