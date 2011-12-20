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

package de.sub.goobi.beans.property;

import java.util.Date;

import org.goobi.production.api.property.xmlbasedprovider.Status;

import de.sub.goobi.helper.enums.PropertyType;

public interface IGoobiProperty {

	public String getAuswahl();

	public void setAuswahl(String auswahl);

	public Integer getId();

	public void setId(Integer id);

	public Boolean isIstObligatorisch();

	public void setIstObligatorisch(Boolean istObligatorisch);

	public String getTitel();

	public void setTitel(String titel);

	public String getWert();

	public void setWert(String wert);

	public void setCreationDate(Date creation);

	public Date getCreationDate();

	/**
	 * set datentyp to specific value from {@link PropertyType}
	 * 
	 * @param inType
	 *            as {@link PropertyType}
	 */
	public void setType(PropertyType inType);

	/**
	 * get datentyp as {@link PropertyType}
	 * 
	 * @return current datentyp
	 */
	public PropertyType getType();

	/**
	 * 
	 * @return {@link Status} of property
	 */
	public Status getStatus();

	/**
	 * 
	 * @return {@link IGoobiEntity} of property
	 */
	public IGoobiEntity getOwningEntity();

	/**
	 * sets {@link IGoobiEntity} of property
	 * @param inEntity
	 */
	public void setOwningEntity(IGoobiEntity inEntity);

	public void setContainer(Integer order);
	
	public Integer getContainer();
	
	public String getNormalizedTitle();
	
	public String getNormalizedValue();
	
}
