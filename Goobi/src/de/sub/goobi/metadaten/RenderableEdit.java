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
package de.sub.goobi.metadaten;

import java.util.ArrayList;
import java.util.List;

import ugh.dl.Metadata;
import ugh.dl.MetadataGroup;
import ugh.dl.MetadataType;

/**
 * A RenderableEdit is a backing bean for a single-line text input element to
 * edit a metadatum renderable by JSF.
 * 
 * @author Matthias Ronge &lt;matthias.ronge@zeutschel.de&gt;
 */
public class RenderableEdit extends RenderableMetadatum implements RenderableGroupableMetadatum,
		SingleValueRenderableMetadatum {

	private String value;

	/**
	 * Constructor. Creates a RenderableEdit.
	 * 
	 * @param metadataType
	 *            metadata type editable by this drop-down list
	 * @param container
	 *            metadata group this drop-down list is showing in
	 */
	public RenderableEdit(MetadataType metadataType, MetadataGroup binding, RenderableMetadataGroup container) {
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
		if (value == null || value.length() == 0) {
			value = data.getValue();
		} else {
			value += "; " + data.getValue();
		}
	}

	/**
	 * Returns the edit field value.
	 * 
	 * @return the value from or for the edit field
	 * 
	 * @see de.sub.goobi.metadaten.SingleValueRenderableMetadatum#getValue()
	 */
	@Override
	public String getValue() {
		return value != null ? value : "";
	}

	/**
	 * Sets the value or saves the value entered by the user.
	 * 
	 * @param value
	 *            value to set
	 * 
	 * @see de.sub.goobi.metadaten.SingleValueRenderableMetadatum#setValue(java.lang.String)
	 */
	@Override
	public void setValue(String value) {
		this.value = value;

		if (binding != null) {
			List<Metadata> bound = binding.getMetadataList();
			bound.removeAll(binding.getMetadataByType(metadataType.getName()));
			bound.addAll(toMetadata());
		}
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
		result.add(getMetadata(value));
		return result;
	}
}
