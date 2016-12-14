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

package org.kitodo.production.plugin.opac.pica;

/**
 * The class FindResult represents the result of a find() operation of the
 * plug-in. The Production plug-in API allows to return any object as result of
 * find() which is passed back to it if further operations on that search result
 * (i.e. getting a certain hit) are required. This class is used to store
 * anything we need for later actions on the hit list.
 * 
 * @author Matthias Ronge &lt;matthias.ronge@zeutschel.de&gt;
 */
class FindResult {
	private final Query query;
	private final long hits;

	FindResult(Query query, long hits) {
		this.query = query;
		this.hits = hits;
	}

	long getHits() {
		return hits;
	}

	Query getQuery() {
		return query;
	}

	/**
	 * The classes Catalogue, ConfigOpacCatalogue, GetOpac and Query do not
	 * implement hashCode()—implementing hashCode() here would not work
	 * correctly.
	 * 
	 * @throws UnsupportedOperationException
	 *             if trying to invoke hashCode()
	 */
	@Override
	public int hashCode() {
		throw new UnsupportedOperationException(
				"The classes Catalogue, ConfigOpacCatalogue, GetOpac and Query do not implement hashCode()—implementing hashCode() here would not work correctly");
	}

	/**
	 * The classes Catalogue, ConfigOpacCatalogue, GetOpac and Query do not
	 * implement equals()—implementing equals() here would not work correctly.
	 * 
	 * @throws UnsupportedOperationException
	 *             if trying to invoke equals()
	 */
	@Override
	public boolean equals(Object obj) {
		throw new UnsupportedOperationException(
				"The classes Catalogue, ConfigOpacCatalogue, GetOpac and Query do not implement equals()—implementing equals() here would not work correctly");
	}
}
