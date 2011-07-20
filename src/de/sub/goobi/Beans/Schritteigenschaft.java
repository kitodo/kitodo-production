package de.sub.goobi.Beans;

import java.io.Serializable;
import java.util.Date;

import de.sub.goobi.helper.enums.PropertyType;

public class Schritteigenschaft implements Serializable {
	private static final long serialVersionUID = -443521810121056341L;
	private Integer id;
	private String titel;
	private String wert;
	private boolean istObligatorisch;
	private Integer datentyp;
	private String auswahl;
	private Schritt schritt;
	private Date creationDate;

	public Schritteigenschaft() {
	}

	/*#####################################################
	 #####################################################
	 ##                                                                                                                          
	 ##                                                             Getter und Setter                                   
	 ##                                                                                                                 
	 #####################################################
	 ####################################################*/

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

	public boolean isIstObligatorisch() {
		return istObligatorisch;
	}

	public void setIstObligatorisch(boolean istObligatorisch) {
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

	public Schritt getSchritt() {
		return schritt;
	}

	public void setSchritt(Schritt schritt) {
		this.schritt = schritt;
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
	 * @return datentyp as integer
	 */
	@SuppressWarnings("unused")
	private Integer getDatentyp() {
		return datentyp;
	}

	/**
	 * set datentyp to defined integer. only for internal 
	 * use through hibernate, for changing datentyp use 
	 * setType instead
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
	public void setType(PropertyType inType) {
		this.datentyp = inType.getValue();
	}

	/**
	 * get datentyp as {@link PropertyType}
	 * 
	 * @return current datentyp
	 */
	public PropertyType getType() {
		return PropertyType.getTypeFromValue(datentyp);
	}

}
