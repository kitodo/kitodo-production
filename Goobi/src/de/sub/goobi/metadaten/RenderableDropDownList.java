package de.sub.goobi.metadaten;

import java.util.ArrayList;
import java.util.Collection;

import javax.faces.model.SelectItem;

import org.goobi.api.display.Item;
import org.goobi.api.display.enums.BindState;
import org.goobi.api.display.enums.DisplayType;
import org.goobi.api.display.helper.ConfigDispayRules;

import ugh.dl.MetadataType;

public class RenderableDropDownList extends RenderableMetadatum implements RenderableGroupableMetadatum {

	private final ArrayList<Item> items;

	public RenderableDropDownList(MetadataType metadataType, RenderableMetadataGroup renderableMetadataGroup,
			String projectName, BindState bindState) {
		items = ConfigDispayRules.getInstance().getItemsByNameAndType(projectName, bindState.getTitle(),
				metadataType.getName(), DisplayType.select1);
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
		throw new UnsupportedOperationException();
	}

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

	@Override
	public void setSelectedItems(Collection<String> selectedItems) {
		throw new UnsupportedOperationException();
	}

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
}
