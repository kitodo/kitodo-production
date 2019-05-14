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

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Objects;

import javax.faces.model.SelectItem;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kitodo.api.dataeditor.rulesetmanagement.MetadataViewInterface;
import org.kitodo.api.dataeditor.rulesetmanagement.StructuralElementViewInterface;
import org.kitodo.api.dataformat.IncludedStructuralElement;
import org.kitodo.api.dataformat.MediaUnit;
import org.kitodo.api.dataformat.View;
import org.kitodo.production.helper.Helper;
import org.kitodo.production.metadata.InsertionPosition;
import org.kitodo.production.metadata.MetadataEditor;

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
    private boolean showingAddMultipleLogicalElements;

    /**
     * Adds a new doc struc type dialog.
     */
    AddDocStrucTypeDialog(DataEditorForm dataEditor) {
        this.dataEditor = dataEditor;
    }

    /**
     * This method is invoked if the user clicks on the add multi doc struc
     * submit btn command button.
     */
    public void addMultiDocStrucSubmitBtnClick() {
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
    public void addSingleDocStrucSubmitBtnClick() {
        if (dataEditor.getSelectedStructure().isPresent()) {
            MetadataEditor.addStructure(docStructAddTypeSelectionSelectedItem, dataEditor.getWorkpiece(),
                dataEditor.getSelectedStructure().get(), docStructPositionSelectionSelectedItem, getViewsToAdd());
            dataEditor.refreshStructurePanel();
        }
    }

    /**
     * Returns the doc struct add type selection items of this add doc struc
     * type dialog.
     *
     * @return the doc struct add type selection items
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
     * Set selected doc struct type.
     *
     * @param docStructAddTypeSelectionSelectedItem
     *          selected doc struct type
     */
    public void setDocStructAddTypeSelectionSelectedItem(String docStructAddTypeSelectionSelectedItem) {
        this.docStructAddTypeSelectionSelectedItem = docStructAddTypeSelectionSelectedItem;
    }

    /**
     * Return list of possible positions where to add new doc struct items.
     *
     * @return list of possible position where to add new doc struct items
     */
    public List<SelectItem> getDocStructPositionSelectionItems() {
        return docStructPositionSelectionItems;
    }

    /**
     * Return selected position where to add new doc struct item.
     *
     * @return selected position where to add new doc struct item.
     */
    public InsertionPosition getDocStructPositionSelectionSelectedItem() {
        return docStructPositionSelectionSelectedItem;
    }

    /**
     * Set selected position where to add new doc struct item.
     *
     * @param docStructPositionSelectionSelectedItem
     *          selected position where to add new doc struct item
     */
    public void setDocStructPositionSelectionSelectedItem(InsertionPosition docStructPositionSelectionSelectedItem) {
        this.docStructPositionSelectionSelectedItem = docStructPositionSelectionSelectedItem;
    }

    /**
     * Return number of elements to add.
     *
     * @return number of elements to add
     */
    public int getElementsToAddSpinnerValue() {
        return elementsToAddSpinnerValue;
    }

    /**
     * Set number of elements to add.
     *
     * @param elementsToAddSpinnerValue
     *          number of elements to add
     */
    public void setElementsToAddSpinnerValue(int elementsToAddSpinnerValue) {
        this.elementsToAddSpinnerValue = elementsToAddSpinnerValue;
    }

    /**
     * Return value of metadata input field.
     *
     * @return value of metadata input field
     */
    public String getInputMetaDataValue() {
        return inputMetaDataValue;
    }

    /**
     * Set value of metadata input field.
     *
     * @param inputMetaDataValue
     *          value of metadata input field
     */
    public void setInputMetaDataValue(String inputMetaDataValue) {
        this.inputMetaDataValue = inputMetaDataValue;
    }

    /**
     * Return list of addable metadata types.
     *
     * @return list of addable metadata types
     */
    public List<SelectItem> getSelectAddableMetadataTypesItems() {
        return selectAddableMetadataTypesItems;
    }

    /**
     * Return selected addable metadata type.
     *
     * @return selected addable metadata type
     */
    public String getSelectAddableMetadataTypesSelectedItem() {
        return selectAddableMetadataTypesSelectedItem;
    }

    /**
     * Set selected addable metadata type.
     *
     * @param selectAddableMetadataTypesSelectedItem
     *          selected addable metadata type.
     */
    public void setSelectAddableMetadataTypesSelectedItem(String selectAddableMetadataTypesSelectedItem) {
        this.selectAddableMetadataTypesSelectedItem = selectAddableMetadataTypesSelectedItem;
    }

    /**
     * Return first selected page.
     *
     * @return first selected page
     */
    public String getSelectFirstPageOnAddNodeSelectedItem() {
        return selectFirstPageOnAddNodeSelectedItem;
    }

    /**
     * Set first selected page.
     *
     * @param selectFirstPageOnAddNodeSelectedItem
     *          first selected page
     */
    public void setSelectFirstPageOnAddNodeSelectedItem(String selectFirstPageOnAddNodeSelectedItem) {
        this.selectFirstPageOnAddNodeSelectedItem = selectFirstPageOnAddNodeSelectedItem;
    }

    /**
     * Return last selected page.
     *
     * @return last selected page
     */
    public String getSelectLastPageOnAddNodeSelectedItem() {
        return selectLastPageOnAddNodeSelectedItem;
    }

    /**
     * Set last selected page.
     * @param selectLastPageOnAddNodeSelectedItem
     *          last selected page
     */
    public void setSelectLastPageOnAddNodeSelectedItem(String selectLastPageOnAddNodeSelectedItem) {
        this.selectLastPageOnAddNodeSelectedItem = selectLastPageOnAddNodeSelectedItem;
    }

    /**
     * Return list of selectable pages.
     *
     * @return list of selectable pages
     */
    public List<SelectItem> getSelectPageOnAddNodeItems() {
        return selectPageOnAddNodeItems;
    }

    /**
     * Return flag indicating whether function to add multiple logical elements at once is visible or not.
     *
     * @return flag indicating whether function to add multiple logical elements at once is visible or not
     */
    public boolean isShowingAddMultipleLogicalElements() {
        return showingAddMultipleLogicalElements;
    }

    /**
     * Set flag indicating whether function to add multiple logical elements at once is visible or not.
     *
     * @param showingAddMultipleLogicalElements
     *          flag indicating whether function to add multiple logical elements at once is visible or not
     */
    public void setShowingAddMultipleLogicalElements(boolean showingAddMultipleLogicalElements) {
        this.showingAddMultipleLogicalElements = showingAddMultipleLogicalElements;
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

    void prepare() {
        if (dataEditor.getSelectedStructure().isPresent()) {
            this.parents = MetadataEditor.getAncestorsOfStructure(dataEditor.getSelectedStructure().get(),
                dataEditor.getWorkpiece().getRootElement());

            prepareDocStructPositionSelectionItems(parents.size() == 0);
            prepareDocStructAddTypeSelectionItemsForChildren();
            prepareDocStructAddTypeSelectionItemsForParent();
            prepareDocStructAddTypeSelectionItemsForSiblings();
            prepareSelectAddableMetadataTypesItems();
        } else {
            docStructAddTypeSelectionItemsForChildren = Collections.emptyList();
            docStructAddTypeSelectionItemsForParent = Collections.emptyList();
            docStructAddTypeSelectionItemsForSiblings = Collections.emptyList();
            docStructPositionSelectionItems = Collections.emptyList();
            selectAddableMetadataTypesItems = Collections.emptyList();
        }
        prepareSelectPageOnAddNodeItems();
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
                    docStructAddTypeSelectionItemsForChildren.add(new SelectItem(newParent, entry.getValue()));
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
                docStructAddTypeSelectionItemsForChildren.add(new SelectItem(entry.getKey(), entry.getValue()));
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
        for (MetadataViewInterface keyView : dataEditor.getRuleset()
                .getStructuralElementView(
                    dataEditor.getSelectedStructure().orElseThrow(IllegalStateException::new).getType(),
                    dataEditor.getAcquisitionStage(), dataEditor.getPriorityList())
                .getAddableMetadata(Collections.emptyMap(), Collections.emptyList())) {
            selectAddableMetadataTypesItems.add(new SelectItem(keyView.getId(), keyView.getLabel()));
        }
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
}
