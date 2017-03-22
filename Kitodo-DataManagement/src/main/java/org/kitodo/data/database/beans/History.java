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

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ForeignKey;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.kitodo.data.database.helper.enums.HistoryType;

/**
 * HistoryItem for any kind of history event of a {@link Process}
 * 
 * @author Steffen Hankiewicz
 * @version 24.05.2009
 */
@Entity
@Table(name = "history")
public class History extends BaseBean {
    private static final long serialVersionUID = 991946177515032238L;

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
    private Process process;

    /**
     * This constructor is only public for hibernate usage. If you want to
     * create a new History please use History(Date date, Number inNumericValue,
     * String inStringValue, HistoryEventType inHistoryEventType, Process
     * process)
     */
    public History() {

    }

    /**
     * Please use only this constructor.
     *
     * @param date
     *            Date of history event
     * @param inNumericValue
     *            value as Number (pages, size,...)
     * @param inStringValue
     *            value as string
     * @param inHistoryEventType
     *            type of History event( {@link HistoryType} )
     * @param process
     *            process of History
     */

    public History(Date date, Number inNumericValue, String inStringValue, HistoryType inHistoryEventType,
            Process process) {
        super();
        this.date = date;
        numericValue = inNumericValue.doubleValue();
        stringValue = inStringValue;
        type = inHistoryEventType.getValue();
        this.process = process;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public Process getProcess() {
        return process;
    }

    public void setProcess(Process process) {
        this.process = process;
    }

    public Double getNumericValue() {
        return numericValue;
    }

    public void setNumericValue(Double numericValue) {
        this.numericValue = numericValue;
    }

    public String getStringValue() {
        return stringValue;
    }

    public void setStringValue(String stringValue) {
        this.stringValue = stringValue;
    }

    /**
     * Getter for type as private method for Hibernate only.
     *
     * @return the type
     */
    @SuppressWarnings("unused")
    private Integer getType() {
        return type;
    }

    /**
     * Setter for type as private method for Hibernate only.
     *
     * @param type
     *            to set
     */
    @SuppressWarnings("unused")
    private void setType(Integer type) {
        this.type = type;
    }

    public HistoryType getHistoryType() {
        return HistoryType.getTypeFromValue(type);
    }

    public void setHistoryType(HistoryType type) {
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

            History event = (History) obj;
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
