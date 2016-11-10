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
import java.io.Serializable;
import java.util.Date;

import de.sub.goobi.helper.enums.HistoryEventType;

/**
 * HistoryItem for any kind of history event of a {@link Prozess}
 * 
 * @author Steffen Hankiewicz
 * @version 24.05.2009
 */
public class HistoryEvent implements Serializable {
	private static final long serialVersionUID = 991946177515032238L;
	private Integer id;
	private Date date;
	private Double numericValue;
	private String stringValue;
	private Integer type;
	private Prozess process;

	/**
	 * This constructor is only public for hibernate usage. If you want to
	 * create a new HistoryEvent please use HistoryEvent(Date date, Number
	 * inNumericValue, String inStringValue, HistoryEventType
	 * inHistoryEventType, Prozess process)
	 * 
	 * 
	 */
	public HistoryEvent() {

	}

	/**
	 * Please use only this constructor.
	 * 
	 * @param date
	 *            Date of HistoryEvent
	 * @param inNumericValue
	 *            value as Number (pages, size,...)
	 * @param inStringValue
	 *            value as string
	 * @param inHistoryEventType
	 *            type of HistoryEvent ( {@link HistoryEventType} )
	 * @param process
	 *            process of HistoryEvent
	 */

	public HistoryEvent(Date date, Number inNumericValue, String inStringValue, HistoryEventType inHistoryEventType, Prozess process) {
		super();
		this.date = date;
		numericValue = inNumericValue.doubleValue();
		stringValue = inStringValue;
		type = inHistoryEventType.getValue();
		this.process = process;
	}

	/**
	 * Getter for ID
	 * 
	 * @return the id
	 */
	public Integer getId() {
		return id;
	}

	/**
	 * Setter for ID
	 * 
	 * @param id
	 *            the id to set
	 */
	public void setId(Integer id) {
		this.id = id;
	}

	/**
	 * Getter for date as {@link Date}
	 * 
	 * @return the date
	 */
	public Date getDate() {
		return date;
	}

	/**
	 * Setter for date
	 * 
	 * @param date
	 *            the date to set
	 */
	public void setDate(Date date) {
		this.date = date;
	}

	/**
	 * Getter for {@link Prozess}
	 * 
	 * @return the process
	 */
	public Prozess getProcess() {
		return process;
	}

	/**
	 * Setter for {@link Prozess}
	 * 
	 * @param process
	 *            the process to set
	 */
	public void setProcess(Prozess process) {
		this.process = process;
	}

	/**
	 * Getter for numericValue
	 * 
	 * @return numericValue as Double
	 */
	public Double getNumericValue() {
		return numericValue;
	}

	/**
	 * Setter for numericValue
	 * 
	 * @param numericValue
	 *            as Double
	 */
	public void setNumericValue(Double numericValue) {
		this.numericValue = numericValue;
	}

	/**
	 * Getter for stringValue
	 * 
	 * @return stringValue as String
	 */
	public String getStringValue() {
		return stringValue;
	}

	/**
	 * Setter for stringValue
	 * 
	 * @param stringValue
	 *            as String
	 */
	public void setStringValue(String stringValue) {
		this.stringValue = stringValue;
	}

	/**
	 * Getter for type as private method for Hibernate only
	 * 
	 * @return the type
	 */
	@SuppressWarnings("unused")
	private Integer getType() {
		return type;
	}

	/**
	 * Setter for type as private method for Hibernate only
	 * 
	 * @param type
	 *            the type to set
	 */
	@SuppressWarnings("unused")
	private void setType(Integer type) {
		this.type = type;
	}

	/**
	 * Getter for type
	 * 
	 * @return type as HistoryEventType
	 */
	public HistoryEventType getHistoryType() {
		return HistoryEventType.getTypeFromValue(type);
	}

	/**
	 * Setter for type
	 * 
	 * @param type
	 *            as HistoryEventType
	 */
	public void setHistoryType(HistoryEventType type) {
		this.type = type.getValue();
	}

	@Override
	public boolean equals(Object obj) {

		try {
			if (this == obj) {
				return true;
			}
			if ((obj == null) || (obj.getClass() != this.getClass())) {
				return false;
			}

			HistoryEvent event = (HistoryEvent) obj;
			if (event.getDate() == null) {
				return false;
			}
			if (!event.getDate().equals(getDate())) {
				return false;
			}

			if (!event.getHistoryType().equals(getHistoryType())) {
				return false;
			}

			if (!event.getNumericValue().equals(getNumericValue())) {
				return false;
			}

			if (!event.getStringValue().equals(getStringValue())) {
				return false;
			}
		} catch (Exception e) {
			return false;
		}

		return true;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((date == null) ? 0 : date.hashCode());
		result = prime * result + getHistoryType().hashCode();
		result = prime * result + ((numericValue == null) ? 0 : numericValue.hashCode());
		result = prime * result + ((stringValue == null) ? 0 : stringValue.hashCode());
		return result;
	}
}
