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

package de.sub.goobi.beans;

import de.sub.goobi.helper.enums.HistoryEventType;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ForeignKey;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

/**
 * HistoryItem for any kind of history event of a {@link Prozess}
 *
 * @author Steffen Hankiewicz
 * @version 24.05.2009
 */
@Entity
@Table(name = "history")
public class HistoryEvent implements Serializable {
	private static final long serialVersionUID = 991946177515032238L;

	@Id
	@Column(name = "id")
	@GeneratedValue
	private Integer id;

	@Column(name = "date")
	private Date date;

	@Column(name = "numericValue")
	private Double numericValue;

	@Column(name = "stringValue")
	private String stringValue;

	@Column(name = "type")
	private Integer type;

	@ManyToOne
	@JoinColumn(name = "process_id", foreignKey = @ForeignKey(name = "FK_history_process_id"))
	private Prozess process;

	/**
	 * This constructor is only public for hibernate usage. If you want to create a new HistoryEvent please use
	 * HistoryEvent(Date date, Number inNumericValue, String inStringValue, HistoryEventType
	 * inHistoryEventType, Prozess process)
	 */
	public HistoryEvent() {

	}

	/**
	 * Please use only this constructor.
	 *
	 * @param date Date of HistoryEvent
	 * @param inNumericValue value as Number (pages, size,...)
	 * @param inStringValue value as string
	 * @param inHistoryEventType type of HistoryEvent ( {@link HistoryEventType} )
	 * @param process process of HistoryEvent
	 */

	public HistoryEvent(Date date, Number inNumericValue, String inStringValue, HistoryEventType inHistoryEventType,
			Prozess process) {
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
	 * @param id the id to set
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
	 * @param date the date to set
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
	 * @param process the process to set
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
	 * @param numericValue as Double
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
	 * @param stringValue as String
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
	 * @param type the type to set
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
	 * @param type as HistoryEventType
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
