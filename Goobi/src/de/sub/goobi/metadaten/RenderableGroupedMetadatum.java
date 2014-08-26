package de.sub.goobi.metadaten;

import java.util.Collection;

import javax.faces.model.SelectItem;

/**
 * A renderable metadatum which is part of a metadata group.
 * 
 * @author Matthias Ronge &lt;matthias.ronge@zeutschel.de&gt;
 */
public interface RenderableGroupedMetadatum {
	/**
	 * Shall return the label for the metadatum in the language previously set.
	 * 
	 * @return the label for the metadatum
	 */
	public String getLabel();

	/**
	 * Shall return the available items for the the user to choose from. May
	 * throw an UnsupportedOperationException if the implementing class doesn’t
	 * represent a select type of input.
	 * 
	 * @return the items to choose from
	 */
	public Collection<SelectItem> getItems();

	/**
	 * Shall return the selected items for multi select inputs. May throw an
	 * UnsupportedOperationException if the implementing class doesn’t represent
	 * a multi select input.
	 * 
	 * @return
	 */
	public Collection<String> getSelectedItems();

	/**
	 * Shall return the value, if any, or an empty String if empty. May throw an
	 * UnsupportedOperationException if the implementing class is a multi select
	 * input.
	 * 
	 * @return the value
	 */
	public String getValue();

	/**
	 * Shall return whether the implementing class represents a multi select
	 * input or not.
	 * 
	 * @return whether the implementing class represents a multi select input
	 */
	public boolean isMultiselect();

	/**
	 * Shall return whether the user shall be depredated the permission to edit
	 * the value(s) on the screen.
	 * 
	 * @return whether the component shall be read-only
	 */
	public boolean isReadonly();

	/**
	 * Sat the language in which labels shall be returned.
	 * 
	 * @param language
	 */
	public void setLanguage(String language);

	/**
	 * Shall save the items selected by the user. May throw an
	 * UnsupportedOperationException if the implementing class isn’t a multi
	 * select input.
	 * 
	 * @param selectedItems
	 *            the items selected by the user
	 */
	public void setSelectedItems(Collection<String> selectedItems);

	/**
	 * Shall save the value entered by the user. May throw an
	 * UnsupportedOperationException if the implementing class is a multi select
	 * input.
	 * 
	 * @param value
	 *            the value entered by the user
	 */
	public void setValue(String value);
}
