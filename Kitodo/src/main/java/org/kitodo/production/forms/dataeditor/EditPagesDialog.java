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

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.faces.model.SelectItem;

import org.kitodo.api.dataformat.MediaUnit;
import org.kitodo.api.dataformat.Structure;
import org.kitodo.api.dataformat.View;
import org.kitodo.api.dataformat.Workpiece;

public class EditPagesDialog {

    private DataEditorForm dataEditor;

    /**
     * Views on media units that are not associated with this structure.
     */
    private List<SelectItem> paginationSelectionItems;

    /**
     * Views on media units that are not associated with this structure selected
     * by the user to add them.
     */
    private List<Integer> paginationSelectionSelectedItems;

    /**
     * Views on media units that are associated with this structure.
     */
    private List<SelectItem> paginationSubSelectionItems;

    /**
     * Views on media units that are associated with this structure selected by
     * the user to remove them.
     */
    private List<Integer> paginationSubSelectionSelectedItems;

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
     * The selected structure.
     */
    private Structure structure;

    /**
     * The current workpiece.
     */
    private Workpiece workpiece;

    public EditPagesDialog(DataEditorForm dataEditor) {
        this.dataEditor = dataEditor;
    }

    /**
     * This method is invoked if the user clicks on the add page btn command
     * button.
     */
    public void addPageBtnClick() {
        structure.getViews().addAll(getViewsToAdd(workpiece, paginationSelectionSelectedItems));
        dataEditor.refreshStructurePanel();
        refreshDialog();
    }

    public List<Integer> getPaginationSelectionSelectedItems() {
        return paginationSelectionSelectedItems;
    }

    public List<Integer> getPaginationSubSelectionSelectedItems() {
        return paginationSubSelectionSelectedItems;
    }

    public Integer getSelectFirstPageSelectedItem() {
        return selectFirstPageSelectedItem;
    }

    public Integer getSelectLastPageSelectedItem() {
        return selectLastPageSelectedItem;
    }

    public List<SelectItem> getPaginationSelectionItems() {
        return paginationSelectionItems;
    }

    public List<SelectItem> getPaginationSubSelectionItems() {
        return paginationSubSelectionItems;
    }

    public List<SelectItem> getSelectPageItems() {
        return selectPageItems;
    }

    static List<View> getViewsToAdd(Workpiece workpiece, int firstPage, int lastPage) {
        boolean forward = firstPage <= lastPage;
        List<Integer> pages = Stream.iterate(firstPage, i -> forward ? i + 1 : i - 1)
                .limit(Math.abs(firstPage - lastPage) + 1).collect(Collectors.toList());
        return getViewsToAdd(workpiece, pages);
    }

    private static List<View> getViewsToAdd(Workpiece workpiece, List<Integer> pages) {
        return pages.parallelStream().map(workpiece.getMediaUnits()::get).map(MetadataEditor::createUnrestrictedViewOn)
                .collect(Collectors.toList());
    }

    void prepare(Workpiece workpiece, Structure selectedStructure) {
        this.workpiece = workpiece;
        this.structure = selectedStructure;
        refreshDialog();
    }

    private void refreshDialog() {
        // refresh selectable items
        List<MediaUnit> mediaUnits = workpiece.getMediaUnits();
        int capacity = (int) Math.ceil(mediaUnits.size() / .75);
        Set<Integer> assigneds = new HashSet<>(capacity);
        Set<Integer> unassigneds = new HashSet<>(capacity);
        for (int i = 0; i < mediaUnits.size(); i++) {
            MediaUnit mediaUnit = mediaUnits.get(i);
            View view = MetadataEditor.createUnrestrictedViewOn(mediaUnit);
            String label = Objects.isNull(mediaUnit.getOrderlabel()) ? Integer.toString(mediaUnit.getOrder())
                    : mediaUnit.getOrder() + " : " + mediaUnit.getOrderlabel();
            Integer id = Integer.valueOf(i);
            SelectItem selectItem = new SelectItem(id, label);
            this.selectPageItems.add(selectItem);
            boolean assigned = structure.getViews().contains(view);
            (assigned ? paginationSubSelectionItems : paginationSelectionItems).add(selectItem);
            (assigned ? assigneds : unassigneds).add(id);
        }

        // refresh selections
        if (Objects.isNull(selectFirstPageSelectedItem)) {
            selectFirstPageSelectedItem = (Integer) selectPageItems.get(0).getValue();
        }
        if (Objects.isNull(selectFirstPageSelectedItem)) {
            selectFirstPageSelectedItem = (Integer) selectPageItems.get(selectPageItems.size() - 1).getValue();
        }
        paginationSubSelectionSelectedItems.retainAll(assigneds);
        paginationSelectionSelectedItems.retainAll(unassigneds);
    }

    /**
     * This method is invoked if the user clicks on the set page start and end
     * btn command button.
     */
    public void setPageStartAndEndBtnClick() {
        structure.getViews().addAll(getViewsToAdd(workpiece, selectFirstPageSelectedItem, selectLastPageSelectedItem));
        dataEditor.refreshStructurePanel();
        refreshDialog();
    }

    public void setPaginationSelectionSelectedItems(List<Integer> paginationSelectionSelectedItems) {
        this.paginationSelectionSelectedItems = paginationSelectionSelectedItems;
    }

    public void setPaginationSubSelectionSelectedItems(List<Integer> paginationSubSelectionSelectedItems) {
        this.paginationSubSelectionSelectedItems = paginationSubSelectionSelectedItems;
    }

    public void setSelectFirstPageSelectedItem(Integer selectFirstPageSelectedItem) {
        this.selectFirstPageSelectedItem = selectFirstPageSelectedItem;
    }

    public void setSelectLastPageSelectedItem(Integer selectLastPageSelectedItem) {
        this.selectLastPageSelectedItem = selectLastPageSelectedItem;
    }

    /**
     * This method is invoked if the user clicks on the take pages from children
     * btn command button.
     */
    public void takePagesFromChildrenBtnClick() {
        MetadataEditor.assignViewsFromChildren(structure);
        dataEditor.refreshStructurePanel();
        refreshDialog();
    }

    /**
     * This method is invoked if the user clicks on the remove page btn command
     * button.
     */
    public void removePageBtnClick() {
        structure.getViews().removeAll(getViewsToAdd(workpiece, paginationSubSelectionSelectedItems));
        dataEditor.refreshStructurePanel();
        refreshDialog();
    }
}
