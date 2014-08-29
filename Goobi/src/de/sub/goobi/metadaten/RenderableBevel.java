package de.sub.goobi.metadaten;

import java.util.Collection;

import javax.faces.model.SelectItem;

import ugh.dl.MetadataType;

public class RenderableBevel extends RenderableMetadatum implements RenderableGroupableMetadatum {

	private String value;

	public RenderableBevel(MetadataType metadataType, RenderableMetadataGroup container) {
		super(container);
		super.labels = metadataType.getAllLanguages();
	}

	/**
	 * Throws UnsupportedOperationException because a bevel doesn’t have items.
	 * 
	 * @see de.sub.goobi.metadaten.RenderableGroupableMetadatum#getItems()
	 */
	@Override
	public Collection<SelectItem> getItems() {
		throw new UnsupportedOperationException();
	}

	/**
	 * Throws UnsupportedOperationException because a bevel doesn’t have items.
	 * 
	 * @see de.sub.goobi.metadaten.RenderableGroupableMetadatum#getSelectedItems()
	 */
	@Override
	public Collection<String> getSelectedItems() {
		throw new UnsupportedOperationException();
	}

	/**
	 * Returns the bevel’s text.
	 * 
	 * @see de.sub.goobi.metadaten.RenderableGroupableMetadatum#getValue()
	 */
	@Override
	public String getValue() {
		return value != null ? value : "";
	}

	/**
	 * Throws UnsupportedOperationException because a bevel doesn’t have items.
	 * 
	 * @see de.sub.goobi.metadaten.RenderableGroupableMetadatum#setSelectedItems(java.util.Collection)
	 */
	@Override
	public void setSelectedItems(Collection<String> selectedItems) {
		throw new UnsupportedOperationException();
	}

	/**
	 * Throws UnsupportedOperationException because a bevel cannot be changed.
	 * 
	 * @see de.sub.goobi.metadaten.RenderableGroupableMetadatum#setValue(java.lang.String)
	 */
	@Override
	public void setValue(String value) {
		throw new UnsupportedOperationException();
	}
}
