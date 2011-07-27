package de.sub.goobi.Beans.Property;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.goobi.production.api.property.xmlbasedprovider.Status;

import de.sub.goobi.Beans.Batch;
import de.sub.goobi.helper.enums.PropertyType;

public class BatchProperty implements Serializable, IGoobiProperty {


	private static final long serialVersionUID = -715998200230932879L;
	private Batch batch;
	private Integer id;
	private String titel;
	private String wert;
	private Boolean istObligatorisch;
	private Integer datentyp;
	private String auswahl;
	private Date creationDate;
	private Integer container;
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
	 * for use in programm use getType instead
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

	public Batch getBatch() {
		return this.batch;
	}

	public void setBatch(Batch prozess) {
		this.batch = prozess;
	}

	@Override
	public Status getStatus() {
		return Status.getBatchStatus(this.batch);
	}

	@Override
	public IGoobiEntity getOwningEntity() {

		return this.batch;
	}

	@Override
	public void setOwningEntity(IGoobiEntity inEntity) {
		this.batch = (Batch) inEntity;

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
