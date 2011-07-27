package org.goobi.production.api.property.xmlbasedprovider.impl;

/**
 * This file is part of the Goobi Application - a Workflow tool for the support of mass digitization.
 * 
 * Visit the websites for more information. - http://gdz.sub.uni-goettingen.de - http://www.intranda.com
 * 
 * Copyright 2009, Center for Retrospective Digitization, Göttingen (GDZ),
 * 
 * This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free
 * Software Foundation; either version 2 of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with this program; if not, write to the Free Software Foundation, Inc., 59
 * Temple Place, Suite 330, Boston, MA 02111-1307 USA
 * 
 * Linking this library statically or dynamically with other modules is making a combined work based on this library. Thus, the terms and conditions
 * of the GNU General Public License cover the whole combination. As a special exception, the copyright holders of this library give you permission to
 * link this library with independent modules to produce an executable, regardless of the license terms of these independent modules, and to copy and
 * distribute the resulting executable under terms of your choice, provided that you also meet, for each linked independent module, the terms and
 * conditions of the license of that module. An independent module is a module which is not derived from or based on this library. If you modify this
 * library, you may extend this exception to your version of the library, but you are not obliged to do so. If you do not wish to do so, delete this
 * exception statement from your version.
 * 
 */
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.TimeZone;

import org.apache.log4j.Logger;
import org.goobi.production.api.property.xmlbasedprovider.Status;

import de.sub.goobi.Beans.Batch;
import de.sub.goobi.Beans.Prozess;
import de.sub.goobi.Beans.Prozesseigenschaft;
import de.sub.goobi.Beans.Schritt;
import de.sub.goobi.Beans.Schritteigenschaft;
import de.sub.goobi.Beans.Vorlage;
import de.sub.goobi.Beans.Vorlageeigenschaft;
import de.sub.goobi.Beans.Werkstueck;
import de.sub.goobi.Beans.Werkstueckeigenschaft;
import de.sub.goobi.Beans.Property.BatchProperty;
import de.sub.goobi.Beans.Property.IGoobiEntity;
import de.sub.goobi.Beans.Property.IGoobiProperty;
import de.sub.goobi.helper.enums.PropertyType;

/**********************************************************************************
 * This Class represents property template.
 * 
 * @author Igor Toker
 * 
 ***********************************************************************************/
public class PropertyTemplate {
	private static final Logger logger = Logger.getLogger(PropertyTemplate.class);

	private String name;
	private List<String> valuesList = new ArrayList<String>();
	// if the type is list
	private String selectedValue;
	/** type - name */
	private HashMap<String, String> entities = new HashMap<String, String>();
	private IGoobiProperty maskedProp = null;
	private IGoobiEntity owningEntity;
	private boolean isUsed = false;

	// private Integer container;

	public PropertyTemplate(IGoobiEntity inEntity) {
		this.owningEntity = inEntity;
	}

	/**
	 * constructor building a new PropertyTemplate from a property
	 * 
	 * @param hgp
	 */
	public PropertyTemplate(IGoobiProperty hgp) {
		try {
			this.name = hgp.getTitel();
			this.owningEntity = hgp.getOwningEntity();
			maskedProp = hgp;
			selectedValue = hgp.getWert();

		} catch (Exception e) {
			logger.error("some error occured in constuctor", e);
		}
	}

	/**
	 * call this constructor, if you create a new propertyTemplate for a known entity
	 * 
	 * @param designatedParent
	 *            , representing the parental entity
	 */
	public PropertyTemplate(IGoobiEntity designatedParent, String name) {
		this.owningEntity = designatedParent;
		this.name = name;
		initPropery();
	}

	/**
	 * this method needs to be called whenever property methods are used to ensure that in case maskedProp is null one gets created
	 */
	private void initPropery() {
		if (this.maskedProp == null) {
//			if (owningEntity != null) {
				this.maskedProp = generateProperty(owningEntity, name);
				maskedProp.setWert(selectedValue);
				// } else {
				// logger.error("scheiße");
//			}
		}
	}

	private IGoobiProperty generateProperty(IGoobiEntity myEntity, String myName) {
		IGoobiProperty prop = null;
		if (myEntity instanceof Prozess) {
			prop = new Prozesseigenschaft();
		} else if (myEntity instanceof Schritt) {
			prop = new Schritteigenschaft();
		} else if (myEntity instanceof Vorlage) {
			prop = new Vorlageeigenschaft();
		} else if (myEntity instanceof Werkstueck) {
			prop = new Werkstueckeigenschaft();
		} else if (myEntity instanceof Batch) {
			prop = new BatchProperty();
		}
		prop.setOwningEntity(myEntity);
		prop.setTitel(myName);
		return prop;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.goobi.production.api.property.xmlbasedprovider.impl.IPropertyTemplate#getValue()
	 */
	public String getValue() {
		initPropery();
		if (getValuesList().size() > 0) {
			return getValuesList().get(0);
		} else {
			return "";
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.goobi.production.api.property.xmlbasedprovider.impl.IPropertyTemplate#setValue(java.lang.String)
	 */
	public void setValue(String value) {
		initPropery();
		setValuesList(new ArrayList<String>());
		getValuesList().add(value);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.goobi.production.api.property.xmlbasedprovider.impl.IPropertyTemplate#getName()
	 */
	public String getName() {
		initPropery();
		return name;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.goobi.production.api.property.xmlbasedprovider.impl.IPropertyTemplate#setName(java.lang.String)
	 */
	public void setName(String name) {
		initPropery();
		this.maskedProp.setTitel(name);
		this.name = name;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.goobi.production.api.property.xmlbasedprovider.impl.IPropertyTemplate#getValuesList()
	 */
	public List<String> getValuesList() {
		initPropery();
		return valuesList;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.goobi.production.api.property.xmlbasedprovider.impl.IPropertyTemplate#getSelectedValuesList()
	 */
	public List<String> getSelectedValuesList() {
		List<String> answerList = new ArrayList<String>();
		String v = getSelectedValue();
		if (v != null) {
			String[] answer = v.split("[\\s][|][\\s]");
			for (int i = 0; i < answer.length; ++i) {
				answerList.add(answer[i]);
			}
		}
		return answerList;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.goobi.production.api.property.xmlbasedprovider.impl.IPropertyTemplate#setSelectedValuesList(java.util.List)
	 */
	public void setSelectedValuesList(List<String> selected) {
		initPropery();
		String answer = "";
		for (String s : selected) {
			answer += s;
			answer += " | ";
		}
		setSelectedValue(answer);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.goobi.production.api.property.xmlbasedprovider.impl.IPropertyTemplate#setValuesList(java.util.List)
	 */
	public void setValuesList(List<String> valuesList) {
		initPropery();
		this.valuesList = valuesList;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.goobi.production.api.property.xmlbasedprovider.impl.IPropertyTemplate#getSelectedValue()
	 */
	public String getSelectedValue() {
		initPropery();
		return selectedValue;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.goobi.production.api.property.xmlbasedprovider.impl.IPropertyTemplate#getSelectedValueBeautified()
	 */
	public String getSelectedValueBeautified() {
		if (getType().equals(PropertyType.ListMultiSelect)) {
			if (selectedValue == null) {
				return "";
			}
			String back = selectedValue.replaceAll("[\\s][|][\\s]", ", ");
			if (back.endsWith(", ")) {
				back = back.substring(0, back.length() - 2);
			}
			return back;
		}
		return selectedValue;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.goobi.production.api.property.xmlbasedprovider.impl.IPropertyTemplate#setSelectedValue(java.lang.String)
	 */
	public void setSelectedValue(String selectedValue) {
		initPropery();
		this.selectedValue = selectedValue;
		this.maskedProp.setWert(selectedValue);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.goobi.production.api.property.xmlbasedprovider.impl.IPropertyTemplate#isRequired()
	 */
	public boolean isRequired() {
		initPropery();
		return maskedProp.isIstObligatorisch();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.goobi.production.api.property.xmlbasedprovider.impl.IPropertyTemplate#setRequired(java.lang.Boolean)
	 */
	public void setRequired(Boolean required) {
		initPropery();
		if (required != null) {
			maskedProp.setIstObligatorisch(required);
		} else {
			maskedProp.setIstObligatorisch(false);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.goobi.production.api.property.xmlbasedprovider.impl.IPropertyTemplate#getEntities()
	 */
	public HashMap<String, String> getEntities() {
		return entities;
	}

	/*
	 * central method to merge properties with templates
	 */
	private void addProperty(IGoobiProperty inProp) {
		this.maskedProp = inProp;
		// this.value = inProp.getWert();
		this.selectedValue = inProp.getWert();
		// this.setType(inProp.getType());
		// this.required = inProp.isIstObligatorisch();
		// if (this.getType() == null) {
		// this.setType(PropertyType.SpecialView);
		// }

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.goobi.production.api.property.xmlbasedprovider.impl.IPropertyTemplate#setProperty(de.sub.goobi.Beans.Property.IGoobiProperty)
	 */

	public boolean setProperty(IGoobiProperty inProp) {
		// refuse to accept any property which doesn't match my name
		IGoobiProperty hgp = inProp;
		try {
			// on this level we operate without property namespaces and only use the naked name
			// this.getName() already cuts out any namespace if there is one, but not the properties getName();
			if (this.name.equals(hgp.getTitel())) {

				// setting the value should be done before setting myProp
				// because setter triggers a synchronization
				addProperty(hgp);
				// logger.debug("name: " + hgp.getName() + " value: " + hgp.getValue() + " type: " + hgp.getType());
				return true;
			}
			return false;
		} catch (NullPointerException e) {
			// don't do anything, if someone tries
			// to pass on a null value
		}
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.goobi.production.api.property.xmlbasedprovider.impl.IPropertyTemplate#getProperty()
	 */
	public IGoobiProperty getProperty() {
		initPropery();
		return maskedProp;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.goobi.production.api.property.xmlbasedprovider.impl.IPropertyTemplate#getId()
	 */
	public Integer getId() {
		initPropery();
		return maskedProp.getId();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.goobi.production.api.property.xmlbasedprovider.impl.IPropertyTemplate#getTimeZone()
	 */
	public TimeZone getTimeZone() {
		return TimeZone.getDefault();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.goobi.production.api.property.xmlbasedprovider.impl.IPropertyTemplate#setDate(java.util.Date)
	 */
	public void setDate(Date inDate) {
		SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd");
		setSelectedValue(format.format(inDate));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.goobi.production.api.property.xmlbasedprovider.impl.IPropertyTemplate#getDate()
	 */
	public Date getDate() {
		SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd");
		try {
			Calendar cal = Calendar.getInstance();
			cal.setTime(format.parse(getSelectedValue()));
			cal.set(Calendar.HOUR, 12);
			return cal.getTime();
		} catch (ParseException e) {
			return new Date();
		} catch (NullPointerException e) {
			return new Date();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.goobi.production.api.property.xmlbasedprovider.impl.IPropertyTemplate#getStatus()
	 */

	public Status getStatus() {
		initPropery();
		return maskedProp.getStatus();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.goobi.production.api.property.xmlbasedprovider.impl.IPropertyTemplate#getOwningEntity()
	 */
	public IGoobiEntity getOwningEntity() {
		initPropery();
		return maskedProp.getOwningEntity();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.goobi.production.api.property.xmlbasedprovider.impl.IPropertyTemplate#setOwningEntity(de.sub.goobi.Beans.Property.IGoobiEntity)
	 */
	public void setOwningEntity(IGoobiEntity inEntity) {
		owningEntity = inEntity;
		initPropery();

		maskedProp.setOwningEntity(inEntity);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.goobi.production.api.property.xmlbasedprovider.impl.IPropertyTemplate#getAuswahl()
	 */
	public String getAuswahl() {
		initPropery();
		return maskedProp.getAuswahl();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.goobi.production.api.property.xmlbasedprovider.impl.IPropertyTemplate#getCreationDate()
	 */
	public Date getCreationDate() {
		initPropery();
		return maskedProp.getCreationDate();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.goobi.production.api.property.xmlbasedprovider.impl.IPropertyTemplate#getTitel()
	 */
	public String getTitel() {
		initPropery();
		return maskedProp.getTitel();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.goobi.production.api.property.xmlbasedprovider.impl.IPropertyTemplate#getType()
	 */
	public PropertyType getType() {
		initPropery();
		return maskedProp.getType();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.goobi.production.api.property.xmlbasedprovider.impl.IPropertyTemplate#getWert()
	 */
	public String getWert() {
		initPropery();
		return maskedProp.getWert();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.goobi.production.api.property.xmlbasedprovider.impl.IPropertyTemplate#isIstObligatorisch()
	 */
	public boolean isIstObligatorisch() {
		initPropery();
		return maskedProp.isIstObligatorisch();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.goobi.production.api.property.xmlbasedprovider.impl.IPropertyTemplate#setAuswahl(java.lang.String)
	 */
	public void setAuswahl(String auswahl) {
		initPropery();
		maskedProp.setAuswahl(auswahl);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.goobi.production.api.property.xmlbasedprovider.impl.IPropertyTemplate#setCreationDate(java.util.Date)
	 */
	public void setCreationDate(Date creation) {
		initPropery();
		maskedProp.setCreationDate(creation);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.goobi.production.api.property.xmlbasedprovider.impl.IPropertyTemplate#setTitel(java.lang.String)
	 */
	public void setTitel(String titel) {
		initPropery();
		this.name = titel;
		maskedProp.setTitel(titel);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.goobi.production.api.property.xmlbasedprovider.impl.IPropertyTemplate#setType(de.sub.goobi.helper.enums.PropertyType)
	 */
	public void setType(PropertyType inType) {
		initPropery();
		maskedProp.setType(inType);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.goobi.production.api.property.xmlbasedprovider.impl.IPropertyTemplate#setWert(java.lang.String)
	 */
	public void setWert(String wert) {
		initPropery();
		this.selectedValue = wert;
		maskedProp.setWert(wert);

	}

	public void setIsUsed(boolean isUsed) {
		this.isUsed = isUsed;
	}

	public boolean getIsUsed() {
		return isUsed;
	}

	public PropertyTemplate copy(int container) {
		PropertyTemplate pt = new PropertyTemplate(owningEntity, this.getName());
		IGoobiProperty ip = generateProperty(owningEntity, this.getName());
		pt.setProperty(ip);
		pt.setAuswahl(this.getAuswahl());
		pt.setCreationDate(new Date());
		pt.setDate(new Date());
		pt.setRequired(this.isRequired());
		pt.setSelectedValue(this.getSelectedValue());
		pt.setValuesList(this.getValuesList());
		pt.setTitel(this.getTitel());
		pt.setType(this.getType());
		pt.setValue(this.getValue());
		pt.setWert(this.getWert());
		pt.setContainer(container);
		pt.setOwningEntity(owningEntity);
		return pt;
	}

	/**
	 * @param container
	 *            the container to set
	 */
	public void setContainer(int container) {
		initPropery();
		// this.container = container;
		maskedProp.setContainer(container);
	}

	/**
	 * @return the container
	 */
	public int getContainer() {
		initPropery();
		// if (container == null) {
		// return 0;
		// }
		return maskedProp.getContainer();
	}

}
