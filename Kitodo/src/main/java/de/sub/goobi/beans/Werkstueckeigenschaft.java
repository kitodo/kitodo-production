package de.sub.goobi.beans;

/**
 * This file is part of the Goobi Application - a Workflow tool for the support of mass digitization.
 * 
 * Visit the websites for more information. 
 *     		- http://www.kitodo.org
 *     		- https://github.com/goobi/goobi-production
 * 		    - http://gdz.sub.uni-goettingen.de
 * 			- http://www.intranda.com
 * 			- http://digiverso.com 
 * 
 * This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free
 * Software Foundation; either version 2 of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
 * Linking this library statically or dynamically with other modules is making a combined work based on this library. Thus, the terms and conditions
 * of the GNU General Public License cover the whole combination. As a special exception, the copyright holders of this library give you permission to
 * link this library with independent modules to produce an executable, regardless of the license terms of these independent modules, and to copy and
 * distribute the resulting executable under terms of your choice, provided that you also meet, for each linked independent module, the terms and
 * conditions of the license of that module. An independent module is a module which is not derived from or based on this library. If you modify this
 * library, you may extend this exception to your version of the library, but you are not obliged to do so. If you do not wish to do so, delete this
 * exception statement from your version.
 */
import de.sub.goobi.beans.property.IGoobiProperty;
import de.sub.goobi.helper.enums.PropertyType;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Werkstueckeigenschaft implements Serializable, IGoobiProperty {
	private static final long serialVersionUID = -88407008893258729L;
	private Werkstueck werkstueck;
	private Integer id;
	private String titel;
	private String wert;
	private Boolean istObligatorisch;
	private Integer datentyp;
	private String auswahl;
	private Date creationDate;
	private Integer container;

	/**
	 *
	 */
	public Werkstueckeigenschaft() {
		this.istObligatorisch = false;
		this.datentyp = PropertyType.String.getId();
		this.creationDate = new Date();
	}

	private List<String> valueList;

	@Override
	public String getAuswahl() {
		return this.auswahl;
	}

	@Override
	public void setAuswahl(String auswahl) {
		this.auswahl = auswahl;
	}

	@Override
	public Integer getId() {
		return this.id;
	}

	@Override
	public void setId(Integer id) {
		this.id = id;
	}

	@Override
	public Boolean isIstObligatorisch() {
		if (this.istObligatorisch == null) {
			this.istObligatorisch = false;
		}
		return this.istObligatorisch;
	}

	@Override
	public void setIstObligatorisch(Boolean istObligatorisch) {
		this.istObligatorisch = istObligatorisch;
	}

	@Override
	public String getTitel() {
		return this.titel;
	}

	@Override
	public void setTitel(String titel) {
		this.titel = titel;
	}

	@Override
	public String getWert() {
		return this.wert;
	}

	@Override
	public void setWert(String wert) {
		this.wert = wert;
	}

	@Override
	public void setCreationDate(Date creation) {
		this.creationDate = creation;
	}

	@Override
	public Date getCreationDate() {
		return this.creationDate;
	}

	/**
	 * getter for datentyp set to private for hibernate for use in program use getType instead
	 *
	 * @return datentyp as integer
	 */
	@SuppressWarnings("unused")
	private Integer getDatentyp() {
		return this.datentyp;
	}

	/**
	 * set datentyp to defined integer. only for internal use through hibernate, for changing datentyp use setType
	 * instead
	 *
	 * @param datentyp as Integer
	 */
	@SuppressWarnings("unused")
	private void setDatentyp(Integer datentyp) {
		this.datentyp = datentyp;
	}

	/**
	 * set datentyp to specific value from {@link PropertyType}
	 *
	 * @param inType as {@link PropertyType}
	 */
	@Override
	public void setType(PropertyType inType) {
		this.datentyp = inType.getId();
	}

	/**
	 * get datentyp as {@link PropertyType}
	 *
	 * @return current datentyp
	 */
	@Override
	public PropertyType getType() {
		if (this.datentyp == null) {
			this.datentyp = PropertyType.String.getId();
		}
		return PropertyType.getById(this.datentyp);
	}

	/**
	 * @return add description
	 */
	public List<String> getValueList() {
		if (this.valueList == null) {
			this.valueList = new ArrayList<String>();
		}
		return this.valueList;
	}

	public void setValueList(List<String> valueList) {
		this.valueList = valueList;
	}

	public Werkstueck getWerkstueck() {
		return this.werkstueck;
	}

	public void setWerkstueck(Werkstueck werkstueck) {
		this.werkstueck = werkstueck;
	}

	@Override
	public Integer getContainer() {
		if (this.container == null) {
			return 0;
		}
		return this.container;
	}

	@Override
	public void setContainer(Integer order) {
		if (order == null) {
			order = 0;
		}
		this.container = order;
	}

	@Override
	public String getNormalizedTitle() {
		return this.titel.replace(" ", "_").trim();
	}

	@Override
	public String getNormalizedValue() {
		return this.wert.replace(" ", "_").trim();
	}
}
