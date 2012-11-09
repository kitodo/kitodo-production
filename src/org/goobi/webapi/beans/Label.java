package org.goobi.webapi.beans;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlValue;

import com.sharkysoft.util.UnreachableCodeException;

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
}