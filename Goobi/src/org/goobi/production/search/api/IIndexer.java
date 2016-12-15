/*
 * (c) Kitodo. Key to digital objects e. V. <contact@kitodo.org>
 *
 * This file is part of the Kitodo project.
 *
 * It is licensed under GNU General Public License version 3 or later.
 *
 * For the full copyright and license information, please read the
 * GPL3-License.txt file that was distributed with this source code.
 */

package org.goobi.production.search.api;

import de.sub.goobi.beans.Prozess;


/**
 * This interface can be used to implement a search index engine for goobi.
 * @author Robert Sehr
 */
@Deprecated
public interface IIndexer {

	/**
	 * adds an object to index
	 * @param process the process to add
	 */
	public void addObject(Prozess process);

	/**
	 * removes an object from index
	 * @param process the process to remove
	 */
	public void removeObject(Prozess process);

	/**
	 * updates an object in index
	 * @param process the process to update
	 */
	public void updateObject(Prozess process);

}
