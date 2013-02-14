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
import java.util.List;

import org.goobi.production.api.property.xmlbasedprovider.IPropertyTemplatesProvider;
import org.goobi.production.api.property.xmlbasedprovider.Status;
import org.jdom.JDOMException;

import de.sub.goobi.beans.property.IGoobiEntity;
import de.sub.goobi.config.ConfigMain;

/**************************************************************************************************
 * This Template Provider parse XML-File and generates PropertyTemplates
 * 
 * @author Igor Toker
 * 
 ***************************************************************************************************/
public class XMLBasedPropertyTemplateProvider implements IPropertyTemplatesProvider {

	private static final String PROJECT = "project";
	private static final String PROCESS = "process";
	private static final String STEP = "step";
	private static final String PRODUCT = "product";
	private static final String PRODUCTIONRESOURCE = "productionresource";

	private static XMLBasedPropertyTemplateProvider instance = null;
	// TODO replace it with real one
	private String filename = ConfigMain.getParameter("KonfigurationVerzeichnis") + "propertyTemplates.xml";
	private ArrayList<PropertyTemplate> propertyList;
	private IGoobiEntity entity;

	

	/****************************************************************************
	 * Singleton implementation
	 * 
	 * @return {@link XMLBasedPropertyTemplateProvider}
	 * @throws IOException
	 * @throws JDOMException
	 ***************************************************************************/
	public static XMLBasedPropertyTemplateProvider getInstance(IGoobiEntity inEntity) throws JDOMException, IOException {
		instance = new XMLBasedPropertyTemplateProvider(inEntity);

		return instance;
	}

	/**************************************************************************
	 * This Constructor loads XML File in Model. If the XML-File too large, we have to search other solution.
	 * 
	 * @throws IOException
	 * @throws JDOMException
	 **************************************************************************/
	private XMLBasedPropertyTemplateProvider(IGoobiEntity inEntity) throws JDOMException, IOException {
		Parser parser = new Parser();
		entity = inEntity;
		propertyList = parser.createModelFromXML(getFilepath(), false, inEntity);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.goobi.production.api.property.xmlbasedprovider.IProvidePropertyTemplates #getTemplates(org.goobi.production.api.property.Status)
	 */
	public List<PropertyTemplate> getTemplates(Status status, IGoobiEntity inEntity) {
		if (propertyList == null) {
			return null;
		}
		List<PropertyTemplate> tempList = filterPropertyList(status);
		List<PropertyTemplate> returnList = new ArrayList<PropertyTemplate>();
		for (PropertyTemplate pt : tempList) {

			PropertyTemplate bla = new PropertyTemplate(inEntity, pt.getName());
			bla.setAuswahl(pt.getAuswahl());
			bla.setCreationDate(pt.getCreationDate());
			bla.setDate(pt.getDate());
			bla.setRequired(pt.isRequired());
			bla.setSelectedValue(pt.getSelectedValue());
			bla.setSelectedValuesList(pt.getSelectedValuesList());
			bla.setType(pt.getType());
			bla.setValue(pt.getValue());
			bla.setValuesList(pt.getValuesList());
			bla.setWert(pt.getWert());
			bla.setContainer(pt.getContainer());
			returnList.add(bla);
		}
		
		
		sortPropertyList();

		return returnList;

	}

	/*******************************************************************************
	 * This method sort the property List with the position argument of property
	 ********************************************************************************/
	private void sortPropertyList() {
		// TODO Implementieren
	}

	/***************************************************************************
	 * This Method compares the status with the property. All Properties with the wrong status will be removed from the
	 * {@link XMLBasedPropertyTemplateProvider#propertyList}
	 * 
	 * @param status
	 *            Status
	 **************************************************************************/
	private List<PropertyTemplate> filterPropertyList(Status status) {

		List<PropertyTemplate> toAdd = new ArrayList<PropertyTemplate>();

		for (PropertyTemplate property : propertyList) {

			// Werkstueck
			if (status.getProduct() != null) {
				if (checkEntity(status.getProject(), property.getEntities().get(PROJECT), property)
						&& checkEntity(status.getProcess(), property.getEntities().get(PROCESS), property)
						&& !checkEntity(status.getStep(), property.getEntities().get(STEP), property)
						&& checkEntity(status.getProduct(), property.getEntities().get(PRODUCT), property)
						&& !checkEntity(status.getProductionResource(), property.getEntities().get(PRODUCTIONRESOURCE), property)) {
					toAdd.add(property);
				}

			}

			// Vorlage
			else if (status.getProductionResource() != null) {
				if (checkEntity(status.getProject(), property.getEntities().get(PROJECT), property)
						&& checkEntity(status.getProcess(), property.getEntities().get(PROCESS), property)
						&& !checkEntity(status.getStep(), property.getEntities().get(STEP), property)
						&& !checkEntity(status.getProduct(), property.getEntities().get(PRODUCT), property)
						&& checkEntity(status.getProductionResource(), property.getEntities().get(PRODUCTIONRESOURCE), property)) {
					toAdd.add(property);
				}

			}

			// Schritt
			else if (status.getStep() != null) {
				if (checkEntity(status.getProject(), property.getEntities().get(PROJECT), property)
						&& checkEntity(status.getProcess(), property.getEntities().get(PROCESS), property)
						&& checkEntity(status.getStep(), property.getEntities().get(STEP), property)
						&& !checkEntity(status.getProduct(), property.getEntities().get(PRODUCT), property)
						&& !checkEntity(status.getProductionResource(), property.getEntities().get(PRODUCTIONRESOURCE), property)) {
					toAdd.add(property);
				}
			}

			// Prozess
			else if (status.getProcess() != null) {
				if (checkEntity(status.getProject(), property.getEntities().get(PROJECT), property)
						&& checkEntity(status.getProcess(), property.getEntities().get(PROCESS), property)
						&& !checkEntity(status.getStep(), property.getEntities().get(STEP), property)
						&& !checkEntity(status.getProduct(), property.getEntities().get(PRODUCT), property)
						&& !checkEntity(status.getProductionResource(), property.getEntities().get(PRODUCTIONRESOURCE), property)) {
					toAdd.add(property);
				}
			}

			// Project
			else if (status.getProject() != null) {
				if (checkEntity(status.getProject(), property.getEntities().get(PROJECT), property)
						&& !checkEntity(status.getProcess(), property.getEntities().get(PROCESS), property)
						&& !checkEntity(status.getStep(), property.getEntities().get(STEP), property)
						&& !checkEntity(status.getProduct(), property.getEntities().get(PRODUCT), property)
						&& !checkEntity(status.getProductionResource(), property.getEntities().get(PRODUCTIONRESOURCE), property)) {
					toAdd.add(property);
				}
			}
		}

		return toAdd;
	}

	/***************************************************************************
	 * This method checks if the values of entities in status and property are the same. If not, the property will be removed from the list.
	 * 
	 * @param statusEntity
	 *            String
	 * @param propertyEntity
	 *            String
	 * @param property
	 *            Property
	 * @return Integer, index of property in {@link XMLBasedPropertyTemplateProvider#propertyList}
	 ****************************************************************************/
	private Boolean checkEntity(String statusEntity, String propertyEntity, PropertyTemplate property) {
		if (propertyEntity != null) {
			if (propertyEntity.equals("")) {
				return true;
			} else {
				if (statusEntity != null) {
					return propertyEntity.equals(statusEntity);
				} else {
					return true;
				}
			}
		}
		return false;

	}

	public void setFilepath(String filename) {
		this.filename = filename;
	}

	public String getFilepath() {
		return filename;
	}


	
	
	
}
