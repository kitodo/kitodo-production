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
<<<<<<< HEAD:Kitodo/src/main/java/org/kitodo/production/metadata/elements/renderable/ListBox.java
import org.kitodo.api.ugh.MetadataTypeInterface;
import org.kitodo.production.helper.metadata.legacytypeimplementations.LegacyMetadataHelper;
import org.kitodo.production.metadata.display.Item;
import org.kitodo.production.metadata.display.enums.DisplayType;
=======
import org.kitodo.helper.metadata.LegacyMetadataHelper;
import org.kitodo.helper.metadata.LegacyMetadataTypeHelper;
import org.kitodo.metadata.display.Item;
import org.kitodo.metadata.display.enums.DisplayType;
>>>>>>> Remove references to MetadataTypeInterface [not compilable]:Kitodo/src/main/java/org/kitodo/metadata/elements/renderable/ListBox.java

/**
 * Backing bean for a select list style input element to edit a metadata with
 * the option to select one or more predefined values renderable by JSF.
 *
 * @author Matthias Ronge &lt;matthias.ronge@zeutschel.de&gt;
 */
public class ListBox extends RenderableMetadata implements RenderableGroupableMetadata {

    /**
     * Holds the options to show in the select list, including their selection
     * state.
     */
    private final Collection<Item> items;

    /**
     * Constructor. Creates a RenderableListBox.
     *
     * @param metadataType
     *            metadata type editable by this list element
     * @param binding
     *            a metadata group whose values shall be updated if the setter
     *            methods are called (optional, may be null)
     * @param container
     *            metadata group this list is showing in
     * @param projectName
     *            project of the process owning this metadata
     */
    public ListBox(LegacyMetadataTypeHelper metadataType, MetadataGroupInterface binding,
                   RenderableMetadataGroup container, String projectName) {

        super(metadataType, binding, container);
        items = getItems(projectName, DisplayType.SELECT);
        if (binding != null) {
            List<LegacyMetadataHelper> elements = binding.getMetadataByType(metadataType.getName());
            List<String> selected = new ArrayList<>(elements.size());
            for (LegacyMetadataHelper m : elements) {
                selected.add(m.getValue());
            }
            setSelectedItems(selected);
        }
    }

    /**
     * Selects all items whose values are equal to the value to set.
     *
     * @param data
     *            data to add
     */
    @Override
    public void addContent(LegacyMetadataHelper data) {
        String valueToSet = data.getValue();
        for (Item item : items) {
            if (valueToSet.equals(item.getValue())) {
                item.setIsSelected(true);
            }
        }
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
     * Uses the passed-in list of identifiers of the items that shall be
     * selected to set the selected state on the items.
     *
     * @param selected
     *            list of identifiers of items to be selected
     */
    public void setSelectedItems(List<String> selected) {
        for (Item item : items) {
            item.setIsSelected(selected.contains(item.getValue()));
        }
        updateBinding();
    }

    /**
     * Returns the value of this edit component as metadata elements.
     *
     * @return a list of metadata elements with the selected values of this
     *         input
     * @see org.kitodo.production.metadata.elements.renderable.RenderableGroupableMetadata#toMetadata()
     */
    @Override
    public List<LegacyMetadataHelper> toMetadata() {
        List<LegacyMetadataHelper> result = new ArrayList<>(items.size());
        for (Item item : items) {
            if (item.getIsSelected()) {
                result.add(getMetadata(item.getValue()));
            }
        }
        return result;
    }
}
