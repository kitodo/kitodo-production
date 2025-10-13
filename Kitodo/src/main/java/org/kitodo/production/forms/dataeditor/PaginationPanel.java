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
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.faces.model.SelectItem;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
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
    private String paginationStartValue = "1";
    private Map<PaginatorType, String> paginationTypeSelectItems;
    private PaginatorType paginationTypeSelectSelectedItem = PaginatorType.ARABIC;
    private List<IllustratedSelectItem> selectPaginationModeItems;
    private IllustratedSelectItem selectPaginationModeSelectedItem;
    private Map<Boolean, String> selectPaginationScopeItems;
    private Boolean selectPaginationScopeSelectedItem;

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

    private void prepareSelectPaginationScopeSelectedItem() {
        selectPaginationScopeSelectedItem = ServiceManager.getUserService().getCurrentUser()
                .isPaginateFromFirstPageByDefault();
    }

    /**
     * prepare selected items to pagination.
     */
    public List<Integer> prepareSelectedItemsForPagination() {
        List<Integer> selectedItemsPreparedForPagination = new ArrayList<>();
        List<PhysicalDivision> physicalDivisions = dataEditor.getWorkpiece().getAllPhysicalDivisionChildrenSortedFilteredByPageAndTrack();
        List<Pair<PhysicalDivision, LogicalDivision>> selectedMedia = dataEditor.getSelectedMedia();
        for (Pair<PhysicalDivision, LogicalDivision> selectedElement : selectedMedia) {
            for (int i = 0; i < physicalDivisions.size(); i++) {
                PhysicalDivision physicalDivision = physicalDivisions.get(i);
                if (physicalDivision.equals(selectedElement.getKey())) {
                    selectedItemsPreparedForPagination.add(i);
                }
            }
        }
        return selectedItemsPreparedForPagination;
    }

    private static PaginatorType resolveDefaultPaginationType() {
        String defaultType = ServiceManager.getUserService().getCurrentUser().getDefaultPaginationType();
        if (defaultType != null && !defaultType.isBlank()) {
            try {
                return PaginatorType.valueOf(defaultType);
            } catch (IllegalArgumentException ex) {
                // Fallback if the saved value is not valid
                return PaginatorType.ARABIC;
            }
        }
        return PaginatorType.ARABIC;
    }

    private void preparePaginationTypeSelectItems() {
        paginationTypeSelectItems = new LinkedHashMap<>(5);
        paginationTypeSelectItems.put(PaginatorType.ARABIC, "arabic");
        paginationTypeSelectItems.put(PaginatorType.ROMAN, "roman");
        paginationTypeSelectItems.put(PaginatorType.UNCOUNTED, "uncounted");
        paginationTypeSelectItems.put(PaginatorType.FREETEXT, "paginationFreetext");
        paginationTypeSelectItems.put(PaginatorType.ALPHABETIC, "alphabetic");
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
        selectPaginationModeItems.add(new IllustratedSelectItem(PaginatorMode.VERSORECTO_FOLIATION, "sheetCountingVersoRecto",
                "paginierung_blatt_versorecto.svg"));
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
        List<Integer> itemsForPagination = prepareSelectedItemsForPagination();
        if (itemsForPagination.isEmpty()) {
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
                for (int i = itemsForPagination.get(0); i < physicalDivisions.size(); i++) {
                    physicalDivisions.get(i).setOrderlabel(paginator.next());
                }
            } else {
                for (int i : itemsForPagination) {
                    physicalDivisions.get(i).setOrderlabel(paginator.next());
                }
            }
        } catch (NumberFormatException e) {
            Helper.setErrorMessage("paginationFormatError", new Object[] { paginationStartValue });
        }
        dataEditor.getMetadataPanel().update();
        dataEditor.refreshStructurePanel();
        PrimeFaces.current().executeScript("PF('notifications').renderMessage({'summary':'"
                + Helper.getTranslation("paginationSaved") + "','severity':'info'})");
    }

    /**
     * Show.
     */
    public void show() {
        paginationTypeSelectSelectedItem = resolveDefaultPaginationType();
        selectPaginationModeSelectedItem = selectPaginationModeItems.get(0);
        paginationStartValue = "1";
        fictitiousCheckboxChecked = false;
        selectPaginationScopeSelectedItem = Boolean.TRUE;
        prepareSelectPaginationScopeSelectedItem();
    }
}
