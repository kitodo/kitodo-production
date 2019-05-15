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
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.faces.model.SelectItem;

import org.kitodo.api.dataformat.MediaUnit;
import org.kitodo.api.dataformat.View;
import org.kitodo.production.metadata.MetadataEditor;

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
    private List<Integer> paginationSelectionSelectedItems = new ArrayList<>();

    /**
     * Views on media units that are associated with this structure.
     */
    private List<SelectItem> paginationSubSelectionItems;

    /**
     * Views on media units that are associated with this structure selected by
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
    public void addPageBtnClick() {
        if (dataEditor.getSelectedStructure().isPresent()) {
            dataEditor.getSelectedStructure().get().getViews().addAll(getViewsToAdd(paginationSelectionSelectedItems));
            dataEditor.refreshStructurePanel();
            prepare();
        }
    }

    /**
     * Return paginationSelectionSelectedItems.
     *
     * @return paginationSelectionSelectedItems
     */
    public List<Integer> getPaginationSelectionSelectedItems() {
        return paginationSelectionSelectedItems;
    }

    /**
     * Set paginationSelectionSelectedItems.
     *
     * @param paginationSelectionSelectedItems
     *          paginationSelectionSelectedItems
     */
    public void setPaginationSelectionSelectedItems(List<Integer> paginationSelectionSelectedItems) {
        this.paginationSelectionSelectedItems = paginationSelectionSelectedItems;
    }

    /**
     * Return paginationSubSelectionSelectedItems.
     *
     * @return paginationSubSelectionSelectedItems
     */
    public List<Integer> getPaginationSubSelectionSelectedItems() {
        return paginationSubSelectionSelectedItems;
    }

    /**
     * Set paginationSubSelectionSelectedItems.
     *
     * @param paginationSubSelectionSelectedItems
     *          paginationSubSelectionSelectedItems
     */
    public void setPaginationSubSelectionSelectedItems(List<Integer> paginationSubSelectionSelectedItems) {
        this.paginationSubSelectionSelectedItems = paginationSubSelectionSelectedItems;
    }

    /**
     * Return selectFirstPageSelectedItem.
     *
     * @return selectFirstPageSelectedItem
     */
    public Integer getSelectFirstPageSelectedItem() {
        return selectFirstPageSelectedItem;
    }

    /**
     * Set selectFirstPageSelectedItem.
     *
     * @param selectFirstPageSelectedItem
     *          selectFirstPageSelectedItem
     */
    public void setSelectFirstPageSelectedItem(Integer selectFirstPageSelectedItem) {
        this.selectFirstPageSelectedItem = selectFirstPageSelectedItem;
    }

    /**
     * Return selectLastPageSelectedItem.
     *
     * @return selectLastPageSelectedItem
     */
    public Integer getSelectLastPageSelectedItem() {
        return selectLastPageSelectedItem;
    }

    /**
     * Set selectLastPageSelectedItem.
     *
     * @param selectLastPageSelectedItem
     *          selectLastPageSelectedItem
     */
    public void setSelectLastPageSelectedItem(Integer selectLastPageSelectedItem) {
        this.selectLastPageSelectedItem = selectLastPageSelectedItem;
    }

    /**
     * Return paginationSelectionItems.
     *
     * @return paginationSelectionItems
     */
    public List<SelectItem> getPaginationSelectionItems() {
        return paginationSelectionItems;
    }

    /**
     * Return paginationSubSelectionItems.
     *
     * @return paginationSubSelectionItems
     */
    public List<SelectItem> getPaginationSubSelectionItems() {
        return paginationSubSelectionItems;
    }

    /**
     * Return selectPageItems.
     *
     * @return selectPageItems
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
        return pages.parallelStream().map(dataEditor.getWorkpiece().getMediaUnit().getChildren()::get)
                .map(MetadataEditor::createUnrestrictedViewOn).collect(Collectors.toList());
    }

    /**
     * This method is invoked if the user clicks on the set page start and end
     * btn command button.
     */
    public void setPageStartAndEndBtnClick() {
        if (dataEditor.getSelectedStructure().isPresent()) {
            dataEditor.getSelectedStructure().get().getViews()
                    .addAll(getViewsToAdd(selectFirstPageSelectedItem, selectLastPageSelectedItem));
            dataEditor.refreshStructurePanel();
            prepare();
        }
    }

    void prepare() {
        // refresh selectable items
        selectPageItems = new ArrayList<>();
        paginationSubSelectionItems = new ArrayList<>();
        paginationSelectionItems = new ArrayList<>();

        List<MediaUnit> mediaUnits = dataEditor.getWorkpiece().getMediaUnit().getChildren();
        int capacity = (int) Math.ceil(mediaUnits.size() / .75);
        Set<Integer> assigneds = new HashSet<>(capacity);
        Set<Integer> unassigneds = new HashSet<>(capacity);
        for (int i = 0; i < mediaUnits.size(); i++) {
            MediaUnit mediaUnit = mediaUnits.get(i);
            View view = MetadataEditor.createUnrestrictedViewOn(mediaUnit);
            String label = Objects.isNull(mediaUnit.getOrderlabel()) ? Integer.toString(mediaUnit.getOrder())
                    : mediaUnit.getOrder() + " : " + mediaUnit.getOrderlabel();
            Integer id = i;
            SelectItem selectItem = new SelectItem(id, label);
            selectPageItems.add(selectItem);
            boolean assigned = dataEditor.getSelectedStructure().isPresent()
                    && dataEditor.getSelectedStructure().get().getViews().contains(view);
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
     * This method is invoked if the user clicks on the take pages from children
     * btn command button.
     */
    public void takePagesFromChildrenBtnClick() {
        if (dataEditor.getSelectedStructure().isPresent()) {
            MetadataEditor.assignViewsFromChildren(dataEditor.getSelectedStructure().get());
            dataEditor.refreshStructurePanel();
            prepare();
        }
    }

    /**
     * This method is invoked if the user clicks on the remove page btn command
     * button.
     */
    public void removePageBtnClick() {
        if (dataEditor.getSelectedStructure().isPresent()) {
            dataEditor.getSelectedStructure().get().getViews()
                    .removeAll(getViewsToAdd(paginationSubSelectionSelectedItems));
            dataEditor.refreshStructurePanel();
            prepare();
        }
    }
}
