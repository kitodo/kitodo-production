package de.sub.goobi.Persistence.apache;

import java.util.Date;

public class Property {
	
	private int id; 
	private String title;
	private String value;
	private boolean IstObligatorisch;
	private int DatentypenID;
	private String auswahl;
	private Date creationDate;
	private int container;
	
	
	
	
	
	public Property(int id, String title, String value, boolean istObligatorisch, int datentypenID, String auswahl, 
			Date creationDate, int container) {
		this.id = id;
		this.title = title;
		this.value = value;
		this.IstObligatorisch = istObligatorisch;
		this.DatentypenID = datentypenID;
		this.auswahl = auswahl;
		this.creationDate = creationDate;
		this.container = container;
	}
	public int getId() {
		return this.id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getTitle() {
		return this.title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getValue() {
		return this.value;
	}
	public void setValue(String value) {
		this.value = value;
	}
	public boolean isIstObligatorisch() {
		return this.IstObligatorisch;
	}
	public void setIstObligatorisch(boolean istObligatorisch) {
		this.IstObligatorisch = istObligatorisch;
	}
	public int getDatentypenID() {
		return this.DatentypenID;
	}
	public void setDatentypenID(int datentypenID) {
		this.DatentypenID = datentypenID;
	}
	public String getAuswahl() {
		return this.auswahl;
	}
	public void setAuswahl(String auswahl) {
		this.auswahl = auswahl;
	}
	public Date getCreationDate() {
		return this.creationDate;
	}
	public void setCreationDate(Date creationDate) {
		this.creationDate = creationDate;
	}
	public int getContainer() {
		return this.container;
	}
	public void setContainer(int container) {
		this.container = container;
	}

	



}
