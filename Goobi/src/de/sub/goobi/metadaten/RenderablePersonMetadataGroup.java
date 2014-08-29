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
package de.sub.goobi.metadaten;

import java.util.Arrays;
import java.util.Collection;

import javax.faces.model.SelectItem;

import org.goobi.api.display.enums.BindState;

import ugh.dl.MetadataGroupType;
import ugh.dl.MetadataType;
import de.sub.goobi.helper.Helper;

public class RenderablePersonMetadataGroup extends RenderableMetadataGroup implements RenderableGroupableMetadatum {

	/**
	 * The enum Field holds the fields to show in a
	 * RenderablePersonMetadataGroup.
	 * 
	 * @author Matthias Ronge &lt;matthias.ronge@zeutschel.de&gt;
	 */
	enum Field {
		NORMDATA_RECORD("normDataRecord", true), FIRSTNAME("vorname", false), LASTNAME("nachname", false);

		private boolean isIdentifier;
		private String resourceKey;

		Field(String resourceKey, boolean isIdentifier) {
			this.resourceKey = resourceKey;
			this.isIdentifier = isIdentifier;
		}

		private String getResourceKey() {
			return resourceKey;
		}

		private boolean isIdentifier() {
			return isIdentifier;
		}
	};

	public RenderablePersonMetadataGroup(MetadataType metadataType, RenderableMetadataGroup renderableMetadataGroup,
			String projectName, BindState bindState) {
		super(Arrays.asList(new MetadataGroupType[] { getGroupTypeFor(metadataType) }), projectName, bindState);
		super.labels = metadataType.getAllLanguages();
	}

	private static final MetadataGroupType getGroupTypeFor(MetadataType type) {
		MetadataGroupType result = new MetadataGroupType();
		result.setName(type.getName());
		result.setAllLanguages(type.getAllLanguages());
		if (type.getNum() != null) {
			result.setNum(type.getNum());
		}
		for (Field field : Field.values()) {
			result.addMetadataType(getMetadataTypeFor(type, field));
		}
		return result;
	}

	private static final MetadataType getMetadataTypeFor(MetadataType type, Field field) {
		MetadataType result = new MetadataType();
		result.setName(type.getName() + '.' + field.toString());
		if (type.getNum() != null) {
			result.setNum(type.getNum());
		}
		result.setAllLanguages(Helper.getAllStrings(field.getResourceKey()));
		result.setIsPerson(false);
		result.setIdentifier(field.isIdentifier());
		return result;
	}

	@Override
	public Collection<SelectItem> getItems() {
		throw new UnsupportedOperationException();
	}

	@Override
	public Collection<String> getSelectedItems() {
		throw new UnsupportedOperationException();
	}

	@Override
	public String getValue() {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setSelectedItems(Collection<String> selectedItems) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setValue(String value) {
		throw new UnsupportedOperationException();
	}

}
