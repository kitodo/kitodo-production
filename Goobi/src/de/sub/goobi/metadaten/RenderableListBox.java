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
