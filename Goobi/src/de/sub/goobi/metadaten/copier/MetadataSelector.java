/**
 * This file is part of the Goobi Application - a Workflow tool for the support
 * of mass digitization.
 * 
 * Copyright (C) 2014 Goobi. Digitalisieren im Verein e.V. <contact@goobi.org>
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

import ugh.dl.DocStruct;

public abstract class MetadataSelector extends DataSelector {

	/**
	 * Factory method to create a metadata selector. Depending on the path, the
	 * required implementation will be constructed.
	 * 
	 * @param path
	 *            path to create a metadata selector from.
	 * @return a metadata selector instance representing the given paht
	 * @throws ConfigurationException
	 *             if the path cannot be evaluated
	 */
	public static MetadataSelector create(String path) throws ConfigurationException {

		if (path.startsWith(METADATA_SEPARATOR)) {
			return new LocalMetadataSelector(path);
		}

		if (path.startsWith(METADATA_PATH_SEPARATOR)) {
			if (path.indexOf(METADATA_SEPARATOR) == 1) {
				return new LocalMetadataSelector(path.substring(1));
			} else {
				return new MetadataPathSelector(path);
			}
		}
		throw new ConfigurationException(
				"Cannot create metadata selector: Path must start with \"@\" or \"/\", but is: " + path);
	}

	/**
	 * Returns the value of the metadatum named by the path used to construct
	 * the metadata selector, or null if either the path or the metadatum at the
	 * end of the path aren’t available.
	 * 
	 * @return the value the path points to, or null if absent
	 * @see de.sub.goobi.metadaten.copier.DataSelector#findIn(de.sub.goobi.metadaten.copier.CopierData)
	 */
	@Override
	public String findIn(CopierData data) {
		return findIn(data.getLogicalDocStruct());
	}

	/**
	 * Calling findIn() on the implementing instance should return the value of
	 * the metadatum named by the path used to construct the metadata selector.
	 * Should return null if either the path or the metadatum at the end of the
	 * path aren’t available.
	 * 
	 * @param logicalNode
	 *            document structure node to examine, intended for recursion
	 * @return the value the path points to, or null if absent
	 */
	protected abstract String findIn(DocStruct logicalNode);

	/**
	 * Checks if the document structure node the metadata selector is pointing
	 * at is available, but no metadatum as named by the path is available at
	 * that document structure node, and only in this case adds a metadatum as
	 * named by the path with the value passed to the function.
	 * 
	 * @param data
	 *            data to work on
	 * @param value
	 *            value to write if no metadatum is available at the path’s end
	 * @throws RuntimeException
	 *             if the operation fails for unfulfilled dependencies
	 */
	public void createIfPathExistsOnly(CopierData data, String value) {
		createIfPathExistsOnly(data, data.getLogicalDocStruct(), value);
	}

	/**
	 * Calling createIfPathExistsOnly() on the implementing instance should
	 * check if the document structure node the metadata selector is pointing at
	 * is available, but no metadatum as named by the path is available at that
	 * document structure node, and only in this case add a metadatum as named
	 * by the path with the value passed to the function.
	 * 
	 * @param data
	 *            data to work on
	 * @param logicalNode
	 *            document structure node to start from, intended for recursion
	 * @param value
	 *            value to write if no metadatum is available at the path’s end
	 * @throws RuntimeException
	 *             if the operation fails for unfulfilled dependencies
	 */
	protected abstract void createIfPathExistsOnly(CopierData data, DocStruct logicalNode, String value);

}
