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
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 59 Temple
 * Place, Suite 330, Boston, MA 02111-1307 USA
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
import org.goobi.api.display.enums.BindState;
import org.goobi.api.display.enums.DisplayType;
import org.goobi.api.display.helper.ConfigDispayRules;

import ugh.dl.Metadata;
import ugh.dl.MetadataType;

/**
 * A RenderableDropDonwList is a backing bean for a drop-down select element to
 * edit a choose-from kind of metadatum with the option to select exactly one
 * value renderable by JSF.
 * 
 * @author Matthias Ronge &lt;matthias.ronge@zeutschel.de&gt;
 */
public class RenderableDropDownList extends RenderableMetadatum implements RenderableGroupableMetadatum,
		SingleValueMetadatum {

	private final ArrayList<Item> items;

	/**
	 * Constructor. Creates a RenderableDropDonwList.
	 * 
	 * @param metadataType
	 *            metadata type editable by this drop-down list
	 * @param container
	 *            metadata group this drop-down list is showing in
	 * @param projectName
	 *            project of the process owning this metadatum
	 * @param bindState
	 *            whether the user is about to create the metadatum anew or edit
	 *            a previously existing one
	 */
	public RenderableDropDownList(MetadataType metadataType, RenderableMetadataGroup container, String projectName,
			BindState bindState) {
		super(metadataType, container);
		items = ConfigDispayRules.getInstance().getItemsByNameAndType(projectName, bindState.getTitle(),
				metadataType.getName(), DisplayType.select1);
	}

	/**
	 * Returns the available items for the the user to choose from.
	 * 
	 * @return the items to choose from
	 * @see de.sub.goobi.metadaten.RenderableGroupableMetadatum#getItems()
	 */
	public Collection<SelectItem> getItems() {
		ArrayList<SelectItem> result = new ArrayList<SelectItem>(items.size());
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
	 * @see de.sub.goobi.metadaten.RenderableGroupableMetadatum#getValue()
	 */
	@Override
	public String getValue() {
		for (Item item : items) {
			if (item.getIsSelected()) {
				return item.getValue();
			} else {
				return items.get(0).getValue();
			}
		}
		return null;
	}

	/**
	 * Uses the passed in identifier of the item to be selected to find the firt
	 * items in the item list in order to mark it as selected and to mark all
	 * other items in the item list as not selected.
	 * 
	 * @param value
	 *            identifier of the item to be marked as selected
	 * 
	 * @see de.sub.goobi.metadaten.RenderableGroupableMetadatum#setValue(java.lang.String)
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
	}

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
