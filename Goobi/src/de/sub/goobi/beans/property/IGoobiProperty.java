/*
 * (c) Kitodo. Key to digital objects e. V. <contact@kitodo.org>
 *
 * This file is part of the Kitodo project.
 *
 * It is licensed under GNU General Public License version 3 or later.
 *
 * For the full copyright and license information, please read the
 * GPL3-License.txt file that was distributed with this source code.
 */

package de.sub.goobi.beans.property;

import java.util.Date;


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


	public void setContainer(Integer order);
	
	public Integer getContainer();
	
	public String getNormalizedTitle();
	
	public String getNormalizedValue();
	
}
