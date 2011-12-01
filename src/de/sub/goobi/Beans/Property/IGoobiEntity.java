package de.sub.goobi.Beans.Property;

import java.util.List;

import org.goobi.production.api.property.xmlbasedprovider.Status;
import org.goobi.production.api.property.xmlbasedprovider.impl.PropertyTemplate;

/**
 * does nothing, but needed to generate properties for process, step, ....
 * 
 * @author rsehr
 * 
 */

public interface IGoobiEntity {

	/**
	 * needed to match {@link eigenschaften} with {@link PropertyTemplate} from xml
	 */
	public Status getStatus();

	/**
	 * 
	 * @return List of IGoobiProperties
	 */
	public List<IGoobiProperty> getProperties();

	/**
	 * adds a new property to list
	 * 
	 * @param toAdd
	 */
	public void addProperty(IGoobiProperty toAdd);

	/**
	 * remove property from list
	 * 
	 * @param toRemove
	 */
	public void removeProperty(IGoobiProperty toRemove);

	/**
	 * reloads properties from storage
	 */
	public void refreshProperties();

	/**
	 * 
	 * @return id of property
	 */
	public Integer getId();

}
