package de.sub.goobi.metadaten;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import javax.faces.model.SelectItem;

import org.apache.commons.lang.StringUtils;

import ugh.dl.MetadataType;

public class RenderableLineEdit extends RenderableMetadatum implements RenderableGroupableMetadatum {
	private static final String HTML_TEXTAREA_LINE_SEPARATOR = "\r\n";
	private List<String> value;

	public RenderableLineEdit(MetadataType metadataType, RenderableMetadataGroup container) {
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
		if (value != null) {
			return StringUtils.join(value, HTML_TEXTAREA_LINE_SEPARATOR);
		} else {
			return "";
		}
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
		this.value = Arrays.asList(value.split(HTML_TEXTAREA_LINE_SEPARATOR));
	}
}
