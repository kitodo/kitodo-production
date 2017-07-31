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

package org.kitodo.dto;

import org.kitodo.data.database.helper.enums.HistoryTypeEnum;

public class HistoryDTO extends BaseDTO {

    private Double numericValue;
    private String stringValue;
    private HistoryTypeEnum type;
    private String date;
    private ProcessDTO process;

    /**
     * Get numeric value.
     * 
     * @return numeric value as Double
     */
    public Double getNumericValue() {
        return numericValue;
    }

    /**
     * Set numeric value.
     * 
     * @param numericValue
     *            as Double
     */
    public void setNumericValue(Double numericValue) {
        this.numericValue = numericValue;
    }

    /**
     * Get string value.
     * 
     * @return string value as String
     */
    public String getStringValue() {
        return stringValue;
    }

    /**
     * Set string value.
     * 
     * @param stringValue
     *            as String
     */
    public void setStringValue(String stringValue) {
        this.stringValue = stringValue;
    }

    /**
     * Get type of history.
     * 
     * @return type of history as HistoryTypeEnum
     */
    public HistoryTypeEnum getType() {
        return type;
    }

    /**
     * Set type of history.
     * 
     * @param type
     *            as HistoryTypeEnum
     */
    public void setType(HistoryTypeEnum type) {
        this.type = type;
    }

    /**
     * Get date.
     * 
     * @return date as String
     */
    public String getDate() {
        return date;
    }

    /**
     * Set date.
     * 
     * @param date
     *            as String
     */
    public void setDate(String date) {
        this.date = date;
    }

    /**
     * Get process as ProcessDTO.
     *
     * @return process as ProcessDTO
     */
    public ProcessDTO getProcess() {
        return process;
    }

    /**
     * Set process as ProcessDTO.
     *
     * @param process
     *            as ProcessDTO
     */
    public void setProcess(ProcessDTO process) {
        this.process = process;
    }
}
