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

import static org.kitodo.production.forms.dataeditor.InsertionPosition.LAST_CHILD_OF_CURRENT_ELEMENT;

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
import org.kitodo.api.dataformat.MediaUnit;
import org.kitodo.api.dataformat.Structure;
import org.kitodo.api.dataformat.View;
import org.kitodo.api.dataformat.Workpiece;
import org.kitodo.production.helper.Helper;

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
    private String inputMetaDataValueValue = "";
    private LinkedList<Structure> parents;
    private List<SelectItem> selectAddableMetadataTypesItems;
    private String selectAddableMetadataTypesSelectedItem = "";
    private String selectFirstPageOnAddNodeSelectedItem;
    private String selectLastPageOnAddNodeSelectedItem;
    private List<SelectItem> selectPageOnAddNodeItems;
    private boolean showingAddMultipleLogicalElements;
    private Structure structure;
    private Workpiece workpiece;

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
        MetadataEditor.addMultipleStructures(elementsToAddSpinnerValue, docStructAddTypeSelectionSelectedItem,
            workpiece, structure, docStructPositionSelectionSelectedItem, selectAddableMetadataTypesSelectedItem,
            inputMetaDataValueValue);
        dataEditor.refreshStructurePanel();
    }

    /**
     * This method is invoked if the user clicks on the add single doc struc
     * submit btn command button.
     */
    public void addSingleDocStrucSubmitBtnClick() {
        MetadataEditor.addStructure(docStructAddTypeSelectionSelectedItem, workpiece,
            structure, docStructPositionSelectionSelectedItem, getViewsToAdd());
        dataEditor.refreshStructurePanel();
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

    public String getDocStructAddTypeSelectionSelectedItem() {
        return docStructAddTypeSelectionSelectedItem;
    }

    public List<SelectItem> getDocStructPositionSelectionItems() {
        return docStructPositionSelectionItems;
    }

    public InsertionPosition getDocStructPositionSelectionSelectedItem() {
        return docStructPositionSelectionSelectedItem;
    }

    public int getElementsToAddSpinnerValue() {
        return elementsToAddSpinnerValue;
    }

    public String getInputMetaDataValueValue() {
        return inputMetaDataValueValue;
    }

    public List<SelectItem> getSelectAddableMetadataTypesItems() {
        return selectAddableMetadataTypesItems;
    }

    public String getSelectAddableMetadataTypesSelectedItem() {
        return selectAddableMetadataTypesSelectedItem;
    }

    public String getSelectFirstPageOnAddNodeSelectedItem() {
        return selectFirstPageOnAddNodeSelectedItem;
    }

    public String getSelectLastPageOnAddNodeSelectedItem() {
        return selectLastPageOnAddNodeSelectedItem;
    }

    public List<SelectItem> getSelectPageOnAddNodeItems() {
        return selectPageOnAddNodeItems;
    }

    private List<View> getViewsToAdd() {
        try {
            int firstPage = Integer.parseInt(selectFirstPageOnAddNodeSelectedItem);
            int lastPage = Integer.parseInt(selectLastPageOnAddNodeSelectedItem);
            return EditPagesDialog.getViewsToAdd(workpiece, firstPage, lastPage);
        } catch (NumberFormatException e) {
            // user didn’t select both start and end page
            logger.catching(Level.TRACE, e);
            return Collections.emptyList();
        }
    }

    public boolean isShowingAddMultipleLogicalElements() {
        return showingAddMultipleLogicalElements;
    }

    void prepare(Workpiece workpiece, Structure selectedStructure) {
        this.structure = selectedStructure;
        this.workpiece = workpiece;
        this.parents = MetadataEditor.getParentsOfStructure(selectedStructure, workpiece.getStructure(), null);

        prepareDocStructPositionSelectionItems(parents.size() == 0);
        prepareDocStructAddTypeSelectionItemsForChildren();
        prepareDocStructAddTypeSelectionItemsForParent();
        prepareDocStructAddTypeSelectionItemsForSiblings();
        prepareSelectPageOnAddNodeItems();
        prepareSelectAddableMetadataTypesItems();
    }

    private void prepareDocStructAddTypeSelectionItemsForChildren() {
        docStructAddTypeSelectionItemsForChildren = new ArrayList<>();
        StructuralElementViewInterface divisionView = dataEditor.getRuleset().getStructuralElementView(
            structure.getType(), dataEditor.getAcquisitionStage(), dataEditor.getPriorityList());
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
                if (newParentDivisionView.getAllowedSubstructuralElements().containsKey(structure.getType())) {
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
            docStructPositionSelectionItems.add(new SelectItem(InsertionPosition.BEFOR_CURRENT_ELEMENT,
                    Helper.getTranslation("vorDasAktuelleElement")));
            docStructPositionSelectionItems.add(new SelectItem(InsertionPosition.AFTER_CURRENT_ELEMENT,
                    Helper.getTranslation("hinterDasAktuelleElement")));
        }
        docStructPositionSelectionItems.add(new SelectItem(InsertionPosition.FIRST_CHILD_OF_CURRENT_ELEMENT,
                Helper.getTranslation("alsErstesKindDesAktuellenElements")));
        docStructPositionSelectionItems.add(new SelectItem(InsertionPosition.LAST_CHILD_OF_CURRENT_ELEMENT,
                Helper.getTranslation("alsLetztesKindDesAktuellenElements")));
        docStructPositionSelectionItems.add(new SelectItem(InsertionPosition.PARENT_OF_CURRENT_ELEMENT,
                Helper.getTranslation("alsElternteilDesAktuellenElements")));
    }

    private void prepareSelectAddableMetadataTypesItems() {
        selectAddableMetadataTypesItems = new ArrayList<>();
        for (MetadataViewInterface keyView : dataEditor.getRuleset().getStructuralElementView(structure.getType(),
            dataEditor.getAcquisitionStage(), dataEditor.getPriorityList()).getAddableMetadata(Collections.emptyMap(),
                Collections.emptyList())) {
            selectAddableMetadataTypesItems.add(new SelectItem(keyView.getId(), keyView.getLabel()));
        }
    }

    private void prepareSelectPageOnAddNodeItems() {
        List<MediaUnit> mediaUnits = workpiece.getMediaUnits();
        selectPageOnAddNodeItems = new ArrayList<>(mediaUnits.size());
        for (int i = 0; i < mediaUnits.size(); i++) {
            MediaUnit mediaUnit = mediaUnits.get(i);
            selectPageOnAddNodeItems.add(
                new SelectItem(Integer.toString(i), Objects.isNull(mediaUnit.getOrderlabel()) ? Integer.toString(i)
                        : i + " : " + mediaUnit.getOrderlabel()));
        }
    }

    public void setDocStructAddTypeSelectionSelectedItem(String docStructAddTypeSelectionSelectedItem) {
        this.docStructAddTypeSelectionSelectedItem = docStructAddTypeSelectionSelectedItem;
    }

    public void setDocStructPositionSelectionSelectedItem(
            InsertionPosition docStructPositionSelectionSelectedItem) {
        this.docStructPositionSelectionSelectedItem = docStructPositionSelectionSelectedItem;
    }

    public void setElementsToAddSpinnerValue(int elementsToAddSpinnerValue) {
        this.elementsToAddSpinnerValue = elementsToAddSpinnerValue;
    }

    public void setInputMetaDataValueValue(String inputMetaDataValueValue) {
        this.inputMetaDataValueValue = inputMetaDataValueValue;
    }

    public void setSelectAddableMetadataTypesSelectedItem(String selectAddableMetadataTypesSelectedItem) {
        this.selectAddableMetadataTypesSelectedItem = selectAddableMetadataTypesSelectedItem;
    }

    public void setSelectFirstPageOnAddNodeSelectedItem(String selectFirstPageOnAddNodeSelectedItem) {
        this.selectFirstPageOnAddNodeSelectedItem = selectFirstPageOnAddNodeSelectedItem;
    }

    public void setSelectLastPageOnAddNodeSelectedItem(String selectLastPageOnAddNodeSelectedItem) {
        this.selectLastPageOnAddNodeSelectedItem = selectLastPageOnAddNodeSelectedItem;
    }

    public void setShowingAddMultipleLogicalElements(boolean showingAddMultipleLogicalElements) {
        this.showingAddMultipleLogicalElements = showingAddMultipleLogicalElements;
    }
}
