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

package de.sub.goobi.metadaten;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.faces.model.SelectItem;

import org.goobi.api.display.Item;
import org.goobi.api.display.enums.DisplayType;
import org.kitodo.api.ugh.MetadataInterface;
import org.kitodo.api.ugh.MetadataGroupInterface;
import org.kitodo.api.ugh.MetadataTypeInterface;

/**
 * Backing bean for a select list style input element to edit a metadatum with
 * the option to select one or more predefined values renderable by JSF.
 * 
 * @author Matthias Ronge &lt;matthias.ronge@zeutschel.de&gt;
 */
public class RenderableListBox extends RenderableMetadatum implements RenderableGroupableMetadatum {

    /**
     * Holds the options to show in the select list, including their selection
     * state.
     */
    private final Collection<Item> items;

    /**
     * Constructor. Creates a RenderableListBox.
     * 
     * @param metadataTypeInterface
     *            metadata type editable by this list element
     * @param binding
     *            a metadata group whose values shall be updated if the setter
     *            methods are called (optional, may be null)
     * @param container
     *            metadata group this list is showing in
     * @param projectName
     *            project of the process owning this metadatum
     */
    public RenderableListBox(MetadataTypeInterface metadataTypeInterface, MetadataGroupInterface binding, RenderableMetadataGroup container,
            String projectName) {
        super(metadataTypeInterface, binding, container);
        items = getItems(projectName, DisplayType.select);
        if (binding != null) {
            List<MetadataInterface> elements = binding.getMetadataByType(metadataTypeInterface.getName());
            List<String> selected = new ArrayList<>(elements.size());
            for (MetadataInterface m : elements) {
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
    public void addContent(MetadataInterface data) {
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
     * Returns a list of identifiers of the items currently selected.
     * 
     * @return the items currently selected
     */
    public List<String> getSelectedItems() {
        List<String> result = new ArrayList<>(items.size());
        for (Item item : items) {
            if (item.getIsSelected()) {
                result.add(item.getValue());
            }
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
     * Returns the value of this edit component as metadata elements
     * 
     * @return a list of metadata elements with the selected values of this
     *         input
     * @see de.sub.goobi.metadaten.RenderableGroupableMetadatum#toMetadata()
     */
    @Override
    public List<MetadataInterface> toMetadata() {
        List<MetadataInterface> result = new ArrayList<>(items.size());
        for (Item item : items) {
            if (item.getIsSelected()) {
                result.add(getMetadata(item.getValue()));
            }
        }
        return result;
    }
}
