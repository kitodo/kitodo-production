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
import java.util.List;
import java.util.Objects;

import javax.faces.model.SelectItem;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.kitodo.api.dataformat.IncludedStructuralElement;
import org.kitodo.api.dataformat.MediaUnit;
import org.kitodo.api.dataformat.View;
import org.kitodo.config.ConfigCore;
import org.kitodo.config.enums.ParameterCore;
import org.kitodo.exceptions.InvalidImagesException;
import org.kitodo.production.helper.Helper;
import org.kitodo.production.helper.metadata.pagination.Paginator;
import org.kitodo.production.helper.metadata.pagination.PaginatorMode;
import org.kitodo.production.helper.metadata.pagination.PaginatorType;
import org.kitodo.production.helper.metadata.pagination.RomanNumeral;
import org.kitodo.production.services.ServiceManager;
import org.primefaces.PrimeFaces;

/**
 * Backing bean for the pagination panel.
 */
public class PaginationPanel {

    private final DataEditorForm dataEditor;
    private boolean fictitiousCheckboxChecked = false;
    private int newPagesCountValue = 0;
    private List<SelectItem> paginationSelectionItems;
    private List<Integer> paginationSelectionSelectedItems = new ArrayList<>();
    private String paginationStartValue = "1";
    private List<SelectItem> paginationTypeSelectItems;
    private PaginatorType paginationTypeSelectSelectedItem = PaginatorType.ARABIC;
    private List<IllustratedSelectItem> selectPaginationModeItems;
    private IllustratedSelectItem selectPaginationModeSelectedItem;
    private List<SelectItem> selectPaginationScopeItems;
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

    /**
     * This method is invoked if the create pagination button is clicked.
     */
    public void createPagination() {
        try {
            ServiceManager.getFileService().searchForMedia(dataEditor.getProcess(), dataEditor.getWorkpiece());
        } catch (InvalidImagesException e) {
            Helper.setErrorMessage(e.getLocalizedMessage());
        }
        Paginator paginator = new Paginator(metsEditorDefaultPagination(1));
        List<MediaUnit> mediaUnits = dataEditor.getWorkpiece().getAllMediaUnitChildrenFilteredByTypePageAndSorted();
        for (int i = 1; i < mediaUnits.size(); i++) {
            MediaUnit mediaUnit = mediaUnits.get(i - 1);
            mediaUnit.setOrder(i);
            mediaUnit.setOrderlabel(paginator.next());
        }
        dataEditor.refreshStructurePanel();
        dataEditor.getGalleryPanel().show();
        show();
    }

    /**
     * This method is invoked if the generate dummy images button is clicked.
     */
    public void generateDummyImagesButtonClick() {
        List<MediaUnit> mediaUnits = dataEditor.getWorkpiece().getAllMediaUnitChildrenFilteredByTypePageAndSorted();
        int order = mediaUnits.isEmpty() ? 1 : mediaUnits.get(mediaUnits.size() - 1).getOrder() + 1;
        boolean withAutomaticPagination = ConfigCore.getBooleanParameter(ParameterCore.WITH_AUTOMATIC_PAGINATION);
        Paginator orderlabel = new Paginator(metsEditorDefaultPagination(order));
        for (int i = 1; i <= newPagesCountValue; i++) {
            MediaUnit mediaUnit = new MediaUnit();
            mediaUnit.setOrder(order++);
            if (withAutomaticPagination) {
                mediaUnit.setOrderlabel(orderlabel.next());
            }
            mediaUnits.add(mediaUnit);
        }
    }

    private static String metsEditorDefaultPagination(int first) {
        switch (ConfigCore.getParameter(ParameterCore.METS_EDITOR_DEFAULT_PAGINATION)) {
            case "arabic":
                return Integer.toString(first);
            case "roman":
                return RomanNumeral.format(first, true);
            case "uncounted":
                return " - ";
            default:
                return "";
        }
    }

    /**
     * Returns the value of the newPagesCount input box.
     *
     * @return the value of the newPagesCount
     */
    public int getNewPagesCountValue() {
        return newPagesCountValue;
    }

    /**
     * Sets the value of the newPagesCount input box.
     *
     * @param newPagesCountValue
     *            value to set
     */
    public void setNewPagesCountValue(int newPagesCountValue) {
        this.newPagesCountValue = newPagesCountValue;
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
     * @param paginationSelectionSelectedItems
     *            selected items to set
     */
    public void setPaginationSelectionSelectedItems(List<Integer> paginationSelectionSelectedItems) {
        List<MediaUnit> mediaUnits = dataEditor.getWorkpiece().getAllMediaUnitChildrenFilteredByTypePageAndSorted();
        int lastItemIndex = paginationSelectionSelectedItems.get(paginationSelectionSelectedItems.size() - 1);
        if (this.paginationSelectionSelectedItems.isEmpty()
                || !Objects.equals(this.paginationSelectionSelectedItems.get(
                        this.paginationSelectionSelectedItems.size() - 1 ), lastItemIndex)) {
            dataEditor.getStructurePanel().updateNodeSelection(
                    dataEditor.getGalleryPanel().getGalleryMediaContent(mediaUnits.get(lastItemIndex)),
                    mediaUnits.get(lastItemIndex).getIncludedStructuralElements().get(0));
            updateMetadataPanel();
        }
        dataEditor.getSelectedMedia().clear();
        for (int i : paginationSelectionSelectedItems) {
            for (IncludedStructuralElement includedStructuralElement : mediaUnits.get(i).getIncludedStructuralElements()) {
                dataEditor.getSelectedMedia().add(new ImmutablePair<>(mediaUnits.get(i), includedStructuralElement));
            }
        }
        this.paginationSelectionSelectedItems = paginationSelectionSelectedItems;
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
    public List<SelectItem> getPaginationTypeSelectItems() {
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
    public List<SelectItem> getSelectPaginationScopeItems() {
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
        List<MediaUnit> mediaUnits = dataEditor.getWorkpiece().getAllMediaUnitChildrenFilteredByTypePageAndSorted();
        paginationSelectionItems = new ArrayList<>(mediaUnits.size());
        for (int i = 0; i < mediaUnits.size(); i++) {
            MediaUnit mediaUnit = mediaUnits.get(i);
            String label = Objects.isNull(mediaUnit.getOrderlabel()) ? Integer.toString(mediaUnit.getOrder())
                    : mediaUnit.getOrder() + " : " + mediaUnit.getOrderlabel();
            paginationSelectionItems.add(new SelectItem(i, label));
        }
    }

    /**
     * prepare selected items to pagination.
     */
    public void preparePaginationSelectionSelectedItems() {
        paginationSelectionSelectedItems = new ArrayList<>();
        List<MediaUnit> mediaUnits = dataEditor.getWorkpiece().getAllMediaUnitChildrenFilteredByTypePageAndSorted();
        for (Pair<MediaUnit, IncludedStructuralElement> selectedElement : dataEditor.getSelectedMedia()) {
            for (int i = 0; i < mediaUnits.size(); i++) {
                MediaUnit mediaUnit = mediaUnits.get(i);
                if (mediaUnit.equals(selectedElement.getKey())) {
                    paginationSelectionSelectedItems.add(i);
                    break;
                }
            }
        }
    }

    private void preparePaginationTypeSelectItems() {
        paginationTypeSelectItems = new ArrayList<>(5);
        paginationTypeSelectItems.add(new SelectItem(PaginatorType.ARABIC, Helper.getTranslation("arabic")));
        paginationTypeSelectItems.add(new SelectItem(PaginatorType.ROMAN, Helper.getTranslation("roman")));
        paginationTypeSelectItems.add(new SelectItem(PaginatorType.UNCOUNTED, Helper.getTranslation("uncounted")));
        paginationTypeSelectItems
                .add(new SelectItem(PaginatorType.FREETEXT, Helper.getTranslation("paginationFreetext")));
        paginationTypeSelectItems
                .add(new SelectItem(PaginatorType.ADVANCED, Helper.getTranslation("paginationAdvanced")));
    }

    private void prepareSelectPaginationModeItems() {
        selectPaginationModeItems = new ArrayList<>(6);
        selectPaginationModeItems.add(new IllustratedSelectItem(PaginatorMode.PAGES, Helper.getTranslation("pageCount"),
                "paginierung_seite.svg"));
        selectPaginationModeItems.add(new IllustratedSelectItem(PaginatorMode.DOUBLE_PAGES,
                Helper.getTranslation("columnCount"), "paginierung_spalte.svg"));
        selectPaginationModeItems.add(new IllustratedSelectItem(PaginatorMode.FOLIATION,
                Helper.getTranslation("blattzaehlung"), "paginierung_blatt.svg"));
        selectPaginationModeItems.add(new IllustratedSelectItem(PaginatorMode.RECTOVERSO_FOLIATION,
                Helper.getTranslation("blattzaehlungrectoverso"), "paginierung_blatt_rectoverso.svg"));
        selectPaginationModeItems.add(new IllustratedSelectItem(PaginatorMode.RECTOVERSO,
                Helper.getTranslation("pageCountRectoVerso"), "paginierung_seite_rectoverso.svg"));
        selectPaginationModeItems.add(new IllustratedSelectItem(PaginatorMode.DOUBLE_PAGES,
                Helper.getTranslation("pageCountDouble"), "paginierung_doppelseite.svg"));
    }

    private void prepareSelectPaginationScopeItems() {
        selectPaginationScopeItems = new ArrayList<>(2);
        selectPaginationScopeItems
                .add(new SelectItem(Boolean.TRUE, Helper.getTranslation("abDerErstenMarkiertenSeite")));
        selectPaginationScopeItems.add(new SelectItem(Boolean.FALSE, Helper.getTranslation("nurDieMarkiertenSeiten")));
    }

    /**
     * This method is invoked if the start pagination action button is clicked.
     */
    public void startPaginationClick() {
        if (paginationSelectionSelectedItems.isEmpty()) {
            Helper.setErrorMessage("fehlerBeimEinlesen", "No pages selected for pagination.");
            return;
        }
        dataEditor.getMetadataPanel().preserve();
        List<Separator> pageSeparators = Separator.factory(ConfigCore.getParameter(ParameterCore.PAGE_SEPARATORS));
        String initializer = paginationTypeSelectSelectedItem.format(selectPaginationModeSelectedItem.getValue(),
                paginationStartValue, fictitiousCheckboxChecked, pageSeparators.get(0).getSeparatorString());
        Paginator paginator = new Paginator(initializer);
        List<MediaUnit> mediaUnits = dataEditor.getWorkpiece().getAllMediaUnitChildrenFilteredByTypePageAndSorted();
        if (selectPaginationScopeSelectedItem) {
            for (int i = paginationSelectionSelectedItems.get(0); i < mediaUnits.size(); i++) {
                mediaUnits.get(i).setOrderlabel(paginator.next());
            }
        } else {
            for (int i : paginationSelectionSelectedItems) {
                mediaUnits.get(i).setOrderlabel(paginator.next());
            }
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
            dataEditor.getMetadataPanel().showPageInLogical(view.getMediaUnit());
        }
    }

    /**
     * Show.
     */
    public void show() {
        paginationSelectionSelectedItems = new ArrayList<>();
        paginationTypeSelectSelectedItem = PaginatorType.ARABIC;
        selectPaginationModeSelectedItem = null;
        paginationStartValue = "1";
        fictitiousCheckboxChecked = false;
        selectPaginationScopeSelectedItem = Boolean.TRUE;
        preparePaginationSelectionItems();
        preparePaginationSelectionSelectedItems();
    }
}
