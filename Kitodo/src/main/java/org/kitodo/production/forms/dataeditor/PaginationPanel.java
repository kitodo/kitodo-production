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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.faces.model.SelectItem;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kitodo.api.dataeditor.rulesetmanagement.FunctionalMetadata;
import org.kitodo.api.dataeditor.rulesetmanagement.RulesetManagementInterface;
import org.kitodo.api.dataformat.LogicalDivision;
import org.kitodo.api.dataformat.PhysicalDivision;
import org.kitodo.api.dataformat.View;
import org.kitodo.config.ConfigCore;
import org.kitodo.config.enums.ParameterCore;
import org.kitodo.exceptions.InvalidImagesException;
import org.kitodo.exceptions.InvalidMetadataValueException;
import org.kitodo.exceptions.MediaNotFoundException;
import org.kitodo.exceptions.NoSuchMetadataFieldException;
import org.kitodo.production.helper.Helper;
import org.kitodo.production.helper.metadata.pagination.Paginator;
import org.kitodo.production.helper.metadata.pagination.PaginatorMode;
import org.kitodo.production.helper.metadata.pagination.PaginatorType;
import org.kitodo.production.services.ServiceManager;
import org.primefaces.PrimeFaces;

/**
 * Backing bean for the pagination panel.
 */
public class PaginationPanel {
    private static final Logger logger = LogManager.getLogger(PaginationPanel.class);

    private final DataEditorForm dataEditor;
    private boolean fictitiousCheckboxChecked = false;
    private List<SelectItem> paginationSelectionItems;
    private List<SelectItem> pageLabelItems;
    private String selectedPageLabelItem;
    private List<Integer> paginationSelectionSelectedItems = new ArrayList<>();
    private String paginationStartValue = "1";
    private Map<PaginatorType, String> paginationTypeSelectItems;
    private PaginatorType paginationTypeSelectSelectedItem = PaginatorType.ARABIC;
    private List<IllustratedSelectItem> selectPaginationModeItems;
    private IllustratedSelectItem selectPaginationModeSelectedItem;
    private Map<Boolean, String> selectPaginationScopeItems;
    private Boolean selectPaginationScopeSelectedItem = Boolean.TRUE;

    /**
     * Constructor.
     *
     * @param dataEditor DataEditorForm instance
     */
    PaginationPanel(DataEditorForm dataEditor) {
        this.dataEditor = dataEditor;
        preparePaginationTypeSelectItems();
        prepareSelectPaginationModeItems();
        prepareSelectPaginationScopeItems();
    }
    
    public String getSelectedPageLabelItem() {
        return this.selectedPageLabelItem;
    }

    public void setSelectedPageLabelItem(String selectedPageLabelItem) {
        this.selectedPageLabelItem = selectedPageLabelItem;
    }

    public List<SelectItem> getPageLabelItems() {
        return this.pageLabelItems;
    }

    public void setPageLabelItems(List<SelectItem> pageLabelItems) {
        this.pageLabelItems = pageLabelItems;
    }

    /**
     * Checks and updates media references in workpiece depending on changes in file system.
     */
    public void updateMediaReferences() {
        boolean mediaReferencesChanged = false;
        try {
            mediaReferencesChanged = ServiceManager.getFileService().searchForMedia(dataEditor.getProcess(),
                    dataEditor.getWorkpiece());
        } catch (InvalidImagesException e) {
            Helper.setErrorMessage(e.getLocalizedMessage());
        } catch (MediaNotFoundException e) {
            Helper.setWarnMessage(e.getMessage());
        }
        dataEditor.setMediaUpdated(mediaReferencesChanged);
        List<PhysicalDivision> physicalDivisions = dataEditor.getWorkpiece().getAllPhysicalDivisionChildrenSortedFilteredByPageAndTrack();
        for (int i = 1; i < physicalDivisions.size(); i++) {
            PhysicalDivision physicalDivision = physicalDivisions.get(i - 1);
            physicalDivision.setOrder(i);
        }
        dataEditor.refreshStructurePanel();
        dataEditor.getGalleryPanel().show();
        show();
        PrimeFaces.current().ajax().update("fileReferencesUpdatedDialog");
        PrimeFaces.current().executeScript("PF('fileReferencesUpdatedDialog').show();");
    }

    /**
     * Returns the selected items of the paginationSelection select menu.
     *
     * @return the selected items of the paginationSelection
     */
    public List<Integer> getPaginationSelectionSelectedItems() {
        return paginationSelectionSelectedItems;
    }

    /**
     * Sets the selected items of the paginationSelection select menu.
     *
     * @param selectedItems
     *            selected items to set
     */
    public void setPaginationSelectionSelectedItems(List<Integer> selectedItems) {
        List<PhysicalDivision> physicalDivisions = dataEditor.getWorkpiece().getAllPhysicalDivisionChildrenSortedFilteredByPageAndTrack();
        if (!selectedItems.isEmpty()) {
            int lastItemIndex = selectedItems.get(selectedItems.size() - 1);
            if (this.paginationSelectionSelectedItems.isEmpty()
                    || !Objects.equals(this.paginationSelectionSelectedItems.get(
                    this.paginationSelectionSelectedItems.size() - 1), lastItemIndex)) {
                dataEditor.getStructurePanel().updateNodeSelection(
                        dataEditor.getGalleryPanel().getGalleryMediaContent(physicalDivisions.get(lastItemIndex)),
                        physicalDivisions.get(lastItemIndex).getLogicalDivisions().get(0));
                updateMetadataPanel();
            }
        }
        dataEditor.getSelectedMedia().clear();
        for (int i : selectedItems) {
            for (LogicalDivision logicalDivision : physicalDivisions.get(i).getLogicalDivisions()) {
                dataEditor.getSelectedMedia().add(new ImmutablePair<>(physicalDivisions.get(i), logicalDivision));
            }
        }
        this.paginationSelectionSelectedItems = selectedItems;
    }

    /**
     * Returns the value of the paginationStart input box.
     *
     * @return the value of the paginationStart
     */
    public String getPaginationStartValue() {
        return paginationStartValue;
    }

    /**
     * Sets the value of the paginationStart input box.
     *
     * @param paginationStartValue
     *            value to set
     */
    public void setPaginationStartValue(String paginationStartValue) {
        this.paginationStartValue = paginationStartValue;
    }

    /**
     * Returns the selected item of the paginationTypeSelect drop-down menu.
     *
     * @return the selected item of the paginationTypeSelect
     */
    public PaginatorType getPaginationTypeSelectSelectedItem() {
        return paginationTypeSelectSelectedItem;
    }

    /**
     * Sets the selected item of the paginationTypeSelect drop-down menu.
     *
     * @param paginationTypeSelectSelectedItem
     *            selected item to set
     */
    public void setPaginationTypeSelectSelectedItem(PaginatorType paginationTypeSelectSelectedItem) {
        this.paginationTypeSelectSelectedItem = paginationTypeSelectSelectedItem;
    }

    /**
     * Returns the selected item of the selectPaginationMode drop-down menu.
     *
     * @return the selected item of the selectPaginationMode
     */
    public IllustratedSelectItem getSelectPaginationModeSelectedItem() {
        return selectPaginationModeSelectedItem;
    }

    /**
     * Sets the selected item of the paginationModeSelect drop-down menu.
     *
     * @param selectPaginationModeSelectedItem
     *            selected item to set
     */
    public void setSelectPaginationModeSelectedItem(IllustratedSelectItem selectPaginationModeSelectedItem) {
        this.selectPaginationModeSelectedItem = selectPaginationModeSelectedItem;
    }

    /**
     * Returns the selected item of the selectPaginationScope drop-down menu.
     *
     * @return the selected item of the selectPaginationScope
     */
    public Boolean getSelectPaginationScopeSelectedItem() {
        return selectPaginationScopeSelectedItem;
    }

    /**
     * Sets the selected item of the paginationScopeSelect drop-down menu.
     *
     * @param selectPaginationScopeSelectedItem
     *            selected item to set
     */
    public void setSelectPaginationScopeSelectedItem(Boolean selectPaginationScopeSelectedItem) {
        this.selectPaginationScopeSelectedItem = selectPaginationScopeSelectedItem;
    }

    /**
     * Returns the items for the paginationSelection select menu.
     *
     * @return the items for the paginationSelection
     */
    public List<SelectItem> getPaginationSelectionItems() {
        return paginationSelectionItems;
    }


    /**
     * Returns the items for the paginationTypeSelect select menu.
     *
     * @return the items for the paginationTypeSelect
     */
    public Map<PaginatorType, String> getPaginationTypeSelectItems() {
        return paginationTypeSelectItems;
    }

    /**
     * Returns the items for the paginationMode select menu.
     *
     * @return the items for the paginationMode
     */
    public List<IllustratedSelectItem> getSelectPaginationModeItems() {
        return selectPaginationModeItems;
    }

    /**
     * Returns the items for the paginationScope select menu.
     *
     * @return the items for the paginationScope
     */
    public Map<Boolean, String> getSelectPaginationScopeItems() {
        return selectPaginationScopeItems;
    }

    /**
     * Returns whether the fictitiousCheckbox is checked.
     *
     * @return whether the fictitiousCheckbox is checked
     */
    public boolean isFictitiousCheckboxChecked() {
        return fictitiousCheckboxChecked;
    }

    /**
     * Sets whether the fictitiousCheckbox is checked.
     *
     * @param fictitiousCheckboxChecked
     *            whether the checkbox is checked
     */
    public void setFictitiousCheckboxChecked(boolean fictitiousCheckboxChecked) {
        this.fictitiousCheckboxChecked = fictitiousCheckboxChecked;
    }

    private void preparePaginationSelectionItems() {
        List<PhysicalDivision> physicalDivisions = dataEditor.getWorkpiece().getAllPhysicalDivisionChildrenSortedFilteredByPageAndTrack();
        paginationSelectionItems = new ArrayList<>(physicalDivisions.size());
        for (int i = 0; i < physicalDivisions.size(); i++) {
            View view = View.of(physicalDivisions.get(i));
            String label = dataEditor.getStructurePanel().buildViewLabel(view);
            paginationSelectionItems.add(new SelectItem(i, label));
        }
    }

    /**
     * prepare selected items to pagination.
     */
    public void preparePaginationSelectionSelectedItems() {
        paginationSelectionSelectedItems = new ArrayList<>();
        List<PhysicalDivision> physicalDivisions = dataEditor.getWorkpiece().getAllPhysicalDivisionChildrenSortedFilteredByPageAndTrack();
        for (Pair<PhysicalDivision, LogicalDivision> selectedElement : dataEditor.getSelectedMedia()) {
            for (int i = 0; i < physicalDivisions.size(); i++) {
                PhysicalDivision physicalDivision = physicalDivisions.get(i);
                if (physicalDivision.equals(selectedElement.getKey())) {
                    paginationSelectionSelectedItems.add(i);
                    break;
                }
            }
        }
    }

    private void preparePaginationTypeSelectItems() {
        paginationTypeSelectItems = new LinkedHashMap<>(5);
        paginationTypeSelectItems.put(PaginatorType.ARABIC, "arabic");
        paginationTypeSelectItems.put(PaginatorType.ROMAN, "roman");
        paginationTypeSelectItems.put(PaginatorType.UNCOUNTED, "uncounted");
        paginationTypeSelectItems.put(PaginatorType.FREETEXT, "paginationFreetext");
        paginationTypeSelectItems.put(PaginatorType.ADVANCED, "paginationAdvanced");
    }

    private void prepareSelectPaginationModeItems() {
        selectPaginationModeItems = new ArrayList<>(6);
        selectPaginationModeItems.add(new IllustratedSelectItem(PaginatorMode.PAGES, "pageCount",
                "paginierung_seite.svg"));
        selectPaginationModeItems.add(new IllustratedSelectItem(PaginatorMode.DOUBLE_PAGES, "columnCount",
                "paginierung_spalte.svg"));
        selectPaginationModeItems.add(new IllustratedSelectItem(PaginatorMode.FOLIATION, "sheetCounting",
                "paginierung_blatt.svg"));
        selectPaginationModeItems.add(new IllustratedSelectItem(PaginatorMode.RECTOVERSO_FOLIATION, "sheetCountingRectoVerso",
                "paginierung_blatt_rectoverso.svg"));
        selectPaginationModeItems.add(new IllustratedSelectItem(PaginatorMode.RECTOVERSO, "pageCountRectoVerso",
                "paginierung_seite_rectoverso.svg"));
        selectPaginationModeItems.add(new IllustratedSelectItem(PaginatorMode.DOUBLE_PAGES, "pageCountDouble",
                "paginierung_doppelseite.svg"));
    }

    private void prepareSelectPaginationScopeItems() {
        selectPaginationScopeItems = new HashMap<>(2);
        selectPaginationScopeItems.put(Boolean.TRUE, "fromFirstSelectedPage");
        selectPaginationScopeItems.put(Boolean.FALSE, "onlySelectedPages");
    }

    /**
     * This method is invoked if the start pagination action button is clicked.
     */
    public void startPaginationClick() {
        if (paginationSelectionSelectedItems.isEmpty()) {
            Helper.setErrorMessage("fehlerBeimEinlesen", "No pages selected for pagination.");
            return;
        }
        try {
            dataEditor.getMetadataPanel().preserve();
        } catch (InvalidMetadataValueException | NoSuchMetadataFieldException e) {
            logger.info(e.getMessage());
        }
        List<Separator> pageSeparators = Separator.factory(ConfigCore.getParameter(ParameterCore.PAGE_SEPARATORS));
        try {
            String initializer = paginationTypeSelectSelectedItem.format(selectPaginationModeSelectedItem.getValue(),
                paginationStartValue, fictitiousCheckboxChecked, pageSeparators.get(0).getSeparatorString());
            Paginator paginator = new Paginator(initializer);
            List<PhysicalDivision> physicalDivisions = dataEditor.getWorkpiece()
                    .getAllPhysicalDivisionChildrenSortedFilteredByPageAndTrack();
            if (selectPaginationScopeSelectedItem) {
                for (int i = paginationSelectionSelectedItems.get(0); i < physicalDivisions.size(); i++) {
                    physicalDivisions.get(i).setOrderlabel(paginator.next());
                }
            } else {
                for (int i : paginationSelectionSelectedItems) {
                    physicalDivisions.get(i).setOrderlabel(paginator.next());
                }
            }
        } catch (NumberFormatException e) {
            Helper.setErrorMessage("paginationFormatError", new Object[] { paginationStartValue });
        }
        paginationSelectionSelectedItems = new ArrayList<>();
        preparePaginationSelectionItems();
        dataEditor.refreshStructurePanel();
        updateMetadataPanel();
        PrimeFaces.current().executeScript("PF('notifications').renderMessage({'summary':'"
                + Helper.getTranslation("paginationSaved") + "','severity':'info'})");
    }

    private void updateMetadataPanel() {
        if (dataEditor.getSelectedStructure().isPresent()) {
            dataEditor.getMetadataPanel().showLogical(dataEditor.getSelectedStructure());
        } else if (Objects.nonNull(dataEditor.getStructurePanel().getSelectedLogicalNode())
                && dataEditor.getStructurePanel().getSelectedLogicalNode().getData() instanceof StructureTreeNode
                && Objects.nonNull(dataEditor.getStructurePanel().getSelectedLogicalNode().getData())
                && ((StructureTreeNode) dataEditor.getStructurePanel().getSelectedLogicalNode().getData())
                .getDataObject() instanceof View) {
            View view = (View) ((StructureTreeNode) dataEditor.getStructurePanel().getSelectedLogicalNode().getData()).getDataObject();
            dataEditor.getMetadataPanel().showPageInLogical(view.getPhysicalDivision());
        }
    }

    /**
     * Show.
     */
    public void show() {
        paginationSelectionSelectedItems = new ArrayList<>();
        paginationTypeSelectSelectedItem = PaginatorType.ARABIC;
        selectPaginationModeSelectedItem = selectPaginationModeItems.get(0);
        paginationStartValue = "1";
        fictitiousCheckboxChecked = false;
        selectPaginationScopeSelectedItem = Boolean.TRUE;
        preparePaginationSelectionItems();
        preparePaginationSelectionSelectedItems();
        preparePageLabelItems();
    }
    
    private void preparePageLabelItems() {
        RulesetManagementInterface rmi = dataEditor.getRulesetManagement();
        pageLabelItems = new ArrayList<>();
        Collection<String> pageLabels = rmi.getFunctionalKeys(FunctionalMetadata.PAGE_LABEL).stream()
                .map(key -> rmi.getTranslationForKey(key, dataEditor.getPriorityList())).filter(Optional::isPresent)
                .map(Optional::get).collect(Collectors.toList());
        for (String s : pageLabels) {
            pageLabelItems.add(new SelectItem(s, s));
        }
    }
}
