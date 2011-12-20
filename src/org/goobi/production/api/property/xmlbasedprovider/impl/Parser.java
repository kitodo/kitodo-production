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

package org.goobi.production.api.property.xmlbasedprovider.impl;

import java.io.IOException;
import java.util.ArrayList;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.Namespace;
import org.jdom.input.SAXBuilder;

import de.sub.goobi.beans.property.IGoobiEntity;
import de.sub.goobi.helper.enums.PropertyType;

/******************************************************************************
 * Parser
 * 
 * 
 * @author Igor Toker
 * 
 *******************************************************************************/
public class Parser {
	// Strings used in xml
	private static final String PROPERTY = "property";
	private static final String NAME = "name";
	private static final String REQUIRED = "required";
	private static final String TYPE = "type";
	private static final String VALUE = "value";
	private static final String SELECTED = "selected";
	private static final String ENTITY = "entity";
	private static final String CONTAINER = "container";

	private Namespace ns = Namespace.getNamespace("tns", "http://www.goobi.org/propertyTemplates");

	/*************************************************************************
	 * Creates Properties from XML-File
	 * 
	 * @param filename
	 *            String
	 * @param validate
	 *            boolean. Validation is not implemented!
	 * @return
	 * @throws JDOMException
	 * @throws IOException
	 ************************************************************************/
	@SuppressWarnings("unchecked")
	public ArrayList<PropertyTemplate> createModelFromXML(String filename, boolean validate, IGoobiEntity inEntity) throws JDOMException, IOException {

		validate = false;
		SAXBuilder builder = new SAXBuilder(validate);
		Document doc;
		doc = builder.build(filename);
		Element rootElement = doc.getRootElement();

		ArrayList<Element> ePropertyList = new ArrayList<Element>();
		ePropertyList.addAll(rootElement.getChildren(PROPERTY, ns));

		ArrayList<PropertyTemplate> propList = new ArrayList<PropertyTemplate>();
		for (Element eProperty : ePropertyList) {
			PropertyTemplate property = generateProperty(eProperty, inEntity);
			if (property != null) {
				propList.add(property);
			}
		}

		return propList;
	}

	/***********************************************************************
	 * This Method generate {@link PropertyTemplate} from Element
	 * 
	 * @param element
	 * @return
	 ************************************************************************/
	@SuppressWarnings("unchecked")
	private PropertyTemplate generateProperty(Element element,IGoobiEntity inEntity) {
		PropertyTemplate property = new PropertyTemplate(inEntity);
		// Name
		String name = element.getAttributeValue(NAME);
		property.setName(name);
		// Position
		// if (element.getAttribute(POSITION) != null) {
		// property.setPosition(Integer.valueOf(element
		// .getAttributeValue(POSITION)));
		// }
		// Required
		if (element.getAttribute(REQUIRED) != null) {
			property.setRequired(Boolean.valueOf(element.getAttributeValue(REQUIRED)));
		}
		// Container
		if (element.getAttribute(CONTAINER) != null) {
			try {
				property.setContainer(new Integer(element.getAttributeValue(CONTAINER)));
			} catch (Exception e) {
				property.setContainer(0);
			}
		} else {
			property.setContainer(0);
		}
		// Type
		if (element.getChild(TYPE, ns) != null) {
			PropertyType type = getPropertyTypeFromString(element.getChild(TYPE, ns).getValue());
			if (type != null) {
				property.setType(type);
			}
		}
		// Values
		ArrayList<Element> eValues = new ArrayList<Element>(element.getChildren(VALUE, ns));
		if (eValues != null & eValues.size() > 0) {
			// �berprufen ob die Werte zu dem Typ passen!
			for (Element eValue : eValues) {
				String value = eValue.getAttributeValue(VALUE);
				property.getValuesList().add(value);
				// wenn es sich um eine liste handelt, �berpr�fen, welcher Wert
				// ist selected
				if (property.getType() == PropertyType.List) {
					if (eValue.getAttribute(SELECTED) != null) {
						if (eValue.getAttributeValue(SELECTED).equals("true")) {
							property.setSelectedValue(value);
						}
					}
				}
			}
		}

		// Entities
		ArrayList<Element> eEntities = new ArrayList<Element>(element.getChildren(ENTITY, ns));
		for (Element eEntity : eEntities) {
			String entityName = eEntity.getAttributeValue(NAME);
			String type = eEntity.getAttributeValue(TYPE);
			if (entityName == null)
				entityName = "";
			if (type != null && type.length() > 0) {
				property.getEntities().put(type, entityName);
			}
		}
		return property;
	}

	/*************************************************************************
	 * Generates {@link Type} from string
	 * 
	 * @param string
	 *            String
	 * @return {@link Type}
	 ************************************************************************/
	private PropertyType getPropertyTypeFromString(String string) {
		if (string.equals(PropertyType.String.toString())) {
			return PropertyType.String;
		} else if (string.equals(PropertyType.Boolean.toString())) {
			return PropertyType.Boolean;
		} else if (string.equals(PropertyType.Number.toString())) {
			return PropertyType.Number;
		} else if (string.equals(PropertyType.List.toString())) {
			return PropertyType.List;
		} else if (string.equals(PropertyType.ListMultiSelect.toString())) {
			return PropertyType.ListMultiSelect;
		} else if (string.equals(PropertyType.Date.toString())) {
			return PropertyType.Date;
		}
		return null;
	}

}
