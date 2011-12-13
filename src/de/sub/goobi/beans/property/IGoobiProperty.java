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
