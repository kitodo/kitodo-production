package de.sub.goobi.metadaten;

import java.util.Collection;

import javax.faces.model.SelectItem;

import ugh.dl.MetadataType;

public class RenderableEdit extends RenderableMetadatum implements RenderableGroupedMetadatum {

	private String value;

	public RenderableEdit(MetadataType metadataType) {
		super.labels = metadataType.getAllLanguages();
	}

	@Override
	public Collection<SelectItem> getItems() {
		throw new UnsupportedOperationException();
	}

	@Override
	public Collection<String> getSelectedItems() {
		throw new UnsupportedOperationException();
	}

	@Override
	public String getValue() {
		return value != null ? value : "";
	}

	@Override
	public boolean isMultiselect() {
		return false;
	}

	@Override
	public void setSelectedItems(Collection<String> selectedItems) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setValue(String value) {
		this.value = value;
	}

}
