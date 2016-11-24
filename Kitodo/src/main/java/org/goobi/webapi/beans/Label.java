/*
 * This file is part of the Goobi Application - a Workflow tool for the support of
 * mass digitization.
 *
 * Visit the websites for more information.
 *     - http://gdz.sub.uni-goettingen.de
 *     - http://www.kitodo.org
 *     - https://github.com/goobi/goobi-production
 *
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.goobi.webapi.beans;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlValue;

import org.kitodo.production.exceptions.UnreachableCodeException;

/**
 * The Label class provides serialization for Map<String,String> objects where
 * keys are language identifiers (examples include “en”, “de”, …) and values are
 * texts in the respective language. This is necessary because Maps
 * unfortunately do not natively serialize to XML.
 * 
 * @author Matthias Ronge <matthias.ronge@zeutschel.de>
 */
public class Label {
	public enum KeyAttribute {
		LABEL, LANGUAGE
	}

	@XmlAttribute(name = "label")
	public String label;

	@XmlAttribute(name = "lang")
	public String language;

	@XmlValue
	public String value;

	/**
	 * Default constructor is required to be explicitly coded because copy
	 * constructor is given. Java only provides an implicit default constructor
	 * as long as no other constructors are given.
	 */
	public Label() {
		// there is nothing to do
	}

	/**
	 * Copy Constructor to instantiate an already populated Label.
	 * 
	 * @param toCopy
	 *            Field to create a copy from
	 */
	public Label(Label toCopy) {
		this.label = toCopy.label;
		this.language = toCopy.language;
		this.value = toCopy.value;
	}

	public static List<Label> toListOfLabels(Map<String, String> data, KeyAttribute keyAttribute) {
		List<Label> result = new ArrayList<Label>();
		for (String key : data.keySet()) {
			Label entry = new Label();
			switch (keyAttribute) {
				case LABEL:
					entry.label = key;
					break;
				case LANGUAGE:
					entry.language = key;
					break;
				default:
					throw new UnreachableCodeException();
			}
			entry.value = data.get(key);
			result.add(entry);
		}
		return result;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public void setLanguage(String language) {
		this.language = language;
	}

	public void setValue(String value) {
		this.value = value;
	}

}
