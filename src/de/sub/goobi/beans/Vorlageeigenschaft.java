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

package de.sub.goobi.beans;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.goobi.production.api.property.xmlbasedprovider.Status;

import de.sub.goobi.beans.property.IGoobiEntity;
import de.sub.goobi.beans.property.IGoobiProperty;
import de.sub.goobi.helper.enums.PropertyType;

public class Vorlageeigenschaft implements Serializable, IGoobiProperty {
	private static final long serialVersionUID = -5981263038302791497L;
	private Vorlage vorlage;
	private Integer id;
	private String titel;
	private String wert;
	private Boolean istObligatorisch;
	private Integer datentyp;
	private String auswahl;
	private Date creationDate;
	private Integer container;

	public Vorlageeigenschaft() {
		istObligatorisch = false;
		datentyp = PropertyType.String.getId();
		creationDate = new Date();
	}

	private List<String> valueList;

	public String getAuswahl() {
		return auswahl;
	}

	public void setAuswahl(String auswahl) {
		this.auswahl = auswahl;
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public Boolean isIstObligatorisch() {
		if (istObligatorisch == null) {
			istObligatorisch = false;
		}
		return istObligatorisch;
	}

	public void setIstObligatorisch(Boolean istObligatorisch) {
		this.istObligatorisch = istObligatorisch;
	}

	public String getTitel() {
		return titel;
	}

	public void setTitel(String titel) {
		this.titel = titel;
	}

	public String getWert() {
		return wert;
	}

	public void setWert(String wert) {
		this.wert = wert;
	}

	public void setCreationDate(Date creation) {
		this.creationDate = creation;
	}

	public Date getCreationDate() {
		return creationDate;
	}

	/**
	 * getter for datentyp set to private for hibernate
	 * 
	 * for use in programm use getType instead
	 * 
	 * @return datentyp as integer
	 */
	@SuppressWarnings("unused")
	private Integer getDatentyp() {
		return datentyp;
	}

	/**
	 * set datentyp to defined integer. only for internal use through hibernate, for changing datentyp use setType instead
	 * 
	 * @param datentyp
	 *            as Integer
	 */
	@SuppressWarnings("unused")
	private void setDatentyp(Integer datentyp) {
		this.datentyp = datentyp;
	}

	/**
	 * set datentyp to specific value from {@link PropertyType}
	 * 
	 * @param inType
	 *            as {@link PropertyType}
	 */
	public void setType(PropertyType inType) {
		this.datentyp = inType.getId();
	}

	/**
	 * get datentyp as {@link PropertyType}
	 * 
	 * @return current datentyp
	 */
	public PropertyType getType() {
		if (datentyp == null) {
			datentyp = PropertyType.String.getId();
		}
		return PropertyType.getById(datentyp);
	}

	public List<String> getValueList() {
		if (valueList == null) {
			valueList = new ArrayList<String>();
		}
		return valueList;
	}

	public void setValueList(List<String> valueList) {
		this.valueList = valueList;
	}

	public void setVorlage(Vorlage vorlage) {
		this.vorlage = vorlage;
	}

	public Vorlage getVorlage() {
		return vorlage;
	}

	public Status getStatus() {
		return Status.getResourceStatusFromEntity(vorlage);
	}

	public IGoobiEntity getOwningEntity() {
		return vorlage;
	}

	public void setOwningEntity(IGoobiEntity inEntity) {
		this.vorlage = (Vorlage) inEntity;
	}
	
	public Integer getContainer() {
		if (container == null) {
			return 0;
		}
		return container;
	}

	public void setContainer(Integer order) {
		if (order == null) {
			order = 0;
		}
		this.container = order;
	}
	
	@Override
	public String getNormalizedTitle() {
		return titel.replace(" ", "_").trim();
	}

	@Override
	public String getNormalizedValue() {
		return wert.replace(" ", "_").trim();
	}
}
