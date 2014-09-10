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
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
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

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.log4j.Logger;

import ugh.dl.DocStruct;
import ugh.dl.Metadata;
import ugh.dl.MetadataType;
import ugh.exceptions.MetadataTypeNotAllowedException;

/**
 * A LocalMetadataSelector provides methods to retrieve or modify metadata on a
 * document structure node.
 * 
 * @author Matthias Ronge &lt;matthias.ronge@zeutschel.de&gt;
 */
public class LocalMetadataSelector extends MetadataSelector {
	private static final Logger LOG = Logger.getLogger(LocalMetadataSelector.class);

	/**
	 * Metadata type to return
	 */
	private final MetadataType selector = new MetadataType();

	/**
	 * Creates a new LocalMetadataSelector.
	 * 
	 * @param path
	 *            Path to metadatum, must consist of an “@” character followed
	 *            by a metadata element name, i.e. “@TitleDocMain”
	 * @throws ConfigurationException
	 *             if the metadata path doesn’t start with an “@” character
	 */
	public LocalMetadataSelector(String path) throws ConfigurationException {
		if (!path.startsWith(METADATA_SEPARATOR)) {
			throw new ConfigurationException(
					"Cannot create local metadata selector: Path must start with \"@\", but is: " + path);
		}
		selector.setName(path.substring(1));
	}

	/**
	 * Checks if no metadatum as named by the path is available at that document
	 * structure node, and only in this case adds a metadatum as named by the
	 * path with the value passed to the function.
	 * 
	 * @param data
	 *            document to work on, required to access the rule set
	 * @param logicalNode
	 *            document structure node to check and enrich
	 * @param value
	 *            value to write if no metadatum of this type is available
	 * @see de.sub.goobi.metadaten.copier.MetadataSelector#createIfPathExistsOnly(de.sub.goobi.metadaten.copier.CopierData,
	 *      ugh.dl.DocStruct, java.lang.String)
	 */
	@Override
	protected void createIfPathExistsOnly(CopierData data, DocStruct logicalNode, String value) {
		if (findMetadatumIn(logicalNode) != null) {
			return;
		}
		tryToCreateANewMetadatum(data, logicalNode, value);
	}

	/**
	 * Sets the value of the metadatum described by the path to the value passed
	 * to the function or creates a metadatum as described by the path,
	 * respectively.
	 * 
	 * @param data
	 *            document to work on, required to access the rule set
	 * @param logicalNode
	 *            document structure node to check and enrich
	 * @param value
	 *            value to write if no metadatum of this type is available
	 * @see de.sub.goobi.metadaten.copier.MetadataSelector#createIfPathExistsOnly(de.sub.goobi.metadaten.copier.CopierData,
	 *      ugh.dl.DocStruct, java.lang.String)
	 */

	@Override
	protected void createOrOverwrite(CopierData data, DocStruct logicalNode, String value) {
		Metadata existingMetadatum = findMetadatumIn(logicalNode);
		if (existingMetadatum != null) {
			existingMetadatum.setValue(value);
		} else {
			tryToCreateANewMetadatum(data, logicalNode, value);
		}
	}

	/**
	 * Returns all concrete metadata selectors the potentially generic metadata
	 * selector expression resolves to.
	 * 
	 * @param node
	 *            Node of the logical document structure to work on
	 * @return all metadata selectors the expression resolves to
	 * @see de.sub.goobi.metadaten.copier.MetadataSelector#findAll(ugh.dl.DocStruct)
	 */
	@Override
	protected Iterable<MetadataSelector> findAll(DocStruct node) {
		ArrayList<MetadataSelector> result = new ArrayList<MetadataSelector>(1);
		List<MetadataType> addableTypes = node.getAddableMetadataTypes();
		if (addableTypes != null) {
			for (MetadataType addable : addableTypes) {
				if (selector.getName().equals(addable.getName())) {
					result.add(this);
					break;
				}
			}
		}
		return result;
	}

	/**
	 * Return the value of the metadatum named by the path used to construct the
	 * metadata selector, or null if no such metadatum is available here.
	 * 
	 * @param node
	 *            document structure node to examine
	 * @return the value of the metadatum, or null if absent
	 * @see de.sub.goobi.metadaten.copier.MetadataSelector#findIn(ugh.dl.DocStruct)
	 */
	@Override
	protected String findIn(DocStruct node) {
		Metadata found = findMetadatumIn(node);
		return found != null ? found.getValue() : null;
	}

	/**
	 * Returns the metadatum named by the path used to construct the metadata
	 * selector, or null if no such metadatum is available here.
	 * 
	 * @param node
	 *            document structure node to examine
	 * @return the metadatum, or null if absent
	 * @see de.sub.goobi.metadaten.copier.MetadataSelector#findIn(ugh.dl.DocStruct)
	 */
	private Metadata findMetadatumIn(DocStruct node) {
		List<? extends Metadata> metadata = node.getAllMetadataByType(selector);
		for (Metadata metadatum : metadata) {
			if (selector.getName().equals(metadatum.getType().getName())) {
				return metadatum;
			}
		}
		return null;
	}

	/**
	 * Returns a string that textually represents this LocalMetadataSelector.
	 * 
	 * @return a string representation of this LocalMetadataSelector
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return METADATA_SEPARATOR + selector.getName();
	}

	/**
	 * Adds a metadatum as named by the path with the value passed to the
	 * function. Doesn’t do anything if that isn’t possible.
	 * 
	 * @param data
	 *            document to work on, required to access the rule set
	 * @param logicalNode
	 *            document structure node to check and enrich
	 * @param value
	 *            value to write if no metadatum of this type is available
	 */
	private void tryToCreateANewMetadatum(CopierData data, DocStruct logicalNode, String value) {
		Metadata copy = null;
		try {
			copy = new Metadata(data.getPreferences().getMetadataTypeByName(selector.getName()));
		} catch (MetadataTypeNotAllowedException e) {
			// copy rules aren’t related to the rule set but depend on it, so
			// copy rules that don’t work with the current rule set are ignored
			LOG.debug("Cannot create metadata element " + selector.getName()
					+ ": The type isn’t defined by the rule set used.");
			return;
		} catch (Exception e) {
			// copy rule failed, skip it
			LOG.debug("Cannot create metadata element " + selector.getName()
					+ ": Accessing the rule set failed with exception: "
					+ (e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName()), e);
			return;
		}
		try {
			copy.setValue(value);
			logicalNode.addMetadata(copy);
		} catch (MetadataTypeNotAllowedException e) {
			// copy rules aren’t related to the rule set but depend on it, so
			// copy rules that don’t work with the current rule set are ignored
			LOG.debug("Cannot assign metadata element " + selector.getName() + " (\"" + value
					+ "\") to structural element "
					+ (logicalNode.getType() != null ? logicalNode.getType().getName() : "without type") + ": "
					+ e.getMessage());
			return;
		}
	}
}
