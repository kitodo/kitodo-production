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
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.faces.model.SelectItem;

import org.kitodo.api.dataformat.LogicalDivision;
import org.kitodo.api.dataformat.PhysicalDivision;
import org.kitodo.api.dataformat.View;
import org.kitodo.production.metadata.MetadataEditor;

public class EditPagesDialog {

    private final DataEditorForm dataEditor;

    /**
     * Views on physical divisions that are not associated with this structure.
     */
    private List<SelectItem> paginationSelectionItems;

    /**
     * Views on physical divisions that are not associated with this structure selected
     * by the user to add them.
     */
    private List<Integer> paginationSelectionSelectedItems = new ArrayList<>();

    /**
     * Views on physical divisions that are associated with this structure.
     */
    private List<SelectItem> paginationSubSelectionItems;

    /**
     * Views on physical divisions that are associated with this structure selected by
     * the user to remove them.
     */
    private List<Integer> paginationSubSelectionSelectedItems = new ArrayList<>();

    /**
     * The first of the views to be assigned.
     */
    private Integer selectFirstPageSelectedItem;

    /**
     * The last of the views to be assigned.
     */
    private Integer selectLastPageSelectedItem;

    /**
     * The totality of views.
     */
    private List<SelectItem> selectPageItems;

    /**
     * Constructor.
     *
     * @param dataEditor
     *          DataEditorForm of this EditPagesDialog
     */
    EditPagesDialog(DataEditorForm dataEditor) {
        this.dataEditor = dataEditor;
    }

    /**
     * This method is invoked if the user clicks on the add page btn command
     * button.
     */
    public void addPage() {
        Optional<LogicalDivision> selectedStructure = dataEditor.getSelectedStructure();
        if (selectedStructure.isPresent()) {
            for (View viewToAdd : getViewsToAdd(paginationSelectionSelectedItems)) {
                dataEditor.assignView(selectedStructure.get(), viewToAdd, -1);
            }
            dataEditor.refreshStructurePanel();
            prepare();
        }
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
        this.paginationSelectionSelectedItems = paginationSelectionSelectedItems;
    }

    /**
     * Returns the selected items of the paginationSubSelection select menu.
     *
     * @return the selected items of the paginationSubSelection
     */
    public List<Integer> getPaginationSubSelectionSelectedItems() {
        return paginationSubSelectionSelectedItems;
    }

    /**
     * Sets the selected items of the paginationSubSelection select menu.
     *
     * @param paginationSubSelectionSelectedItems
     *            selected items to set
     */
    public void setPaginationSubSelectionSelectedItems(List<Integer> paginationSubSelectionSelectedItems) {
        this.paginationSubSelectionSelectedItems = paginationSubSelectionSelectedItems;
    }

    /**
     * Returns the selected item of the selectFirstPage drop-down menu.
     *
     * @return the selected item of the selectFirstPage
     */
    public Integer getSelectFirstPageSelectedItem() {
        return selectFirstPageSelectedItem;
    }

    /**
     * Sets the selected item of the selectFirstPage drop-down menu.
     *
     * @param selectFirstPageSelectedItem
     *            selected item to set
     */
    public void setSelectFirstPageSelectedItem(Integer selectFirstPageSelectedItem) {
        this.selectFirstPageSelectedItem = selectFirstPageSelectedItem;
    }

    /**
     * Returns the selected item of the selectLastPage drop-down menu.
     *
     * @return the selected item of the selectLastPage
     */
    public Integer getSelectLastPageSelectedItem() {
        return selectLastPageSelectedItem;
    }

    /**
     * Sets the selected item of the selectLastPage drop-down menu.
     *
     * @param selectLastPageSelectedItem
     *            selected item to set
     */
    public void setSelectLastPageSelectedItem(Integer selectLastPageSelectedItem) {
        this.selectLastPageSelectedItem = selectLastPageSelectedItem;
    }

    /**
     * Returns the items of the paginationSelection select menu.
     *
     * @return the items of the paginationSelection
     */
    public List<SelectItem> getPaginationSelectionItems() {
        return paginationSelectionItems;
    }

    /**
     * Returns the items of the paginationSubSelection select menu.
     *
     * @return the items of the paginationSubSelection
     */
    public List<SelectItem> getPaginationSubSelectionItems() {
        return paginationSubSelectionItems;
    }

    /**
     * Returns the items of the selectFirstPage and selectLastPage drop-down
     * menus.
     *
     * @return the items of the selectFirstPage and selectLastPage
     */
    public List<SelectItem> getSelectPageItems() {
        return selectPageItems;
    }

    List<View> getViewsToAdd(int firstPage, int lastPage) {
        boolean forward = firstPage <= lastPage;
        List<Integer> pages = Stream.iterate(firstPage, i -> forward ? i + 1 : i - 1)
                .limit(Math.abs(firstPage - lastPage) + 1).collect(Collectors.toList());
        return getViewsToAdd(pages);
    }

    private List<View> getViewsToAdd(List<Integer> pages) {
        return pages.parallelStream()
                .map(dataEditor.getWorkpiece().getAllPhysicalDivisionChildrenSortedFilteredByPageAndTrack()::get)
                .map(MetadataEditor::getFirstViewForPhysicalDivision)
                .collect(Collectors.toList());
    }

    /**
     * This method is invoked if the user clicks on the set page start and end
     * btn command button.
     */
    public void setPageStartAndEnd() {
        Optional<LogicalDivision> selectedStructure = dataEditor.getSelectedStructure();
        if (selectedStructure.isPresent()) {
            for (View viewToAdd : getViewsToAdd(selectFirstPageSelectedItem, selectLastPageSelectedItem)) {
                dataEditor.assignView(selectedStructure.get(), viewToAdd, -1);
            }
            dataEditor.refreshStructurePanel();
            prepare();
        }
    }

    void prepare() {
        // refresh selectable items
        selectPageItems = new ArrayList<>();
        paginationSubSelectionItems = new ArrayList<>();
        paginationSelectionItems = new ArrayList<>();

        List<PhysicalDivision> physicalDivisions = dataEditor.getWorkpiece().getAllPhysicalDivisionChildrenSortedFilteredByPageAndTrack();
        int capacity = (int) Math.ceil(physicalDivisions.size() / .75);
        Set<Integer> assigneds = new HashSet<>(capacity);
        Set<Integer> unassigneds = new HashSet<>(capacity);
        for (int i = 0; i < physicalDivisions.size(); i++) {
            PhysicalDivision physicalDivision = physicalDivisions.get(i);
            View view = MetadataEditor.createUnrestrictedViewOn(physicalDivision);
            String label = Objects.isNull(physicalDivision.getOrderlabel()) ? Integer.toString(physicalDivision.getOrder())
                    : physicalDivision.getOrder() + " : " + physicalDivision.getOrderlabel();
            Integer id = i;
            SelectItem selectItem = new SelectItem(id, label);
            selectPageItems.add(selectItem);
            Optional<LogicalDivision> selectedStructure = dataEditor.getSelectedStructure();
            boolean assigned = selectedStructure.isPresent()
                    && selectedStructure.get().getViews().contains(view);
            (assigned ? paginationSubSelectionItems : paginationSelectionItems).add(selectItem);
            (assigned ? assigneds : unassigneds).add(id);
        }

        // refresh selections
        if (Objects.isNull(selectFirstPageSelectedItem) && !selectPageItems.isEmpty()) {
            selectFirstPageSelectedItem = (Integer) selectPageItems.get(0).getValue();
        }
        if (Objects.isNull(selectFirstPageSelectedItem) && !selectPageItems.isEmpty()) {
            selectFirstPageSelectedItem = (Integer) selectPageItems.get(selectPageItems.size() - 1).getValue();
        }
        paginationSubSelectionSelectedItems.retainAll(assigneds);
        paginationSelectionSelectedItems.retainAll(unassigneds);
    }

    /**
     * This method is invoked if the user clicks on the remove page btn command
     * button.
     */
    public void removePage() {
        Optional<LogicalDivision> selectedStructure = dataEditor.getSelectedStructure();
        if (selectedStructure.isPresent()) {
            for (View viewToRemove : getViewsToAdd(paginationSubSelectionSelectedItems)) {
                dataEditor.unassignView(selectedStructure.get(), viewToRemove, false);
            }
            dataEditor.refreshStructurePanel();
            prepare();
        }
    }
}
