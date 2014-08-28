package de.sub.goobi.metadaten;

import java.util.Collection;

import javax.faces.model.SelectItem;

import ugh.dl.MetadataType;

public class RenderableEdit extends RenderableMetadatum implements RenderableGroupableMetadatum {

	private String value;

	public RenderableEdit(MetadataType metadataType, RenderableMetadataGroup container) {
		super(container);
		super.labels = metadataType.getAllLanguages();
	}

	/**
	 * Throws UnsupportedOperationException because an edit component doesn’t
	 * have items.
	 * 
	 * @see de.sub.goobi.metadaten.RenderableGroupableMetadatum#getItems()
	 */
	@Override
	public Collection<SelectItem> getItems() {
		throw new UnsupportedOperationException();
	}

	/**
	 * Throws UnsupportedOperationException because an edit component doesn’t
	 * have items.
	 * 
	 * @see de.sub.goobi.metadaten.RenderableGroupableMetadatum#getSelectedItems()
	 */
	@Override
	public Collection<String> getSelectedItems() {
		throw new UnsupportedOperationException();
	}

	/**
	 * Returns the edit field value.
	 * 
	 * @see de.sub.goobi.metadaten.RenderableGroupableMetadatum#getValue()
	 */
	@Override
	public String getValue() {
		return value != null ? value : "";
	}

	/**
	 * Returns false because an edit component isn’t a multiselect component.
	 * 
	 * @see de.sub.goobi.metadaten.RenderableGroupableMetadatum#isMultiselect()
	 */
	@Override
	public boolean isMultiselect() {
		return false;
	}

	/**
	 * Throws UnsupportedOperationException because an edit component doesn’t
	 * have items.
	 * 
	 * @see de.sub.goobi.metadaten.RenderableGroupableMetadatum#setSelectedItems(java.util.Collection)
	 */
	@Override
	public void setSelectedItems(Collection<String> selectedItems) {
		throw new UnsupportedOperationException();
	}

	/**
	 * Saves the value entered by the user.
	 * 
	 * @see de.sub.goobi.metadaten.RenderableGroupableMetadatum#setValue(java.lang.String)
	 */
	@Override
	public void setValue(String value) {
		this.value = value;
	}
}
