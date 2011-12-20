/*
 * This file is part of the Goobi Application - a Workflow tool for the support of
 * mass digitization.
 *
 * Visit the websites for more information.
 *     - http://gdz.sub.uni-goettingen.de
 *     - http://www.goobi.org
 *     - http://launchpad.net/goobi-production
 *
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU General Public License for more details. You
 * should have received a copy of the GNU General Public License along with this
 * program; if not, write to the Free Software Foundation, Inc., 59 Temple Place,
 * Suite 330, Boston, MA 02111-1307 USA
 */

package de.sub.goobi.beans.property;

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
