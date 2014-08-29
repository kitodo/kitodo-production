/**
 * This file is part of the Goobi Application - a Workflow tool for the support
 * of mass digitization.
 * 
 * (c) 2014 Goobi. Digialisieren im Verein e.V. &lt;contact@goobi.org&gt;
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
import java.util.HashSet;

import javax.faces.model.SelectItem;

import org.goobi.api.display.Item;
import org.goobi.api.display.enums.BindState;
import org.goobi.api.display.enums.DisplayType;
import org.goobi.api.display.helper.ConfigDispayRules;

import ugh.dl.MetadataType;
import de.sub.goobi.helper.Util;

public class RenderableListBox extends RenderableMetadatum implements RenderableGroupableMetadatum {

	private final ArrayList<Item> items;

	public RenderableListBox(MetadataType metadataType, RenderableMetadataGroup renderableMetadataGroup,
			String projectName, BindState bindState) {
		items = ConfigDispayRules.getInstance().getItemsByNameAndType(projectName, bindState.getTitle(),
				metadataType.getName(), DisplayType.select);
	}

	@Override
	public Collection<SelectItem> getItems() {
		ArrayList<SelectItem> result = new ArrayList<SelectItem>(items.size());
		for (Item item : items) {
			result.add(new SelectItem(item.getValue(), item.getLabel()));
		}
		return result;
	}

	@Override
	public Collection<String> getSelectedItems() {
		HashSet<String> result = new HashSet<String>(Util.mapCapacityFor(items));
		for (Item item : items) {
			result.add(item.getValue());
		}
		return result;
	}

	@Override
	public String getValue() {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setSelectedItems(Collection<String> selected) {
		HashSet<String> selectedSet = selected instanceof HashSet ? (HashSet<String>) selected : new HashSet<String>(
				selected);
		for (Item item : items) {
			item.setIsSelected(selectedSet.contains(item.getValue()));
		}
	}

	@Override
	public void setValue(String value) {
		throw new UnsupportedOperationException();
	}

}
