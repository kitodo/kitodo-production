package de.sub.goobi.metadaten;

import java.util.Collection;

import javax.faces.model.SelectItem;

/**
 * A renderable metadatum which is part of a metadata group.
 * 
 * @author Matthias Ronge &lt;matthias.ronge@zeutschel.de&gt;
 */
interface RenderableGroupedMetadatum {

	/**
	 * Shall return the label for the metadatum in the language previously set.
	 * 
	 * @return the label for the metadatum
	 */
	String getLabel();

	/**
	 * Shall return the available items for the the user to choose from. May
	 * throw an UnsupportedOperationException if the implementing class doesn’t
	 * represent a select type of input.
	 * 
	 * @return the items to choose from
	 */
	Collection<SelectItem> getItems();

	/**
	 * Shall return the selected items for multi select inputs. May throw an
	 * UnsupportedOperationException if the implementing class doesn’t represent
	 * a multi select input.
	 * 
	 * @return
	 */
	Collection<String> getSelectedItems();

	/**
	 * Shall return the value, if any, or an empty String if empty. May throw an
	 * UnsupportedOperationException if the implementing class is a multi select
	 * input.
	 * 
	 * @return the value
	 */
	String getValue();

	/**
	 * Shall return true if the element is contained in a group and is the first
	 * element in its members list, false otherwise.
	 * 
	 * @return if the element is the first in its list
	 */
	boolean isFirst();

	/**
	 * Shall return whether the implementing class represents a multi select
	 * input or not.
	 * 
	 * @return whether the implementing class represents a multi select input
	 */
	boolean isMultiselect();

	/**
	 * Shall return whether the user shall be depredated the permission to edit
	 * the value(s) on the screen.
	 * 
	 * @return whether the component shall be read-only
	 */
	boolean isReadonly();

	/**
	 * Shall save the items selected by the user. May throw an
	 * UnsupportedOperationException if the implementing class isn’t a multi
	 * select input.
	 * 
	 * @param selectedItems
	 *            the items selected by the user
	 */
	void setSelectedItems(Collection<String> selectedItems);

	/**
	 * Shall save the value entered by the user. May throw an
	 * UnsupportedOperationException if the implementing class is a multi select
	 * input.
	 * 
	 * @param value
	 *            the value entered by the user
	 */
	void setValue(String value);
}
