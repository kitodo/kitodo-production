package de.sub.goobi.metadaten;

import java.util.Collection;

import javax.faces.model.SelectItem;

/**
 * A RenderableGroupableMetadatum is a metadatum which can—but doesn’t have to
 * be—a member of a RenderableMetadataGroup. A RenderableGroupableMetadatum can be
 * a RenderablePersonMetadataGroup—which is a special case of a
 * RenderableMetadataGroup—but must not be a RenderableMetadataGroup.
 * 
 * Java interfaces are always public and this interface holds the public methods
 * that are accessed by JSF during rendering. Other methods with a more
 * restricted visibility cannot be defined here. They will be defined in the
 * abstract class {@link RenderableGroupableMetadatum}.
 * 
 * @author Matthias Ronge &lt;matthias.ronge@zeutschel.de&gt;
 */
interface RenderableGroupableMetadatum {

	/**
	 * Shall return the available items for the the user to choose from. May
	 * throw an UnsupportedOperationException if the implementing class doesn’t
	 * represent a select type of input.
	 * 
	 * @return the items to choose from
	 */
	Collection<SelectItem> getItems();

	/**
	 * Shall return the label for the metadatum in the language previously set.
	 * 
	 * @return the label for the metadatum
	 */
	String getLabel();

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
