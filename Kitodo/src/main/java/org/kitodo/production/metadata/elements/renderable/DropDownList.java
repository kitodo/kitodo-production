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

package org.kitodo.production.metadata.elements.renderable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.faces.model.SelectItem;

import org.kitodo.api.ugh.MetadataGroupInterface;
<<<<<<< HEAD:Kitodo/src/main/java/org/kitodo/production/metadata/elements/renderable/DropDownList.java
import org.kitodo.api.ugh.MetadataTypeInterface;
import org.kitodo.production.helper.metadata.legacytypeimplementations.LegacyMetadataHelper;
import org.kitodo.production.metadata.display.Item;
import org.kitodo.production.metadata.display.enums.DisplayType;
=======
import org.kitodo.helper.metadata.LegacyMetadataHelper;
import org.kitodo.helper.metadata.LegacyMetadataTypeHelper;
import org.kitodo.metadata.display.Item;
import org.kitodo.metadata.display.enums.DisplayType;
>>>>>>> Remove references to MetadataTypeInterface [not compilable]:Kitodo/src/main/java/org/kitodo/metadata/elements/renderable/DropDownList.java

/**
 * Backing bean for a drop-down style select element to edit a single-select
 * metadata renderable by JSF.
 *
 * @author Matthias Ronge &lt;matthias.ronge@zeutschel.de&gt;
 */
public class DropDownList extends RenderableMetadata
        implements RenderableGroupableMetadata, SingleValueRenderableMetadata {

    /**
     * A list holding the items to display in the drop-down list.
     */
    private final Collection<Item> items;

    /**
     * Constructor to create a backing bean for a drop-down select element
     * renderable by JSF to edit a choose-from kind of metadata with the option
     * to select exactly one value.
     *
     * @param metadataType
     *            metadata type editable by this drop-down list
     * @param binding
     *            metadata group whose value shall be updated if the setter on
     *            the backing bean is invoked, may be null
     * @param container
     *            metadata group this drop-down list is showing in
     * @param projectName
     *            project of the process owning this metadata
     */
    public DropDownList(LegacyMetadataTypeHelper metadataType, MetadataGroupInterface binding,
                        RenderableMetadataGroup container, String projectName) {

        super(metadataType, binding, container);
        items = getItems(projectName, DisplayType.SELECT1);
        if (binding != null) {
            for (LegacyMetadataHelper data : binding.getMetadataByType(metadataType.getName())) {
                addContent(data);
            }
        }
    }

    /**
     * Adds the data passed from the metadata element as content to the input.
     * This will overwrite any previously set value.
     *
     * @param data
     *            data to add
     * @see org.kitodo.production.metadata.elements.renderable.RenderableGroupableMetadata#addContent(org.kitodo.api.ugh.LegacyMetadataHelper)
     */
    @Override
    public void addContent(LegacyMetadataHelper data) {
        setValue(data.getValue());
    }

    /**
     * Returns the available items for the the user to choose from.
     *
     * @return the items to choose from
     */
    public Collection<SelectItem> getItems() {
        ArrayList<SelectItem> result = new ArrayList<>(items.size());
        for (Item item : items) {
            result.add(new SelectItem(item.getValue(), item.getLabel()));
        }
        return result;
    }

    /**
     * Returns the identifier of the item currently selected in this drop-down
     * list. If multiple items are internally marked as selected (which is
     * possible if the metadata type under edit was bound to a multi-select list
     * box during creation and is later bound to a drop-down list box for
     * editing) the first of them will be selected. If no item has been selected
     * yet the first available item will be selected.
     *
     * @return the identifier of the selected item
     * @see org.kitodo.production.metadata.elements.renderable.SingleValueRenderableMetadata#getValue()
     */
    @Override
    public String getValue() {
        for (Item item : items) {
            if (item.getIsSelected()) {
                return item.getValue();
            }
        }
        return items.iterator().next().getValue();
    }

    /**
     * Uses the passed-in identifier of the item to be selected to find the
     * first items in the item list in order to mark it as selected and to mark
     * all other items in the item list as not selected.
     *
     * @param value
     *            identifier of the item to be marked as selected
     * @see org.kitodo.production.metadata.elements.renderable.SingleValueRenderableMetadata#setValue(java.lang.String)
     */
    @Override
    public void setValue(String value) {
        boolean search = true;
        for (Item item : items) {
            if (search && item.getValue().equals(value)) {
                item.setIsSelected(true);
                search = false;
            } else {
                item.setIsSelected(false);
            }
        }
        updateBinding();
    }

    /**
     * Returns a metadata element that contains the value selected in the
     * drop-down list.
     *
     * @return the selected value as metadata element
     * @see org.kitodo.production.metadata.elements.renderable.RenderableGroupableMetadata#toMetadata()
     */
    @Override
    public List<LegacyMetadataHelper> toMetadata() {
        List<LegacyMetadataHelper> result = new ArrayList<>(1);
        for (Item item : items) {
            if (item.getIsSelected()) {
                result.add(getMetadata(item.getValue()));
            }
        }
        return result;
    }
}
