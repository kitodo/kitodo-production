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
package de.sub.goobi.metadaten;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import ugh.dl.Metadata;
import ugh.dl.MetadataGroup;
import ugh.dl.MetadataType;

/**
 * Backing bean for a (multi-line) text input element to edit metadatum
 * renderable by JSF.
 * 
 * @author Matthias Ronge &lt;matthias.ronge@zeutschel.de&gt;
 */
public class RenderableLineEdit extends RenderableMetadatum implements RenderableGroupableMetadatum,
		SingleValueRenderableMetadatum {
	/**
	 * Line separator used in web front-end I/O
	 */
	private static final String HTML_TEXTAREA_LINE_SEPARATOR = "\r\n";

	/**
	 * Line separator used in filesystem I/O
	 */
	private static final String METADATA_LINE_SEPARATOR = "\n";

	/**
	 * Holds the content lines of the edit box.
	 */
	private List<String> value;

	/**
	 * Constructor. Creates a RenderableLineEdit.
	 * 
	 * @param metadataType
	 *            metadata type editable by this drop-down list
	 * @param binding
	 *            a metadata group whose corresponding metadata element shall be
	 *            updated if the setter method is called
	 * @param container
	 *            metadata group this drop-down list is showing in
	 */
	public RenderableLineEdit(MetadataType metadataType, MetadataGroup binding, RenderableMetadataGroup container) {
		super(metadataType, binding, container);
		if (binding != null) {
			for (Metadata data : binding.getMetadataByType(metadataType.getName())) {
				addContent(data);
			}
		}
	}

	/**
	 * Adds the data passed from the metadata element as content to the input.
	 * If there is data already (shouldn’t be, but however) it is appended for
	 * not being lost.
	 * 
	 * @param data
	 *            data to add
	 */
	@Override
	public void addContent(Metadata data) {
		if (value == null) {
			value = new ArrayList<String>(Arrays.asList(data.getValue().split(METADATA_LINE_SEPARATOR)));
		} else {
			value.addAll(Arrays.asList(data.getValue().split(METADATA_LINE_SEPARATOR)));
		}
	}

	/**
	 * Returns the edit field value.
	 * 
	 * @see de.sub.goobi.metadaten.SingleValueRenderableMetadatum#getValue()
	 */
	@Override
	public String getValue() {
		if (value != null) {
			return StringUtils.join(value, HTML_TEXTAREA_LINE_SEPARATOR);
		} else {
			return "";
		}
	}

	/**
	 * Saves the value entered by the user.
	 * 
	 * @see de.sub.goobi.metadaten.SingleValueRenderableMetadatum#setValue(java.lang.String)
	 */
	@Override
	public void setValue(String value) {
		this.value = Arrays.asList(value.split(HTML_TEXTAREA_LINE_SEPARATOR));
		updateBinding();
	}

	/**
	 * Returns the value of this edit component as metadata element
	 * 
	 * @return a list with one metadata element with the value of this component
	 * @see de.sub.goobi.metadaten.RenderableGroupableMetadatum#toMetadata()
	 */
	@Override
	public List<Metadata> toMetadata() {
		List<Metadata> result = new ArrayList<Metadata>(1);
		result.add(getMetadata(StringUtils.join(value, METADATA_LINE_SEPARATOR)));
		return result;
	}
}
