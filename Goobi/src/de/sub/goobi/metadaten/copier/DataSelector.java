/**
 * This file is part of the Goobi Application - a Workflow tool for the support
 * of mass digitization.
 * 
 * (c) 2014 Goobi. Digitalisieren im Verein e.V. &lt;contact@goobi.org&gt;
 * 
 * Visit the websites for more information.
 *     		- http://www.goobi.org/en/
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
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 59 Temple
 * Place, Suite 330, Boston, MA 02111-1307 USA
 * 
 * Linking this library statically or dynamically with other modules is making a
 * combined work based on this library. Thus, the terms and conditions of the
 * GNU General Public License cover the whole combination. As a special
 * exception, the copyright holders of this library give you permission to link
 * this library with independent modules to produce an executable, regardless of
 * the license terms of these independent modules, and to copy and distribute
 * the resulting executable under terms of your choice, provided that you also
 * meet, for each linked independent module, the terms and conditions of the
 * license of that module. An independent module is a module which is not
 * derived from or based on this library. If you modify this library, you may
 * extend this exception to your version of the library, but you are not obliged
 * to do so. If you do not wish to do so, delete this exception statement from
 * your version.
 */
package de.sub.goobi.metadaten.copier;

import org.apache.commons.configuration.ConfigurationException;

/**
 * A DataSelector is useful to access a data object. There are different
 * DataSelectors available to access metadata and program variables.
 * 
 * The factory method {{@link #create(String)} can be used to retrieve a
 * DataSelector instance for a given path.
 * 
 * @author Matthias Ronge &lt;matthias.ronge@zeutschel.de&gt;
 */
public abstract class DataSelector {
	/**
	 * Symbol indicating that the element of several to choose shall be the last
	 * one
	 */
	protected final static String LAST_CHILD_QUANTIFIER = ">";

	/**
	 * Symbol indicating that the next segment of the path is a document
	 * structure hierarchy level
	 */
	protected final static String METADATA_PATH_SEPARATOR = "/";

	/**
	 * Symbol indicating that the next segment of the path is a metadatum
	 */
	protected final static String METADATA_SEPARATOR = "@";

	/**
	 * Symbol indicating that the next segment of the path is a reference to the
	 * node of the logical document structure that the metadata will be written
	 * to.
	 */
	protected static final String RESPECTIVE_DESTINATION_REFERENCE = "#";

	/**
	 * Symbol indicating that the value is a static string
	 */
	private static final String STRING_MARK = "\"";

	/**
	 * Symbol indicating that the selector is to select a variable
	 */
	protected final static String VARIABLE_REFERENCE = "$";

	/**
	 * Factory method to create a DataSelector.
	 * 
	 * @param path
	 *            path to the data object to access
	 * @return a subclass implementing the data selector required for the given
	 *         path
	 * @throws ConfigurationException
	 *             if the path cannot be evaluated
	 */
	public static DataSelector create(String path) throws ConfigurationException {
		if (path.startsWith(METADATA_PATH_SEPARATOR) || path.startsWith(METADATA_SEPARATOR)) {
			return MetadataSelector.create(path);
		}
		if (path.startsWith(VARIABLE_REFERENCE)) {
			return new VariableSelector(path);
		}
		if (path.startsWith(STRING_MARK)) {
			return new StringSelector(path);
		}
		if (path.startsWith(RESPECTIVE_DESTINATION_REFERENCE)) {
			return new DestinationReferenceSelector(path);
		}
		throw new ConfigurationException(
				"Cannot create data selector: Path must start with \"@\", \"/\" or \"$\", but is: " + path);
	}

	/**
	 * Calling findIn() on the implementing instance should return the value of
	 * the metadatum named by the path used to construct the metadata selector.
	 * Should return null if either the path or the metadatum at the end of the
	 * path aren’t available.
	 * 
	 * @param data
	 *            data collection to locate the metadatum in
	 * @return the value the path points to, or null if absent
	 * @throws RuntimeException
	 *             if the path cannot be resolved
	 */
	public abstract String findIn(CopierData data);
}
