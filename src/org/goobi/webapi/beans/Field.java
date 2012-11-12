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
 * PARTICULAR PURPOSE. See the GNU General Public License for more details. You
 * should have received a copy of the GNU General Public License along with this
 * program; if not, write to the Free Software Foundation, Inc., 59 Temple Place,
 * Suite 330, Boston, MA 02111-1307 USA
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

	@SuppressWarnings("unused")
	@XmlAttribute
	private String key;
	@SuppressWarnings("unused")
	@XmlElement
	private boolean required;
	@SuppressWarnings("unused")
	@XmlElement
	private List<Label> option;
	@SuppressWarnings("unused")
	@XmlElement(name="source")
	private String from;
	@SuppressWarnings("unused")
	@XmlElement
	private Boolean ughbinding;
	@SuppressWarnings("unused")
	@XmlElement(name="insertionLevel")
	private String docstruct;

	public static List<Field> getFieldConfigForProject(Projekt project) throws IOException {
		List<Field> fields = new ArrayList<Field>();

		ConfigProjects projectConfig = new ConfigProjects(project);
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
}
