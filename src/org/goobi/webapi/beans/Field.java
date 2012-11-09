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
