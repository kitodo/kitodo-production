/**
 * This file is part of the Goobi Application - a Workflow tool for the support
 * of mass digitization.
 *
 * (c) 2014 Goobi. Digitalisieren im Verein e.V. <contact@goobi.org>
 *
 * Visit the websites for more information.
 *     		- http://www.kitodo.org/en/
 *     		- https://github.com/goobi
 *
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.kitodo.production.plugin.CataloguePlugin.ModsPlugin;

/**
 * The class FindResult represents the result of a find() operation of the
 * plug-in. The Production plug-in API allows to return any object as result of
 * find() which is passed back to it if further operations on that search result
 * (i.e. getting a certain hit) are required. This class is used to store
 * anything we need for later actions on the hit list.
 *
 * @author Arved Solth, Christopher Timm
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
