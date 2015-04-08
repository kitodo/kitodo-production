/**
 * This file is part of the Goobi Application - a Workflow tool for the support
 * of mass digitization.
 * 
 * (c) 2014 Goobi. Digitalisieren im Verein e.V. &lt;contact@goobi.org&gt;
 * 
 * Visit the websites for more information.
 *     		- http://www.goobi.org/en/
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
 * Backing bean for a select list style input element to edit a metadatum with
 * the option to select one or more predefined values renderable by JSF.
 * 
 * @author Matthias Ronge &lt;matthias.ronge@zeutschel.de&gt;
 */
public class RenderableListBox extends RenderableMetadatum implements RenderableGroupableMetadatum {

	/**
	 * Holds the options to show in the select list, including their selection
	 * state
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
	 *            project of the process owning this metadatum
	 */
	public RenderableListBox(MetadataType metadataType, MetadataGroup binding, RenderableMetadataGroup container,
			String projectName) {
		super(metadataType, binding, container);
		items = getItems(projectName, DisplayType.select);
		if (binding != null) {
			List<Metadata> elements = binding.getMetadataByType(metadataType.getName());
			List<String> selected = new ArrayList<String>(elements.size());
			for (Metadata m : elements) {
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
	public void addContent(Metadata data) {
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
		ArrayList<SelectItem> result = new ArrayList<SelectItem>(items.size());
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
		List<String> result = new ArrayList<String>(items.size());
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
	public List<Metadata> toMetadata() {
		List<Metadata> result = new ArrayList<Metadata>(items.size());
		for (Item item : items) {
			if (item.getIsSelected()) {
				result.add(getMetadata(item.getValue()));
			}
		}
		return result;
	}
}
