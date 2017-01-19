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

package org.kitodo.data.database.beans;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.kitodo.data.database.beans.property.IGoobiProperty;
import org.kitodo.data.database.helper.enums.PropertyType;

@Entity
@Table(name = "userProperty")
public class Benutzereigenschaft implements Serializable, IGoobiProperty {
	private static final long serialVersionUID = -2356566712752716107L;

	@Id
	@Column(name = "id")
	@GeneratedValue
	private Integer id;

	@Column(name = "title")
	private String titel;

	@Column(name = "value")
	private String wert;

	@Column(name = "isObligatory")
	private Boolean istObligatorisch;

	@Column(name = "dataType")
	private Integer datentyp;

	@Column(name = "choice")
	private String auswahl;

	@Column(name = "creationDate")
	private Date creationDate;

	@ManyToOne
	@JoinColumn(name = "user_id")
	private Benutzer benutzer;

	public Benutzereigenschaft() {
		this.istObligatorisch = false;
		this.datentyp = PropertyType.String.getId();
		this.creationDate = new Date();
	}

	@Transient
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
	 * getter for datentyp set to private for hibernate
	 * 
	 * for use in program use getType instead
	 * 
	 * @return datentyp as integer
	 */
	@SuppressWarnings("unused")
	private Integer getDatentyp() {
		return this.datentyp;
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

	public List<String> getValueList() {
		if (this.valueList == null) {
			this.valueList = new ArrayList<String>();
		}
		return this.valueList;
	}

	public void setValueList(List<String> valueList) {
		this.valueList = valueList;
	}

	/**
	 * 
	 * @return user (owner) of property
	 */

	public Benutzer getBenutzer() {
		return this.benutzer;
	}

	/**
	 * sets the user(owner) of property
	 * @param benutzer
	 */
	
	public void setBenutzer(Benutzer benutzer) {
		this.benutzer = benutzer;
	}

	

	@Override
	public Integer getContainer() {
		return 0;
	}

	@Override
	public void setContainer(Integer order) {
		
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
