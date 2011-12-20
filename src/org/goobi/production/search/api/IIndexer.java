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

package org.goobi.production.search.api;

import de.sub.goobi.beans.Prozess;

/**
 * This interface can be used to implement a search index engine for goobi. 
 * @author Robert Sehr
 */

public interface IIndexer {
	
	/**
	 * adds am Object to index
	 * @param p the process to add
	 */
	public void addObject(Prozess process);
	
	/**
	 * removes an Object from index
	 * @param p the process to remove
	 */
	public void removeObject(Prozess process);
	
	/**
	 * updates an Object in index
	 * @param p the process to update
	 */
	public void updateObject(Prozess process);

}
