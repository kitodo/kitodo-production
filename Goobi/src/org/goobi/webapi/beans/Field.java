/*
 * This file is part of the Goobi Application - a Workflow tool for the support of
 * mass digitization.
 *
 * Visit the websites for more information.
 *     - http://gdz.sub.uni-goettingen.de
 *     - http://www.goobi.org
 *     - http://launchpad.net/goobi-production
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

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

import org.goobi.webapi.beans.Label.KeyAttribute;

import de.sub.goobi.beans.Projekt;
import de.sub.goobi.config.ConfigProjects;

@XmlType(propOrder = { "required", "from", "option", "ughbinding", "docstruct" })
public class Field {

	@XmlAttribute
	private String key;
	@XmlElement
	private boolean required;
	@XmlElement
	private List<Label> option;
	@XmlElement(name="source")
	private String from;
	@XmlElement
	private Boolean ughbinding;
	@XmlElement(name="insertionLevel")
	private String docstruct;

	/**
	 * Default constructor is required to be explicitly coded because copy
	 * constructor is given. Java only provides an implicit default constructor
	 * as long as no other constructors are given.
	 */
	public Field() {
		// there is nothing to do
	}

	/**
	 * Copy Constructor to instantiate an already populated Field. Copying is
	 * done that way that copies are created of the List and Boolean object—if
	 * present—so modifying one of them will *not* influence the one in the
	 * Field the copy was derived from. However, no copies are created of the
	 * list *entries*, so modifying a Label in the List *will* modify the equal
	 * Label in the List the copy was derived from.
	 * 
	 * @param toCopy
	 *            Field to create a copy from
	 */
	public Field(Field toCopy) {
		this.docstruct = toCopy.docstruct;
		this.from = toCopy.from;
		this.key = toCopy.key;
		this.option = toCopy.option != null ? new ArrayList<Label>(toCopy.option) : null;
		this.required = toCopy.required;
		this.ughbinding = toCopy.ughbinding != null ? new Boolean(toCopy.ughbinding) : null;
	}

	public static List<Field> getFieldConfigForProject(Projekt project) throws IOException {
		List<Field> fields = new ArrayList<Field>();

		ConfigProjects projectConfig = new ConfigProjects(project.getTitel());
		Integer numFields = projectConfig.getParamList("createNewProcess.itemlist.item").size();

		for (Integer field = 0; field < numFields; field++) {
			Field fieldConfig = new Field();
			String fieldRef = "createNewProcess.itemlist.item(" + field + ")";
			fieldConfig.key = projectConfig.getParamString(fieldRef);

			fieldConfig.from = projectConfig.getParamString(fieldRef + "[@from]");
			if (projectConfig.getParamBoolean(fieldRef + "[@ughbinding]")) {
				fieldConfig.ughbinding = Boolean.TRUE;
				fieldConfig.docstruct = projectConfig.getParamString(fieldRef + "[@docstruct]");
			} else {
				fieldConfig.ughbinding = Boolean.FALSE;
			}
			Integer selectEntries = projectConfig.getParamList(fieldRef + ".select").size();
			if (selectEntries > 0) {
				Map<String, String> selectConfig = new HashMap<String, String>();
				for (Integer selectEntry = 0; selectEntry < selectEntries; selectEntry++) {
					String key = projectConfig.getParamString(fieldRef + ".select(" + selectEntry + ")");
					String value = projectConfig.getParamString(fieldRef + ".select(" + selectEntry + ")[@label]");
					selectConfig.put(key, value);
				}
				fieldConfig.option = Label.toListOfLabels(selectConfig, KeyAttribute.LABEL);
			}
			fieldConfig.required = projectConfig.getParamBoolean(fieldRef + "[@required]");
			fields.add(fieldConfig);
		}
		return fields;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public void setRequired(boolean required) {
		this.required = required;
	}

	public void setOption(List<Label> option) {
		this.option = option;
	}

	public void setFrom(String from) {
		this.from = from;
	}

	public void setUghbinding(Boolean ughbinding) {
		this.ughbinding = ughbinding;
	}

	public void setDocstruct(String docstruct) {
		this.docstruct = docstruct;
	}

}
