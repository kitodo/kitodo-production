//CHECKSTYLE:OFF
/**
 * This file is part of the Goobi Application - a Workflow tool for the support
 * of mass digitization.
 * 
 * (c) 2014 Goobi. Digitalisieren im Verein e. V. <contact@goobi.org>
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
//CHECKSTYLE:ON

package de.sub.goobi.metadaten.copier;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.configuration.ConfigurationException;

import ugh.dl.DocStruct;

/**
 * A DestinationReferenceSelector provides methods to retrieve document structure nodes relative to the respective
 * document structure that the result of the operation shall be written to for reading from them.
 *
 * @author Matthias Ronge &lt;matthias.ronge@zeutschel.de&gt;
 */
public class DestinationReferenceSelector extends DataSelector {

	/**
	 * Regular expression pattern to parse the path string
	 */
	Pattern DESTINATION_REFERENCE_SELECTOR_SCHEME = Pattern.compile(Pattern.quote(RESPECTIVE_DESTINATION_REFERENCE)
			+ "(\\d+)([" + METADATA_PATH_SEPARATOR + METADATA_SEPARATOR + "].*)");

	/**
	 * Hierarchical level to retrieve (0 references the top level)
	 */
	private final int index;

	/**
	 * A further selector to read data relative to the resolved result of this selector.
	 */
	private final MetadataSelector nextSelector;

	/**
	 * Creates a new DestinationReferenceSelector.
	 *
	 * @param path reference to resolve
	 * @throws ConfigurationException if the path is syntactically wrong
	 */
	public DestinationReferenceSelector(String path) throws ConfigurationException {
		Matcher pathSplitter = DESTINATION_REFERENCE_SELECTOR_SCHEME.matcher(path);
		if (!pathSplitter.find()) {
			throw new ConfigurationException("Invalid destination reference selector: " + path);
		}
		this.index = Integer.parseInt(pathSplitter.group(1));
		this.nextSelector = MetadataSelector.create(pathSplitter.group(2));
	}

	/**
	 * Returns the document structure level indicated by the index form the respective destination path.
	 *
	 * @see de.sub.goobi.metadaten.copier.DataSelector#findIn(de.sub.goobi.metadaten.copier.CopierData)
	 */
	@Override
	public String findIn(CopierData data) {
		DocStruct currentLevel = data.getLogicalDocStruct();
		MetadataSelector destination = data.getDestination();
		for (int descend = index; descend > 0; descend--) {
			if (!(destination instanceof MetadataPathSelector)) {
				return null;
			}
			int childReference = ((MetadataPathSelector) destination).getIndex();
			currentLevel = currentLevel.getAllChildren().get(childReference);
			destination = ((MetadataPathSelector) destination).getSelector();
		}
		return nextSelector.findIn(currentLevel);
	}

}
