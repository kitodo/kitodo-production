/**
 * This file is part of the Goobi Application - a Workflow tool for the support
 * of mass digitization.
 * 
 * (c) 2014 Goobi. Digitalisieren im Verein e. V. <contact@goobi.org>
 * 
 * Visit the websites for more information.
 *     		- http://www.kitodo.org/en/
 *     		- https://github.com/goobi
 * 
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
 * Linking this library statically or dynamically with other modules is making a
 * combined work based on this library. Thus, the terms and conditions of the
 * GNU General Public License cover the whole combination. As a special
 * exception, the copyright holders of this library give you permission to link
 * this library with independent modules to produce an executable, regardless of
 * the license terms of these independent modules, and to copy and distribute
 * the resulting executable under terms of your choice, provided that you also
 * meet, for each linked independent module, the terms and conditions of the
 * license of that module. An independent module is a module which is not
 * derived from or based on this library. If you modify this library, you may
 * extend this exception to your version of the library, but you are not obliged
 * to do so. If you do not wish to do so, delete this exception statement from
 * your version.
 */

package de.sub.goobi.metadaten;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.faces.model.SelectItem;

import org.goobi.api.display.Item;
import org.goobi.api.display.enums.DisplayType;

import ugh.dl.Metadata;
import ugh.dl.MetadataGroup;
import ugh.dl.MetadataType;

/**
 * Backing bean for a drop-down style select element to edit a single-select metadatum renderable by JSF.
 *
 * @author Matthias Ronge &lt;matthias.ronge@zeutschel.de&gt;
 */
public class RenderableDropDownList extends RenderableMetadatum implements RenderableGroupableMetadatum,
		SingleValueRenderableMetadatum {

	/**
	 * A list holding the items to display in the drop-down list.
	 */
	private final Collection<Item> items;

	/**
	 * Constructor to create a backing bean for a drop-down select element renderable by JSF to edit a choose-from
	 * kind of metadatum with the option to select exactly one value.
	 *
	 * @param metadataType metadata type editable by this drop-down list
	 * @param binding metadata group whose value shall be updated if the setter on the backing bean is invoked,
	 *                   may be null
	 * @param container metadata group this drop-down list is showing in
	 * @param projectName project of the process owning this metadatum
	 */
	public RenderableDropDownList(MetadataType metadataType, MetadataGroup binding, RenderableMetadataGroup container,
			String projectName) {
		super(metadataType, binding, container);
		items = getItems(projectName, DisplayType.select1);
		if (binding != null) {
			for (Metadata data : binding.getMetadataByType(metadataType.getName())) {
				addContent(data);
			}
		}
	}

	/**
	 * Adds the data passed from the metadata element as content to the input. This will overwrite any previously
	 * set value.
	 *
	 * @param data data to add
	 * @see de.sub.goobi.metadaten.RenderableGroupableMetadatum#addContent(ugh.dl.Metadata)
	 */
	@Override
	public void addContent(Metadata data) {
		setValue(data.getValue());
	}

	/**
	 * Returns the available items for the the user to choose from.
	 *
	 * @return the items to choose from
	 */
	public Collection<SelectItem> getItems() {
		ArrayList<SelectItem> result = new ArrayList<SelectItem>(items.size());
		for (Item item : items) {
			result.add(new SelectItem(item.getValue(), item.getLabel()));
		}
		return result;
	}

	/**
	 * Returns the identifier of the item currently selected in this drop-down list. If multiple items are internally
	 * marked as selected (which is  possible if the metadata type under edit was bound to a multi-select list box
	 * during creation and is later bound to a drop-down list box for editing) the first of them will be selected. If
	 * no item has been selected yet the first available item will be selected.
	 *
	 * @return the identifier of the selected item
	 * @see de.sub.goobi.metadaten.SingleValueRenderableMetadatum#getValue()
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
	 * Uses the passed-in identifier of the item to be selected to find the first items in the item list in order to
	 * mark it as selected and to mark all other items in the item list as not selected.
	 *
	 * @param value identifier of the item to be marked as selected
	 *
	 * @see de.sub.goobi.metadaten.SingleValueRenderableMetadatum#setValue(java.lang.String)
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
	 * Returns a metadata element that contains the value selected in the drop-down list.
	 *
	 * @return the selected value as metadata element
	 * @see de.sub.goobi.metadaten.RenderableGroupableMetadatum#toMetadata()
	 */
	@Override
	public List<Metadata> toMetadata() {
		List<Metadata> result = new ArrayList<Metadata>(1);
		for (Item item : items) {
			if (item.getIsSelected()) {
				result.add(getMetadata(item.getValue()));
			}
		}
		return result;
	}
}
