/**
 * This file is part of the Goobi Application - a Workflow tool for the support
 * of mass digitization.
 * 
 * (c) 2014 Goobi. Digialisieren im Verein e.V. &lt;contact@goobi.org&gt;
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

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.configuration.ConfigurationException;

import ugh.dl.DocStruct;

/**
 * A MetadataPathSelector provides methods to retrieve or modify document
 * structure nodes on a document structure node.
 * 
 * @author Matthias Ronge &lt;matthias.ronge@zeutschel.de&gt;
 */
public class MetadataPathSelector extends MetadataSelector {
	/**
	 * The constant METADATA_SPLIT_PATH_SCHEME holds a regular expression used
	 * to extract the first metadata path segment.
	 */
	private static final Pattern METADATA_SPLIT_PATH_SCHEME = Pattern.compile("^" + METADATA_PATH_SEPARATOR + "([^"
			+ METADATA_PATH_SEPARATOR + METADATA_SEPARATOR + "]+?)");
	/**
	 * The constant SEGMENT_WITH_ELEMENT_SELELCTOR_SCHEME holds a regular
	 * expression used to detect and extract a quantifier expression at the end
	 * of the string.
	 */
	private static final Pattern SEGMENT_WITH_ELEMENT_SELELCTOR_SCHEME = Pattern.compile("(.*?)\\[(.+?)\\]");

	/**
	 * DocStructType name of the structure element to look for or create. "*"
	 * may be used to look up "any element", but will not work if an element
	 * needs to be constructed at this level.
	 */
	private final String docStructType;

	/**
	 * Integer of the element referenced, where Integer.MAX_VALUE indicates the
	 * "last" element, or null if none.
	 */
	private final Integer index;

	/**
	 * A metadata selector resolving the subsequent path
	 */
	private final MetadataSelector selector;

	/**
	 * Creates a new MetadataPathSelector.
	 * 
	 * @param selectNode
	 *            expression to select a node, must either be equal to a
	 *            metadata structure node type name or a Kleene star, or consist
	 *            of the former followed by an indexing expression in square
	 *            braces. The indexing expression may either be numeric or ">"
	 *            to refer to the last of several nodes.
	 * @param path
	 *            path to create sub-selector, passed to {
	 *            {@link #create(String)}.
	 * @throws ConfigurationException
	 */

	public MetadataPathSelector(String path) throws ConfigurationException {
		String pathSegment = matchCurrentPathSegment(path);
		Matcher pathSelectorHasElementSelector = SEGMENT_WITH_ELEMENT_SELELCTOR_SCHEME.matcher(pathSegment);
		if (pathSelectorHasElementSelector.matches()) {
			docStructType = pathSelectorHasElementSelector.group(1);
			String indexSymbol = pathSelectorHasElementSelector.group(2);
			try {
				index = getIndexValue(indexSymbol);
				if (index < 0) {
					throw new ConfigurationException("Negative element count is not allowed, in path: " + path);
				}
			} catch (NumberFormatException e) {
				throw new ConfigurationException("Cannot create metadata path selector: " + e.getMessage(), e);
			}
		} else {
			docStructType = pathSegment;
			index = null;
		}
		selector = super.create(path.substring(pathSegment.length() + 1));
	}

	/**
	 * The function getIndexValue() returns the numerical value represented by
	 * the symbolic (String) representation passed in. Since the method is
	 * called from the constructor it must not be overridden in subclasses.
	 * 
	 * @param indexSymbol
	 *            an integer value or ">" to refer to Integer.MAX_VALUE
	 * @return the integer value of the string, or Integer.MAX_VALUE for the
	 *         symbol ">".
	 */
	private final Integer getIndexValue(String indexSymbol) {
		try {
			return Integer.valueOf(indexSymbol);
		} catch (NumberFormatException cannotParseInt) {
			if (">".equals(indexSymbol)) {
				return Integer.MAX_VALUE;
			} else {
				throw cannotParseInt;
			}
		}
	}

	/**
	 * The function matchCurrentPathSegment() returns the path segment this
	 * metadata path selector is responsible to represent. Since the method is
	 * called from the constructor it must not be overridden in subclasses.
	 * 
	 * @param path
	 *            path expression to parse
	 * @return the path segment for this selector
	 * @throws ConfigurationException
	 *             if the path cannot be parsed
	 */
	private final String matchCurrentPathSegment(String path) throws ConfigurationException {
		Matcher metadataPathSplitter = METADATA_SPLIT_PATH_SCHEME.matcher(path);
		if (!metadataPathSplitter.find()) {
			throw new ConfigurationException(
					"Cannot create metadata path selector: Path must contain path segment, but is: " + path);
		}
		return metadataPathSplitter.group(1);
	}

	/**
	 * Returns the value of the metadatum named by the path used to construct
	 * the metadata selector, or null if either the path or the metadatum at the
	 * end of the path aren’t available. This works recursively, by calling
	 * itself on the subnode, if found, or returning null otherwise.
	 * 
	 * @see de.sub.goobi.metadaten.copier.MetadataSelector#findIn(ugh.dl.DocStruct)
	 */
	@Override
	protected String findIn(DocStruct supernode) {
		DocStruct subnode = getSubnode(supernode);
		if (subnode == null) {
			return null;
		} else {
			return selector.findIn(subnode);
		}
	}

	/**
	 * Returns the subnode identified by the path segment this metadata path
	 * selector is responsible for. Returns null if no such node can be found.
	 * 
	 * @param logicalNode
	 *            document structure node to retrieve the subnode from
	 * @return the subnode in question
	 * @throws RuntimeException
	 *             if there is more than one element matching but no index was
	 *             given to chose among them
	 */
	private DocStruct getSubnode(DocStruct logicalNode) {
		List<DocStruct> children = logicalNode.getAllChildrenByTypeAndMetadataType(docStructType, "*");
		switch (children.size()) {
		case 0:
			return null;
		case 1:
			if (index == null || index.equals(0) || index.equals(Integer.MAX_VALUE)) {
				return children.get(0);
			}
		default:
			if (index == null) {
				throw new RuntimeException("Could not resolve metadata path: Path selector is ambiguous for "
						+ docStructType);
			} else {
				if (index.equals(Long.MAX_VALUE)) {
					return children.get(children.size() - 1);
				}
				if (children.size() >= index) {
					return children.get(index);
				} else {
					return null;
				}
			}
		}

	}

	/**
	 * Creates a metadatum with the given value if the full path is applied and
	 * no such metadatum is already available under at the path. Leaves the
	 * document structure element unchanged if such a metadatum already exists.
	 * This works recursively, by calling itself on the subnode, if found, or
	 * returning null otherwise. Metadata creation is, by definition, always
	 * done in a {@link LocalMetadataSelector}.
	 * 
	 * @see de.sub.goobi.metadaten.DataSelector#findIn(ugh.dl.DocStruct)
	 */
	@Override
	public void createIfPathExistsOnly(CopierData data, DocStruct logicalNode, String value) {
		DocStruct subnode = getSubnode(logicalNode);
		if (subnode == null) {
			return;
		}
		selector.createIfPathExistsOnly(data, subnode, value);
	}
}
